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

package net.fabricmc.fabric.impl.block;

public final class EnchantmentPowerBridge {
	public static final float FABRIC_DEFAULT_POWER = 1.0F;

	private static final ScopedValue<Boolean> FABRIC_CALL = ScopedValue.newInstance();
	private static final ScopedValue<Boolean> NEO_CALL = ScopedValue.newInstance();

	public static float callNeo(PowerSupplier supplier) {
		return ScopedValue.where(FABRIC_CALL, true).call(supplier::get);
	}

	public static float callFabric(PowerSupplier supplier) {
		return ScopedValue.where(NEO_CALL, true).call(supplier::get);
	}

	public static boolean isFabricCall() {
		return FABRIC_CALL.orElse(false);
	}

	public static boolean isNeoCall() {
		return NEO_CALL.orElse(false);
	}

	@FunctionalInterface
	public interface PowerSupplier {
		float get();
	}

	private EnchantmentPowerBridge() {
	}
}
