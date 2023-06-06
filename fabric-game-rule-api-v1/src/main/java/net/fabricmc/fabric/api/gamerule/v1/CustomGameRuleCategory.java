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

package net.fabricmc.fabric.api.gamerule.v1;

import net.fabricmc.fabric.impl.gamerule.RuleKeyExtensions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;

import java.util.Optional;

/**
 * Utility class for creating custom game rule categories outside of the categories {@link GameRules.Category Minecraft provides}.
 */
public final class CustomGameRuleCategory {
	private final ResourceLocation id;
	private final Component name;

	/**
	 * Creates a custom game rule category.
	 *
	 * @param id the id of this category
	 * @param name the name of this category
	 */
	public CustomGameRuleCategory(ResourceLocation id, Component name) {
		this.id = id;
		this.name = name;
	}

	public ResourceLocation getId() {
		return this.id;
	}

	public Component getName() {
		return this.name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CustomGameRuleCategory that = (CustomGameRuleCategory) o;

		return this.id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	/**
	 * Gets the custom category a {@link GameRules.Key game rule key} is registered to.
	 *
	 * @param key the rule key
	 * @param <T> the type of value the rule holds
	 * @return the custom category this rule belongs to. Otherwise {@link Optional#empty() empty}
	 */
	public static <T extends GameRules.Value<T>> Optional<CustomGameRuleCategory> getCategory(GameRules.Key<T> key) {
		return Optional.ofNullable(((RuleKeyExtensions) (Object) key).fabric_getCustomCategory());
	}
}
