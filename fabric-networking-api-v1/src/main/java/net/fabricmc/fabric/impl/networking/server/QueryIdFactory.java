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

package net.fabricmc.fabric.impl.networking.server;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks the current query id used for login query responses.
 */
interface QueryIdFactory {
	static QueryIdFactory create() {
		return new QueryIdFactory() {
			private final AtomicInteger currentId = new AtomicInteger();

			@Override
			public int nextId() {
				return this.currentId.getAndIncrement();
			}

			@Override
			public void set(int id) {
				this.currentId.set(id);
			}
		};
	}

	// called async prob.
	int nextId();

	// FFAPI: Setter to set initial offset
	void set(int id);
}
