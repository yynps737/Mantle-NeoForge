package slimeknights.mantle.recipe.helper;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

/** Simple implementation of a recipe serializer with no properties. */
public record SimpleRecipeSerializer<T extends Recipe<?>>(Supplier<T> factory) implements RecipeSerializer<T> {
  @Override
  public MapCodec<T> codec() {
    return MapCodec.unit(factory);
  }

  @Override
  public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
    return StreamCodec.of((buf, recipe) -> {}, buf -> factory.get());
  }
}
