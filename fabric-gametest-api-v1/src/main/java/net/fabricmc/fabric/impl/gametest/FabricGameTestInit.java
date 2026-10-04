package net.fabricmc.fabric.impl.gametest;

import java.lang.reflect.Field;
import java.util.List;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.sinytra.fabric.gametest_api.generated.GeneratedEntryPoint;
import org.slf4j.Logger;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;

import net.fabricmc.fabric.impl.gametest.TestAnnotationLocator.TestMethod;

@Mod(GeneratedEntryPoint.MOD_ID)
public class FabricGameTestInit {
	private static final Logger LOGGER = LogUtils.getLogger();

	public FabricGameTestInit(IEventBus bus) {
		TestAnnotationLocator locator = new TestAnnotationLocator();
		List<TestMethod> methods = locator.getTestMethods();

		bus.addListener(RegisterGameTestsEvent.class, e -> {
			Registry<TestEnvironmentDefinition<?>> registry = getEnvironmentsRegistry(e);
			
			for (TestMethod method : methods) {
				e.registerTest(method.identifier(), method.testInstance(registry));
			}
		});

		bus.addListener(RegisterEvent.class, e -> {
			for (TestAnnotationLocator.TestMethod testMethod : methods) {
				LOGGER.debug("Registering test method: {}", testMethod.identifier());
				e.register(Registries.TEST_FUNCTION, testMethod.identifier(), testMethod::testFunction);
			}
		});
	}

	private Registry<TestEnvironmentDefinition<?>> getEnvironmentsRegistry(RegisterGameTestsEvent event) {
		try {
			Field field = RegisterGameTestsEvent.class.getDeclaredField("environmentsRegistry");
			field.setAccessible(true);
			return (Registry<TestEnvironmentDefinition<?>>) field.get(event);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
