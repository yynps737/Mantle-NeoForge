package slimeknights.mantle.recipe.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.MantleRecipes;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class ShapedFallbackRecipe extends ShapedRecipe {

  /** Recipes to skip if they match */
  private final List<ResourceLocation> alternatives;
  private List<CraftingRecipe> alternativeCache;

  public ShapedFallbackRecipe(ShapedRecipe base, List<ResourceLocation> alternatives) {
    super(base.getGroup(), base.category(), base.pattern, base.result, base.showNotification());
    this.alternatives = alternatives;
  }

  @Override
  public boolean matches(CraftingInput input, Level world) {
    if (!super.matches(input, world)) {
      return false;
    }

    if (alternativeCache == null) {
      RecipeManager manager = world.getRecipeManager();
      alternativeCache = alternatives.stream()
                                     .map(manager::byKey)
                                     .filter(java.util.Optional::isPresent)
                                     .map(java.util.Optional::get)
                                     .map(RecipeHolder::value)
                                     .filter(recipe -> {
                                       Class<?> clazz = recipe.getClass();
                                       return clazz == ShapedRecipe.class || clazz == ShapelessRecipe.class;
                                     })
                                     .map(recipe -> (CraftingRecipe) recipe)
                                     .collect(Collectors.toList());
    }
    return this.alternativeCache.stream().noneMatch(recipe -> recipe.matches(input, world));
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return MantleRecipes.CRAFTING_SHAPED_FALLBACK.get();
  }

  public static class Serializer implements RecipeSerializer<ShapedFallbackRecipe> {
    public static final MapCodec<ShapedFallbackRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      ShapedRecipe.Serializer.CODEC.forGetter(r -> r),
      ResourceLocation.CODEC.listOf().fieldOf("alternatives").forGetter(r -> r.alternatives)
    ).apply(instance, ShapedFallbackRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedFallbackRecipe> STREAM_CODEC = StreamCodec.composite(
      ShapedRecipe.Serializer.STREAM_CODEC, r -> (ShapedRecipe) r,
      ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.alternatives,
      ShapedFallbackRecipe::new
    );

    @Override
    public MapCodec<ShapedFallbackRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ShapedFallbackRecipe> streamCodec() {
      return STREAM_CODEC;
    }
  }
}
