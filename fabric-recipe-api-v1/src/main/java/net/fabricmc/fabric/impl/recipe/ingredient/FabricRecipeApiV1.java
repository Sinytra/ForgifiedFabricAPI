package net.fabricmc.fabric.impl.recipe.ingredient;

import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.sinytra.fabric.recipe_api.generated.GeneratedEntryPoint;

import net.minecraft.world.item.crafting.Ingredient;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.fabric.impl.recipe.ingredient.compat.NeoCustomIngredientWrapper;
import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl;
import net.fabricmc.fabric.impl.recipe.sync.ServerboundSupportedRecipeSerializersPayload;

@Mod(GeneratedEntryPoint.MOD_ID)
public class FabricRecipeApiV1 {
	private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, GeneratedEntryPoint.MOD_ID);

	public static final DeferredHolder<IngredientType<?>, IngredientType<NeoCustomIngredientWrapper>> FABRIC_INGREDIENT_WRAPPER = INGREDIENT_TYPES.register("fabric_wrapper", () -> new IngredientType<>(NeoCustomIngredientWrapper.CODEC, NeoCustomIngredientWrapper.STREAM_CODEC));

	public FabricRecipeApiV1(IEventBus bus) {
		INGREDIENT_TYPES.register(bus);
		
		bus.addListener(RegisterPayloadHandlersEvent.class, event -> {
			PayloadRegistrar registrar = event.registrar("1").optional();

			registrar.configurationToServer(
					ServerboundSupportedRecipeSerializersPayload.TYPE,
					ServerboundSupportedRecipeSerializersPayload.CODEC,
					RecipeSyncImpl::onRecipeSyncRequest
			);
		});
	}

	public static Codec<Ingredient> makeIngredientMapCodec(Codec<Ingredient> original) {
		var customIngredientCodec = CustomIngredientImpl.CODEC.<CustomIngredient>dispatch(
				CustomIngredientImpl.TYPE_KEY, CustomIngredient::getSerializer, CustomIngredientSerializer::getCodec);
		return Codec.xor(customIngredientCodec, original)
				.xmap(
						e -> e.map(c -> new NeoCustomIngredientWrapper(c).toVanilla(), Function.identity()),
						s -> s.getCustomIngredient() instanceof NeoCustomIngredientWrapper wrapper ? Either.left(wrapper.ingredient()) : Either.right(s)
				);
	}
	
	static {
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(Event.DEFAULT_PHASE, RecipeSynchronization.RECIPE_SYNC_EVENT_PHASE);
	}
}
