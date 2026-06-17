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

package net.fabricmc.fabric.impl.client.rendering.hud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.fabricmc.fabric.mixin.client.rendering.GuiAccessor;

import net.fabricmc.fabric.mixin.client.rendering.GuiLayerManagerAccessor;

import net.minecraft.client.Minecraft;

import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.GuiLayerManager;
import net.neoforged.neoforge.client.gui.GuiLayerManager.NamedLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.VisibleForTesting;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public class HudElementRegistryImpl {
	@VisibleForTesting
	static final List<Identifier> VANILLA_ELEMENT_IDS = List.of(
			VanillaHudElements.MISC_OVERLAYS,
			VanillaHudElements.CROSSHAIR,
			VanillaHudElements.SPECTATOR_MENU,
			VanillaHudElements.HOTBAR,
			VanillaHudElements.ARMOR_BAR,
			VanillaHudElements.HEALTH_BAR,
			VanillaHudElements.FOOD_BAR,
			VanillaHudElements.AIR_BAR,
			VanillaHudElements.MOUNT_HEALTH,
			VanillaHudElements.INFO_BAR,
			VanillaHudElements.EXPERIENCE_LEVEL,
			VanillaHudElements.HELD_ITEM_TOOLTIP,
			VanillaHudElements.SPECTATOR_TOOLTIP,
			VanillaHudElements.MOB_EFFECTS,
			VanillaHudElements.BOSS_BAR,
			VanillaHudElements.SLEEP,
			VanillaHudElements.DEMO_TIMER,
			VanillaHudElements.SCOREBOARD,
			VanillaHudElements.OVERLAY_MESSAGE,
			VanillaHudElements.TITLE_AND_SUBTITLE,
			VanillaHudElements.CHAT,
			VanillaHudElements.PLAYER_LIST,
			VanillaHudElements.SUBTITLES
	);
	/**
	 * A map containing vanilla layers.
	 * This map should not be modified. Modify {@link RootLayer#layers()} instead.
	 */
	@VisibleForTesting
	public static final Map<Identifier, RootLayer> ROOT_ELEMENTS = VANILLA_ELEMENT_IDS.stream()
			.map(RootLayer::new)
			.collect(Collectors.toMap(RootLayer::id, Function.identity(), (a, b) -> a, IdentityHashMap::new));
	private static final RootLayer FIRST = ROOT_ELEMENTS.get(VanillaHudElements.MISC_OVERLAYS);
	private static final RootLayer LAST = ROOT_ELEMENTS.get(VanillaHudElements.SUBTITLES);
	
	private static final Map<Identifier, Identifier> FABRIC_TO_NEO_IDS = buildVanillaIDTranslations();
	private static boolean registered;
	private static List<Consumer<RegisterGuiLayersEvent>> LATE_CALLS = new ArrayList<>();

	public static void register(RegisterGuiLayersEvent event) {
		LATE_CALLS.forEach(c -> c.accept(event));
		LATE_CALLS = List.of(); // Make immutable
		registered = true;
	}
	
	private static void addLateLayer(Consumer<RegisterGuiLayersEvent> consumer) {
		if (registered) {
			GuiLayerManager manager = ((GuiAccessor) Minecraft.getInstance().gui).fabric$getLayerManager();
			List<NamedLayer> layers = ((GuiLayerManagerAccessor) manager).getLayers();

			RegisterGuiLayersEvent event = new RegisterGuiLayersEvent(layers);
			consumer.accept(event);
		} else {
			LATE_CALLS.add(consumer);
		}
	}

	public static RootLayer getRoot(Identifier id) {
		return ROOT_ELEMENTS.get(id);
	}

	public static void addFirst(Identifier id, HudElement element) {
		validateUnique(id);
		FIRST.layers().addFirst(HudLayer.ofElement(id, element));
		addLateLayer(e -> e.registerBelowAll(translate(id), asGuiLayer(element)));
	}

	public static void addLast(Identifier id, HudElement element) {
		validateUnique(id);
		LAST.layers().addLast(HudLayer.ofElement(id, element));
		addLateLayer(e -> e.registerAboveAll(translate(id), asGuiLayer(element)));
	}

	public static void attachElementBefore(Identifier beforeThis, Identifier id, HudElement element) {
		validateUnique(id);

		boolean didChange = findLayer(beforeThis, (l, iterator) -> {
			iterator.previous();
			iterator.add(HudLayer.ofElement(id, element));
			iterator.next();
			return true;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + beforeThis + " not found");
		}

		addLateLayer(e -> e.registerBelow(translate(beforeThis), translate(id), asGuiLayer(element)));
	}

	public static void attachElementAfter(Identifier afterThis, Identifier id, HudElement element) {
		validateUnique(id);

		boolean didChange = findLayer(afterThis, (l, iterator) -> {
			iterator.add(HudLayer.ofElement(id, element));
			return true;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + afterThis + " not found");
		}

		addLateLayer(e -> e.registerAbove(translate(afterThis), translate(id), asGuiLayer(element)));
	}

	public static void removeElement(Identifier identifier) {
		boolean didChange = findLayer(identifier, (l, iterator) -> {
			iterator.set(HudLayer.of(l.id(), l::element, true));
			return true;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + identifier + " not found");
		}

		addLateLayer(e -> e.wrapLayer(translate(identifier), l -> (g, d) -> {
		}));
	}

	public static void replaceElement(Identifier identifier, Function<HudElement, HudElement> replacer) {
		boolean didChange = findLayer(identifier, (l, iterator) -> {
			iterator.set(HudLayer.of(l.id(), replacer.compose(l::element), l.isRemoved()));
			return true;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + identifier + " not found");
		}

		addLateLayer(e -> e.wrapLayer(translate(identifier), l -> asGuiLayer(replacer.apply(asHudElement(l)))));
	}

	@VisibleForTesting
	static void validateUnique(Identifier id) {
		visitLayers((l, iterator) -> {
			if (l.id().equals(id)) {
				throw new IllegalArgumentException("Layer with identifier " + id + " already exists");
			}

			return false;
		});
	}

	/**
	 * @return true if an element with the given identifier was found
	 */
	@VisibleForTesting
	static boolean findLayer(Identifier identifier, LayerVisitor visitor) {
		MutableBoolean found = new MutableBoolean(false);

		visitLayers((l, iterator) -> {
			if (l.id().equals(identifier)) {
				found.setTrue();
				return visitor.visit(l, iterator);
			}

			return false;
		});

		return found.booleanValue();
	}

	@VisibleForTesting
	static boolean visitLayers(LayerVisitor visitor) {
		boolean modified = false;

		for (Identifier id : VANILLA_ELEMENT_IDS) {
			RootLayer rootLayer = ROOT_ELEMENTS.get(id);
			modified |= visitLayers(rootLayer.layers(), visitor);
		}

		return modified;
	}

	private static boolean visitLayers(List<HudLayer> layers, LayerVisitor visitor) {
		MutableBoolean modified = new MutableBoolean(false);
		ListIterator<HudLayer> iterator = layers.listIterator();

		while (iterator.hasNext()) {
			HudLayer layer = iterator.next();

			if (visitor.visit(layer, iterator)) {
				modified.setTrue();
			}
		}

		return modified.booleanValue();
	}

	@VisibleForTesting
	interface LayerVisitor {
		/**
		 * @return true if the list has been modified, false if not modified
		 */
		boolean visit(HudLayer layer, ListIterator<HudLayer> iterator);
	}

	/**
	 * An element that wraps a vanilla element using a list, allowing for users to attach layers before or after it, replace it, or remove it.
	 */
	public record RootLayer(Identifier id, List<HudLayer> layers) {
		private RootLayer(Identifier id) {
			this(id, new ArrayList<>());
			layers().add(HudLayer.ofVanilla(id));
		}

		public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, HudElement vanillaElement) {
			for (HudLayer layer : layers) {
				if (!layer.isRemoved()) {
					layer.element(vanillaElement).extractRenderState(graphics, deltaTracker);
				}
			}
		}
	}

	private static GuiLayer asGuiLayer(HudElement element) {
		return element::extractRenderState;
	}

	private static HudElement asHudElement(GuiLayer layer) {
		return layer::render;
	}
	
	private static Identifier translate(Identifier id) {
		return FABRIC_TO_NEO_IDS.getOrDefault(id, id);
	}
	
	private static Map<Identifier, Identifier> buildVanillaIDTranslations() {
		Map<Identifier, Identifier> map = new HashMap<>();
		
		map.put(VanillaHudElements.MISC_OVERLAYS, VanillaGuiLayers.CAMERA_OVERLAYS);
		map.put(VanillaHudElements.CROSSHAIR, VanillaGuiLayers.CROSSHAIR);
		map.put(VanillaHudElements.SPECTATOR_MENU, VanillaGuiLayers.HOTBAR);
		map.put(VanillaHudElements.HOTBAR, VanillaGuiLayers.HOTBAR);
		map.put(VanillaHudElements.ARMOR_BAR, VanillaGuiLayers.ARMOR_LEVEL);
		map.put(VanillaHudElements.HEALTH_BAR, VanillaGuiLayers.PLAYER_HEALTH);
		map.put(VanillaHudElements.FOOD_BAR, VanillaGuiLayers.FOOD_LEVEL);
		map.put(VanillaHudElements.AIR_BAR, VanillaGuiLayers.AIR_LEVEL);
		map.put(VanillaHudElements.MOUNT_HEALTH, VanillaGuiLayers.VEHICLE_HEALTH);
		map.put(VanillaHudElements.INFO_BAR, VanillaGuiLayers.CONTEXTUAL_INFO_BAR);
		map.put(VanillaHudElements.EXPERIENCE_LEVEL, VanillaGuiLayers.EXPERIENCE_LEVEL);
		map.put(VanillaHudElements.HELD_ITEM_TOOLTIP, VanillaGuiLayers.SELECTED_ITEM_NAME);
		map.put(VanillaHudElements.SPECTATOR_TOOLTIP, VanillaGuiLayers.SPECTATOR_TOOLTIP);
		map.put(VanillaHudElements.MOB_EFFECTS, VanillaGuiLayers.EFFECTS);
		map.put(VanillaHudElements.BOSS_BAR, VanillaGuiLayers.BOSS_OVERLAY);
		map.put(VanillaHudElements.SLEEP, VanillaGuiLayers.SLEEP_OVERLAY);
		map.put(VanillaHudElements.DEMO_TIMER, VanillaGuiLayers.DEMO_OVERLAY);
		map.put(VanillaHudElements.SCOREBOARD, VanillaGuiLayers.SCOREBOARD_SIDEBAR);
		map.put(VanillaHudElements.OVERLAY_MESSAGE, VanillaGuiLayers.OVERLAY_MESSAGE);
		map.put(VanillaHudElements.TITLE_AND_SUBTITLE, VanillaGuiLayers.TITLE);
		map.put(VanillaHudElements.CHAT, VanillaGuiLayers.CHAT);
		map.put(VanillaHudElements.PLAYER_LIST, VanillaGuiLayers.TAB_LIST);
		map.put(VanillaHudElements.SUBTITLES, VanillaGuiLayers.SUBTITLE_OVERLAY);
		
		return map;
	}
}
