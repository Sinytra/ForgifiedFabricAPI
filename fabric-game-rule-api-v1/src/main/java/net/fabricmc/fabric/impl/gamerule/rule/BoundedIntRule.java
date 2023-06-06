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

package net.fabricmc.fabric.impl.gamerule.rule;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BoundedIntRule extends GameRules.IntegerValue {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameRuleRegistry.class);

	private final int minimumValue;
	private final int maximumValue;

	public BoundedIntRule(GameRules.Type<GameRules.IntegerValue> type, int initialValue, int minimumValue, int maximumValue) {
		super(type, initialValue);
		this.minimumValue = minimumValue;
		this.maximumValue = maximumValue;
	}

	@Override
	protected void deserialize(String value) {
		final int i = BoundedIntRule.parseInt(value);

		if (this.minimumValue > i || this.maximumValue < i) {
			LOGGER.warn("Failed to parse integer {}. Was out of bounds {} - {}", value, this.minimumValue, this.maximumValue);
			return;
		}

		this.value = i;
	}

	@Override
	public boolean tryDeserialize(String input) {
		try {
			int value = Integer.parseInt(input);

			if (this.minimumValue > value || this.maximumValue < value) {
				return false;
			}

			this.value = value;
			return true;
		} catch (NumberFormatException var3) {
			return false;
		}
	}

	@Override
	protected GameRules.IntegerValue copy() {
		return new BoundedIntRule(this.type, this.value, this.minimumValue, this.maximumValue);
	}

	private static int parseInt(String input) {
		if (!input.isEmpty()) {
			try {
				return Integer.parseInt(input);
			} catch (NumberFormatException var2) {
				LOGGER.warn("Failed to parse integer {}", input);
			}
		}

		return 0;
	}
}
