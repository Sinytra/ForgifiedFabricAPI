package net.fabricmc.fabric.impl.datagen;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.minecraftforge.fml.common.Mod;

@Mod("fabric_data_generation_api_v1")
public class FabricDatagenImpl {

    public FabricDatagenImpl() {
    }

    /**
     * Adjust the default sort order of some keys provided by Fabric API.
     * Referenced from the addFabricKeySortOrders.js coremod.
     */
    @SuppressWarnings("unused")
    public static void addFabricKeySortOrders(Object2IntOpenHashMap<String> map) {
        map.put(ResourceConditions.CONDITIONS_KEY, -100); // always at the beginning
        map.put(CustomIngredientImpl.TYPE_KEY, 0); // mimic vanilla "type"
    }
}
