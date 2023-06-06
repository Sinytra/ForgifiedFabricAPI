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

package net.fabricmc.fabric.api.gamerule.v1.rule;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DoubleRule extends GameRules.Value<DoubleRule> implements ValidateableRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameRuleRegistry.class);

    private final double minimumValue;
    private final double maximumValue;
    private double value;

    /**
     * @deprecated You should not be calling this constructor!
     */
    @Deprecated
    public DoubleRule(GameRules.Type<DoubleRule> type, double value, double minimumValue, double maximumValue) {
        super(type);
        this.value = value;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;

        if (Double.isInfinite(value) || Double.isNaN(value)) {
            throw new IllegalArgumentException("Value cannot be infinite or NaN");
        }

        if (Double.isInfinite(minimumValue) || Double.isNaN(minimumValue)) {
            throw new IllegalArgumentException("Minimum value cannot be infinite or NaN");
        }

        if (Double.isInfinite(maximumValue) || Double.isNaN(maximumValue)) {
            throw new IllegalArgumentException("Maximum value cannot be infinite or NaN");
        }
    }

    @Override
    public void updateFromArgument(CommandContext<CommandSourceStack> context, String name) {
        this.value = context.getArgument(name, Double.class);
    }

    @Override
    protected void deserialize(String value) {
        if (!value.isEmpty()) {
            try {
                final double d = Double.parseDouble(value);

                if (this.inBounds(d)) {
                    this.value = d;
                } else {
                    LOGGER.warn("Failed to parse double {}. Was out of bounds {} - {}", value, this.minimumValue, this.maximumValue);
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("Failed to parse double {}", value);
            }
        }
    }

    @Override
    public String serialize() {
        return Double.toString(this.value);
    }

    @Override
    public int getCommandResult() {
        return Double.compare(this.value, 0.0D);
    }

    // Stub method to preserve API compat
    protected DoubleRule getThis() {
        return this;
    }

    @Override
    protected DoubleRule getSelf() {
        return this;
    }

    @Override
    protected DoubleRule copy() {
        return new DoubleRule(this.type, this.value, this.minimumValue, this.maximumValue);
    }

    // Stub method to preserve API compat
    public void setValue(DoubleRule rule, MinecraftServer minecraftServer) {
        setFrom(rule, minecraftServer);
    }

    @Override
    public void setFrom(DoubleRule rule, MinecraftServer minecraftServer) {
        if (!this.inBounds(rule.value)) {
            throw new IllegalArgumentException(String.format("Could not set value to %s. Was out of bounds %s - %s", rule.value, this.minimumValue, this.maximumValue));
        }

        this.value = rule.value;
        this.onChanged(minecraftServer);
    }

    @Override
    public boolean validate(String value) {
        try {
            final double d = Double.parseDouble(value);

            return this.inBounds(d);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public double get() {
        return this.value;
    }

    private boolean inBounds(double value) {
        return value >= this.minimumValue && value <= this.maximumValue;
    }
}
