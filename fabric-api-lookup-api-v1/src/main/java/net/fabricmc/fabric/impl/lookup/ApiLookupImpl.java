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

package net.fabricmc.fabric.impl.lookup;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.impl.lookup.entity.EntityApiLookupImpl;
import net.minecraftforge.fml.common.Mod;

@Mod("fabric_api_lookup_api_v1")
public class ApiLookupImpl {

	public ApiLookupImpl() {
		ServerLifecycleEvents.SERVER_STARTED.register(EntityApiLookupImpl::checkSelfImplementingTypes);
	}
}
