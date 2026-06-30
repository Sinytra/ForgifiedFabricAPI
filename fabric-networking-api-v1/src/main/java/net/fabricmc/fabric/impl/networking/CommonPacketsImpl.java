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

package net.fabricmc.fabric.impl.networking;

import java.util.Arrays;

public class CommonPacketsImpl {
	public static void init() {
	}

	public static int getHighestCommonVersion(int[] a, int[] b) {
		int[] as = a.clone();
		int[] bs = b.clone();

		Arrays.sort(as);
		Arrays.sort(bs);

		int ap = as.length - 1;
		int bp = bs.length - 1;

		while (ap >= 0 && bp >= 0) {
			if (as[ap] == bs[bp]) {
				return as[ap];
			}

			if (as[ap] > bs[bp]) {
				ap--;
			} else {
				bp--;
			}
		}

		return -1;
	}
}
