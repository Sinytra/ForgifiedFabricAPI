package net.fabricmc.fabric.test.resource.conditions;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ConditionalResourcesTest.MOD_ID)
public class ConditionalResourcesGameTest {

    private static ResourceLocation id(String path) {
        return new ResourceLocation(ConditionalResourcesTest.MOD_ID, path);
    }

    @GameTest(templateNamespace = ConditionalResourcesTest.MOD_ID, template = "empty")
	@PrefixGameTestTemplate(false)
    public void conditionalRecipes(GameTestHelper context) {
        RecipeManager manager = context.getLevel().getRecipeManager();

        if (manager.byKey(id("not_loaded")).isPresent()) {
            throw new AssertionError("not_loaded recipe should not have been loaded.");
        }

        if (manager.byKey(id("loaded")).isEmpty()) {
            throw new AssertionError("loaded recipe should have been loaded.");
        }

        if (manager.byKey(id("item_tags_populated")).isEmpty()) {
            throw new AssertionError("item_tags_populated recipe should have been loaded.");
        }

        if (manager.byKey(id("tags_populated")).isEmpty()) {
            throw new AssertionError("tags_populated recipe should have been loaded.");
        }

        if (manager.byKey(id("tags_populated_default")).isEmpty()) {
            throw new AssertionError("tags_populated_default recipe should have been loaded.");
        }

        if (manager.byKey(id("tags_not_populated")).isPresent()) {
            throw new AssertionError("tags_not_populated recipe should not have been loaded.");
        }

        if (manager.byKey(id("features_enabled")).isEmpty()) {
            throw new AssertionError("features_enabled recipe should have been loaded.");
        }

        long loadedRecipes = manager.getRecipes().stream().filter(r -> r.getId().getNamespace().equals(ConditionalResourcesTest.MOD_ID)).count();
        if (loadedRecipes != 5) throw new AssertionError("Unexpected loaded recipe count: " + loadedRecipes);

        context.succeed();
    }
}
