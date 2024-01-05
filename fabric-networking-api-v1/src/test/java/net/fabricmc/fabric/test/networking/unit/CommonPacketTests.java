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

package net.fabricmc.fabric.test.networking.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.impl.networking.CommonPacketsImpl;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;

public class CommonPacketTests {
	@BeforeAll
	static void beforeAll() {
		CommonPacketsImpl.init();
		ClientNetworkingImpl.clientInit();
	}

	@Test
	public void testHighestCommonVersionWithCommonElement() {
		int[] a = {1, 2, 3};
		int[] b = {1, 2};
		assertEquals(2, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}

	@Test
	public void testHighestCommonVersionWithoutCommonElement() {
		int[] a = {1, 3, 5};
		int[] b = {2, 4, 6};
		assertEquals(-1, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}

	@Test
	public void testHighestCommonVersionWithOneEmptyArray() {
		int[] a = {1, 3, 5};
		int[] b = {};
		assertEquals(-1, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}

	@Test
	public void testHighestCommonVersionWithBothEmptyArrays() {
		int[] a = {};
		int[] b = {};
		assertEquals(-1, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}

	@Test
	public void testHighestCommonVersionWithIdenticalArrays() {
		int[] a = {1, 2, 3};
		int[] b = {1, 2, 3};
		assertEquals(3, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}
}
