package net.fabricmc.fabric.impl.client.keybinding;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.fabricmc.fabric.mixin.client.keybinding.KeyBindingAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class KeyBindingRegistryHandler {
    private static final List<KeyMapping> MODDED_KEY_BINDINGS = new ReferenceArrayList<>(); // ArrayList with identity based comparisons for contains/remove/indexOf etc., required for correctly handling duplicate keybinds

    static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        MODDED_KEY_BINDINGS.forEach(event::register);
    }

    private static Map<String, Integer> getCategoryMap() {
        return KeyBindingAccessor.fabric_getCategoryMap();
    }

    public static boolean addCategory(String categoryTranslationKey) {
        Map<String, Integer> map = getCategoryMap();

        if (map.containsKey(categoryTranslationKey)) {
            return false;
        }

        Optional<Integer> largest = map.values().stream().max(Integer::compareTo);
        int largestInt = largest.orElse(0);
        map.put(categoryTranslationKey, largestInt + 1);
        return true;
    }

    public static KeyMapping registerKeyBinding(KeyMapping binding) {
        for (KeyMapping existingKeyBindings : MODDED_KEY_BINDINGS) {
            if (existingKeyBindings == binding) {
                throw new IllegalArgumentException("Attempted to register a key binding twice: " + binding.getTranslatedKeyMessage());
            } else if (existingKeyBindings.getTranslatedKeyMessage().equals(binding.getTranslatedKeyMessage())) {
                throw new IllegalArgumentException("Attempted to register two key bindings with equal ID: " + binding.getTranslatedKeyMessage() + "!");
            }
        }

        // This will do nothing if the category already exists.
        addCategory(binding.getCategory());
        MODDED_KEY_BINDINGS.add(binding);
        return binding;
    }

    private KeyBindingRegistryHandler() {
    }
}
