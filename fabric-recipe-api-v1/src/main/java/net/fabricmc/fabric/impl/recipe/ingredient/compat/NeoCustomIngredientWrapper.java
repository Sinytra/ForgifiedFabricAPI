package net.fabricmc.fabric.impl.recipe.ingredient.compat;

import java.util.Objects;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.fabricmc.fabric.impl.recipe.ingredient.FabricRecipeApiV1;

public record NeoCustomIngredientWrapper(CustomIngredient ingredient) implements ICustomIngredient {
    public static final StreamCodec<RegistryFriendlyByteBuf, CustomIngredient> CUSTOM_INGREDIENT_SERIALIZER_STREAM_CODEC = Identifier.STREAM_CODEC
        .<RegistryFriendlyByteBuf>cast()
        .dispatch(i -> i.getSerializer().getIdentifier(), l -> Objects.requireNonNull(CustomIngredientSerializer.get(l)).getStreamCodec());
    public static final StreamCodec<RegistryFriendlyByteBuf, NeoCustomIngredientWrapper> STREAM_CODEC = StreamCodec.composite(
        CUSTOM_INGREDIENT_SERIALIZER_STREAM_CODEC,
        w -> w.ingredient,
        NeoCustomIngredientWrapper::new
    );
    public static final Codec<CustomIngredient> CUSTOM_INGREDIENT_CODEC = CustomIngredientImpl.CODEC.dispatch(CustomIngredient::getSerializer, CustomIngredientSerializer::getCodec);
    public static final MapCodec<NeoCustomIngredientWrapper> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CUSTOM_INGREDIENT_CODEC.fieldOf("ingredient").forGetter(w -> w.ingredient)
    ).apply(instance, NeoCustomIngredientWrapper::new));

    @Override
    public boolean test(ItemStack arg) {
        return this.ingredient.test(arg);
    }

    @Override
    public boolean isSimple() {
        return !this.ingredient.requiresTesting();
    }

	@Override
	public Stream<Holder<Item>> items() {
		return this.ingredient.items();
	}

	@Override
    public IngredientType<?> getType() {
        return FabricRecipeApiV1.FABRIC_INGREDIENT_WRAPPER.get();
    }
}
