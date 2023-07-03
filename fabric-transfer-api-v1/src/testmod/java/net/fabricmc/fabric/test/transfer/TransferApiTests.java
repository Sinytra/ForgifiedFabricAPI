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

package net.fabricmc.fabric.test.transfer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

import net.fabricmc.fabric.test.transfer.ingame.TransferTestInitializer;
import net.fabricmc.fabric.test.transfer.ingame.client.FluidVariantRenderTest;
import net.fabricmc.fabric.test.transfer.unittests.UnitTestsInitializer;

@Mod(TransferApiTests.MODID)
public class TransferApiTests {
    public static final String MODID = "fabric_transfer_api_v1_testmod";
    public static final String NAMESPACE = "fabric-transfer-api-v1-testmod";

    public TransferApiTests() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        if (FMLLoader.getDist() == Dist.CLIENT) {
            FluidVariantRenderTest.onInitializeClient();
        }
        TransferTestInitializer.onInitialize(bus);
        UnitTestsInitializer.onInitialize();
    }
}
