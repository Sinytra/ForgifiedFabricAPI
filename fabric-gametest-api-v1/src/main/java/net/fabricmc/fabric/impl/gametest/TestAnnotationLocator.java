/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.gametest;

import java.lang.annotation.ElementType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;

final class TestAnnotationLocator {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestAnnotationLocator.class);

	private List<TestMethod> testMethods = null;

	public List<TestMethod> getTestMethods() {
		if (testMethods != null) {
			return testMethods;
		}

		return testMethods = findNeoForgeTestMethods();
	}

	private List<TestMethod> findNeoForgeTestMethods() {
		List<TestMethod> results = new ArrayList<>();

		for (ModFileScanData data : ModList.get().getAllScanData()) {
			IModFileInfo modFileInfo = data.getIModInfoData().getFirst();
			IModInfo modInfo = modFileInfo.getMods().getFirst();
			String modid = modInfo.getModId();

			data.getAnnotatedBy(GameTest.class, ElementType.METHOD).forEach(ann -> {
				try {
					Class<?> clazz = Class.forName(ann.clazz().getClassName());

					Method method = getMethod(clazz, ann.memberName());
					if (method == null)
						return;

					if (!CustomTestMethodInvoker.class.isAssignableFrom(clazz)) {
						validateMethod(method);
					}

					Supplier<Object> instanceSupplier = Suppliers.memoize(() -> {
						try {
							return clazz.getConstructor().newInstance();
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					});

					results.add(new TestMethod(clazz, method, method.getAnnotation(GameTest.class), modid, instanceSupplier));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}

		return results;
	}

	private void validateMethod(Method method) {
		List<String> issues = new ArrayList<>();

		if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != GameTestHelper.class) {
			issues.add("must have a single parameter of type TestContext");
		}

		if (!Modifier.isPublic(method.getModifiers())) {
			issues.add("must be public");
		}

		if (Modifier.isStatic(method.getModifiers())) {
			issues.add("must not be static");
		}

		if (method.getReturnType() != void.class) {
			issues.add("must return void");
		}

		if (issues.isEmpty()) {
			return;
		}

		String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
		throw new UnsupportedOperationException("Test method (%s) has the following issues: %s".formatted(methodName, String.join(", ", issues)));
	}

	public record TestMethod(Class<?> clazz, Method method, GameTest gameTest, String modid,
	                         Supplier<Object> instanceSupplier) {
		Identifier identifier() {
			String name = camelToSnake(clazz.getSimpleName() + "_" + method.getName());
			return Identifier.fromNamespaceAndPath(modid, name);
		}

		Consumer<GameTestHelper> testFunction() {
			return context -> {
				Object instance = instanceSupplier.get();

				try {
					if (instance instanceof CustomTestMethodInvoker customTestMethodInvoker) {
						customTestMethodInvoker.invokeTestMethod(context, method);
						return;
					}

					method.invoke(instance, context);
				} catch (InvocationTargetException e) {
					LOGGER.error("Failed to invoke test method", e);

					// Ensure that any GameTestException are propagated without wrapping
					if (e.getTargetException() instanceof RuntimeException runtimeException) {
						throw runtimeException;
					}

					throw new RuntimeException("Failed to invoke test method: " + e.getMessage(), e);
				} catch (ReflectiveOperationException e) {
					LOGGER.error("Failed to invoke test method", e);
					throw new RuntimeException("Failed to invoke test method: " + e.getMessage(), e);
				}
			};
		}

		TestData<Holder<TestEnvironmentDefinition<?>>> testData(Registry<TestEnvironmentDefinition<?>> testEnvironmentDefinitionRegistry) {
			Holder<TestEnvironmentDefinition<?>> testEnvironment = testEnvironmentDefinitionRegistry.getOrThrow(ResourceKey.create(Registries.TEST_ENVIRONMENT, Identifier.parse(gameTest.environment())));

			return new TestData<>(
					testEnvironment,
					Identifier.parse(gameTest.structure()),
					gameTest.maxTicks(),
					gameTest.setupTicks(),
					gameTest.required(),
					gameTest.rotation(),
					gameTest.manualOnly(),
					gameTest.maxAttempts(),
					gameTest.requiredSuccesses(),
					gameTest.skyAccess(),
					gameTest.padding()
			);
		}

		GameTestInstance testInstance(Registry<TestEnvironmentDefinition<?>> testEnvironmentDefinitionRegistry) {
			return new FunctionGameTestInstance(
					ResourceKey.create(Registries.TEST_FUNCTION, identifier()),
					testData(testEnvironmentDefinitionRegistry)
			);
		}

		private static String camelToSnake(String input) {
			return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
		}
	}

	public static Method getMethod(Class<?> owner, String nameAndDescriptor) throws Exception {
		int parenIndex = nameAndDescriptor.indexOf('(');
		String methodName = nameAndDescriptor.substring(0, parenIndex);
		String descriptor = nameAndDescriptor.substring(parenIndex);

		Type[] argTypes = Type.getArgumentTypes(descriptor);
		Class<?>[] paramClasses = new Class<?>[argTypes.length];
		for (int i = 0; i < argTypes.length; i++) {
			paramClasses[i] = typeToClass(argTypes[i]);
		}

		return owner.getDeclaredMethod(methodName, paramClasses);
	}

	private static Class<?> typeToClass(Type type) throws ClassNotFoundException {
		return switch (type.getSort()) {
			case Type.VOID -> void.class;
			case Type.BOOLEAN -> boolean.class;
			case Type.BYTE -> byte.class;
			case Type.CHAR -> char.class;
			case Type.SHORT -> short.class;
			case Type.INT -> int.class;
			case Type.LONG -> long.class;
			case Type.FLOAT -> float.class;
			case Type.DOUBLE -> double.class;
			case Type.ARRAY -> Class.forName(type.getDescriptor().replace('/', '.'));
			case Type.OBJECT -> Class.forName(type.getInternalName().replace('/', '.'));
			default -> throw new IllegalArgumentException("Unknown type: " + type);
		};
	}
}
