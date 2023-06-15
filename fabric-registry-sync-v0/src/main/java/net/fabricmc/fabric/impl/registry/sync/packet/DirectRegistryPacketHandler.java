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

package net.fabricmc.fabric.impl.registry.sync.packet;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A more optimized method to sync registry ids to client.
 * Produce smaller packet than old nbt-based method.
 *
 * <p>This method optimize the packet in multiple way:
 * <ul>
 *     <li>Directly write into the buffer instead of using an nbt;</li>
 *     <li>Group all {@link ResourceLocation} with same namespace together and only send those unique namespaces once for each group;</li>
 *     <li>Group consecutive rawIds together and only send the difference of the first rawId and the last rawId of the bulk before.
 *     This is based on the assumption that mods generally register all of their object at once,
 *     therefore making the rawIds somewhat densely packed.</li>
 * </ul>
 *
 * <p>This method also split into multiple packets if it exceeds the limit, defaults to 1 MB.
 */
public class DirectRegistryPacketHandler extends RegistryPacketHandler {
	/**
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#MAX_PAYLOAD_SIZE
	 */
	@SuppressWarnings("JavadocReference")
	private static final int MAX_PAYLOAD_SIZE = Integer.getInteger("fabric.registry.direct.maxPayloadSize", 0x100000);
	private static final ResourceLocation ID = new ResourceLocation("fabric", "registry/sync/direct");

	@Nullable
	private FriendlyByteBuf combinedBuf;

	@Nullable
	private Map<ResourceLocation, Object2IntMap<ResourceLocation>> syncedRegistryMap;

	private boolean isPacketFinished = false;
	private int totalPacketReceived = 0;

	@Override
	public ResourceLocation getPacketId() {
		return ID;
	}

	@Override
	public void sendPacket(ServerPlayer player, Map<ResourceLocation, Object2IntMap<ResourceLocation>> registryMap) {
		FriendlyByteBuf buf = PacketByteBufs.create();

		// Group registry ids with same namespace.
		Map<String, List<ResourceLocation>> regNamespaceGroups = registryMap.keySet().stream()
				.collect(Collectors.groupingBy(ResourceLocation::getNamespace));

		buf.writeVarInt(regNamespaceGroups.size());

		regNamespaceGroups.forEach((regNamespace, regIds) -> {
			buf.writeUtf(optimizeNamespace(regNamespace));
			buf.writeVarInt(regIds.size());

			for (ResourceLocation regId : regIds) {
				buf.writeUtf(regId.getPath());

				Object2IntMap<ResourceLocation> idMap = registryMap.get(regId);

				// Sort object ids by its namespace. We use linked map here to keep the original namespace ordering.
				Map<String, List<Object2IntMap.Entry<ResourceLocation>>> idNamespaceGroups = idMap.object2IntEntrySet().stream()
						.collect(Collectors.groupingBy(e -> e.getKey().getNamespace(), LinkedHashMap::new, Collectors.toCollection(ArrayList::new)));

				buf.writeVarInt(idNamespaceGroups.size());

				int lastBulkLastRawId = 0;

				for (Map.Entry<String, List<Object2IntMap.Entry<ResourceLocation>>> idNamespaceEntry : idNamespaceGroups.entrySet()) {
					// Make sure the ids are sorted by its raw id.
					List<Object2IntMap.Entry<ResourceLocation>> idPairs = idNamespaceEntry.getValue();
					idPairs.sort(Comparator.comparingInt(Object2IntMap.Entry::getIntValue));

					// Group consecutive raw ids together.
					List<List<Object2IntMap.Entry<ResourceLocation>>> bulks = new ArrayList<>();

					Iterator<Object2IntMap.Entry<ResourceLocation>> idPairIter = idPairs.iterator();
					List<Object2IntMap.Entry<ResourceLocation>> currentBulk = new ArrayList<>();
					Object2IntMap.Entry<ResourceLocation> currentPair = idPairIter.next();
					currentBulk.add(currentPair);

					while (idPairIter.hasNext()) {
						currentPair = idPairIter.next();

						if (currentBulk.get(currentBulk.size() - 1).getIntValue() + 1 != currentPair.getIntValue()) {
							bulks.add(currentBulk);
							currentBulk = new ArrayList<>();
						}

						currentBulk.add(currentPair);
					}

					bulks.add(currentBulk);

					buf.writeUtf(optimizeNamespace(idNamespaceEntry.getKey()));
					buf.writeVarInt(bulks.size());

					for (List<Object2IntMap.Entry<ResourceLocation>> bulk : bulks) {
						int firstRawId = bulk.get(0).getIntValue();
						int bulkRawIdStartDiff = firstRawId - lastBulkLastRawId;

						buf.writeVarInt(bulkRawIdStartDiff);
						buf.writeVarInt(bulk.size());

						for (Object2IntMap.Entry<ResourceLocation> idPair : bulk) {
							buf.writeUtf(idPair.getKey().getPath());

							lastBulkLastRawId = idPair.getIntValue();
						}
					}
				}
			}
		});

		// Split the packet to multiple MAX_PAYLOAD_SIZEd buffers.
		int readableBytes = buf.readableBytes();
		int sliceIndex = 0;

		while (sliceIndex < readableBytes) {
			int sliceSize = Math.min(readableBytes - sliceIndex, MAX_PAYLOAD_SIZE);
			FriendlyByteBuf slicedBuf = PacketByteBufs.slice(buf, sliceIndex, sliceSize);
			sendPacket(player, slicedBuf);
			sliceIndex += sliceSize;
		}

		// Send an empty buffer to mark the end of the split.
		sendPacket(player, PacketByteBufs.empty());
	}

	@Override
	public void receivePacket(FriendlyByteBuf slicedBuf) {
		Preconditions.checkState(!isPacketFinished);
		totalPacketReceived++;

		if (combinedBuf == null) {
			combinedBuf = PacketByteBufs.create();
		}

		if (slicedBuf.readableBytes() != 0) {
			combinedBuf.writeBytes(slicedBuf);
			return;
		}

		isPacketFinished = true;

		computeBufSize(combinedBuf);
		syncedRegistryMap = new LinkedHashMap<>();
		int regNamespaceGroupAmount = combinedBuf.readVarInt();

		for (int i = 0; i < regNamespaceGroupAmount; i++) {
			String regNamespace = unoptimizeNamespace(combinedBuf.readUtf());
			int regNamespaceGroupLength = combinedBuf.readVarInt();

			for (int j = 0; j < regNamespaceGroupLength; j++) {
				String regPath = combinedBuf.readUtf();
				Object2IntMap<ResourceLocation> idMap = new Object2IntLinkedOpenHashMap<>();
				int idNamespaceGroupAmount = combinedBuf.readVarInt();

				int lastBulkLastRawId = 0;

				for (int k = 0; k < idNamespaceGroupAmount; k++) {
					String idNamespace = unoptimizeNamespace(combinedBuf.readUtf());
					int rawIdBulkAmount = combinedBuf.readVarInt();

					for (int l = 0; l < rawIdBulkAmount; l++) {
						int bulkRawIdStartDiff = combinedBuf.readVarInt();
						int bulkSize = combinedBuf.readVarInt();

						int currentRawId = (lastBulkLastRawId + bulkRawIdStartDiff) - 1;

						for (int m = 0; m < bulkSize; m++) {
							currentRawId++;
							String idPath = combinedBuf.readUtf();
							idMap.put(new ResourceLocation(idNamespace, idPath), currentRawId);
						}

						lastBulkLastRawId = currentRawId;
					}
				}

				syncedRegistryMap.put(new ResourceLocation(regNamespace, regPath), idMap);
			}
		}

		combinedBuf.release();
		combinedBuf = null;
	}

	@Override
	public boolean isPacketFinished() {
		return isPacketFinished;
	}

	@Override
	public int getTotalPacketReceived() {
		Preconditions.checkState(isPacketFinished);
		return totalPacketReceived;
	}

	@Override
	@Nullable
	public Map<ResourceLocation, Object2IntMap<ResourceLocation>> getSyncedRegistryMap() {
		Preconditions.checkState(isPacketFinished);
		Map<ResourceLocation, Object2IntMap<ResourceLocation>> map = syncedRegistryMap;
		isPacketFinished = false;
		totalPacketReceived = 0;
		syncedRegistryMap = null;
		return map;
	}

	private static String optimizeNamespace(String namespace) {
		return namespace.equals(ResourceLocation.DEFAULT_NAMESPACE) ? "" : namespace;
	}

	private static String unoptimizeNamespace(String namespace) {
		return namespace.isEmpty() ? ResourceLocation.DEFAULT_NAMESPACE : namespace;
	}
}
