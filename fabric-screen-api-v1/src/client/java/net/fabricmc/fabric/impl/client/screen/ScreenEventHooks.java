package net.fabricmc.fabric.impl.client.screen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import net.minecraft.client.gui.screens.Screen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

@EventBusSubscriber(Dist.CLIENT)
public class ScreenEventHooks {
	@SubscribeEvent
	public static void beforeScreenDraw(ScreenEvent.Render.Pre event) {
		Screen screen = event.getScreen();
		ScreenEvents.beforeExtract(screen).invoker().beforeExtract(screen, event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
	}

	@SubscribeEvent
	public static void afterScreenDraw(ScreenEvent.Render.Post event) {
		Screen screen = event.getScreen();
		ScreenEvents.afterExtract(screen).invoker().afterExtract(screen, event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
	}

	@SubscribeEvent
	public static void beforeKeyPressed(ScreenEvent.KeyPressed.Pre event) {
		Screen screen = event.getScreen();
		if (!ScreenKeyboardEvents.allowKeyPress(screen).invoker().allowKeyPress(screen, event.getKeyEvent())) {
			event.setCanceled(true);
		} else {
			ScreenKeyboardEvents.beforeKeyPress(screen).invoker().beforeKeyPress(screen, event.getKeyEvent());
		}
	}

	@SubscribeEvent
	public static void afterKeyPressed(ScreenEvent.KeyPressed.Post event) {
		Screen screen = event.getScreen();
		ScreenKeyboardEvents.afterKeyPress(screen).invoker().afterKeyPress(screen, event.getKeyEvent());
	}

	@SubscribeEvent
	public static void beforeKeyReleased(ScreenEvent.KeyReleased.Pre event) {
		Screen screen = event.getScreen();
		if (!ScreenKeyboardEvents.allowKeyRelease(screen).invoker().allowKeyRelease(screen, event.getKeyEvent())) {
			event.setCanceled(true);
		} else {
			ScreenKeyboardEvents.beforeKeyRelease(screen).invoker().beforeKeyRelease(screen, event.getKeyEvent());
		}
	}

	@SubscribeEvent
	public static void afterKeyReleased(ScreenEvent.KeyReleased.Post event) {
		Screen screen = event.getScreen();
		ScreenKeyboardEvents.afterKeyRelease(screen).invoker().afterKeyRelease(screen, event.getKeyEvent());
	}

	@SubscribeEvent
	public static void beforeMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
		Screen screen = event.getScreen();
		if (!ScreenMouseEvents.allowMouseClick(screen).invoker().allowMouseClick(screen, event.getMouseButtonEvent())) {
			event.setCanceled(true);
		} else {
			ScreenMouseEvents.beforeMouseClick(screen).invoker().beforeMouseClick(screen, event.getMouseButtonEvent());
		}
	}

	@SubscribeEvent
	public static void afterMouseClicked(ScreenEvent.MouseButtonPressed.Post event) {
		Screen screen = event.getScreen();
		ScreenMouseEvents.afterMouseClick(screen).invoker().afterMouseClick(screen, event.getMouseButtonEvent(), event.wasClickHandled());
	}

	@SubscribeEvent
	public static void beforeMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
		Screen screen = event.getScreen();
		if (!ScreenMouseEvents.allowMouseRelease(screen).invoker().allowMouseRelease(screen, event.getMouseButtonEvent())) {
			event.setCanceled(true);
		} else {
			ScreenMouseEvents.beforeMouseRelease(screen).invoker().beforeMouseRelease(screen, event.getMouseButtonEvent());
		}
	}

	@SubscribeEvent
	public static void afterMouseReleased(ScreenEvent.MouseButtonReleased.Post event) {
		Screen screen = event.getScreen();
		ScreenMouseEvents.afterMouseRelease(screen).invoker().afterMouseRelease(screen, event.getMouseButtonEvent(), event.wasReleaseHandled());
	}

	@SubscribeEvent
	public static void beforeMouseScroll(ScreenEvent.MouseScrolled.Pre event) {
		Screen screen = event.getScreen();
		if (!ScreenMouseEvents.allowMouseScroll(screen).invoker().allowMouseScroll(screen, event.getMouseX(), event.getMouseY(), event.getScrollDeltaX(), event.getScrollDeltaY())) {
			event.setCanceled(true);
		} else {
			ScreenMouseEvents.beforeMouseScroll(screen).invoker().beforeMouseScroll(screen, event.getMouseX(), event.getMouseY(), event.getScrollDeltaX(), event.getScrollDeltaY());
		}
	}

	@SubscribeEvent
	public static void afterMouseScroll(ScreenEvent.MouseScrolled.Post event) {
		Screen screen = event.getScreen();
		ScreenMouseEvents.afterMouseScroll(screen).invoker().afterMouseScroll(screen, event.getMouseX(), event.getMouseY(), event.getScrollDeltaX(), event.getScrollDeltaY(), false);
	}

	@SubscribeEvent
	public static void beforeMouseDragged(ScreenEvent.MouseDragged.Pre event) {
		Screen screen = event.getScreen();
		if (!ScreenMouseEvents.allowMouseDrag(screen).invoker().allowMouseDrag(screen, event.getMouseButtonEvent(), event.getDragX(), event.getDragY())) {
			event.setCanceled(true);
		} else {
			ScreenMouseEvents.beforeMouseDrag(screen).invoker().beforeMouseDrag(screen, event.getMouseButtonEvent(), event.getDragX(), event.getDragY());
		}
	}

	@SubscribeEvent
	public static void afterMouseDragged(ScreenEvent.MouseDragged.Post event) {
		Screen screen = event.getScreen();
		ScreenMouseEvents.afterMouseDrag(screen).invoker().afterMouseDrag(screen, event.getMouseButtonEvent(), event.getDragX(), event.getDragY(), false);
	}
	
	@SubscribeEvent
	public static void beforeCharTyped(ScreenEvent.CharacterTyped.Pre event) {
		Screen screen = event.getScreen();
		if (!ScreenKeyboardEvents.allowCharType(screen).invoker().allowCharType(screen, event.getCharacterEvent())) {
			event.setCanceled(true);
		} else {
			ScreenKeyboardEvents.beforeCharType(screen).invoker().beforeCharType(screen, event.getCharacterEvent());
		}
	}
	
	@SubscribeEvent
	public static void afterCharTypes(ScreenEvent.CharacterTyped.Post event) {
		Screen screen = event.getScreen();
		ScreenKeyboardEvents.afterCharType(screen).invoker().afterCharType(screen, event.getCharacterEvent());
	}
}
