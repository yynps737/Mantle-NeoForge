package slimeknights.mantle.recipe.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.util.RetexturedHelper;

@SuppressWarnings("WeakerAccess")
public class ShapedRetexturedRecipe extends ShapedRecipe {
  @Getter
  private final Ingredient texture;
  private final boolean matchAll;

  protected ShapedRetexturedRecipe(ShapedRecipe orig, Ingredient texture, boolean matchAll) {
    super(orig.getGroup(), orig.category(), orig.pattern, orig.result, orig.showNotification());
    this.texture = texture;
    this.matchAll = matchAll;
  }

  public ItemStack getResultItem(Item texture, HolderLookup.Provider registries) {
    return RetexturedHelper.setTexture(getResultItem(registries).copy(), Block.byItem(texture));
  }

  @Override
  public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
    ItemStack result = super.assemble(input, registries);
    Block currentTexture = null;
    for (int i = 0; i < input.size(); i++) {
      ItemStack stack = input.getItem(i);
      if (!stack.isEmpty() && texture.test(stack)) {
        Block block = RetexturedHelper.getTexture(stack);
        if (block == Blocks.AIR && stack.getItem() != result.getItem()) {
          block = Block.byItem(stack.getItem());
        }
        if (block == Blocks.AIR) {
          continue;
        }
        if (currentTexture == null) {
          currentTexture = block;
          if (!matchAll) {
            break;
          }
        } else if (currentTexture != block) {
          currentTexture = null;
          break;
        }
      }
    }
    if (currentTexture != null) {
      return RetexturedHelper.setTexture(result, currentTexture);
    }
    return result;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return MantleRecipes.CRAFTING_SHAPED_RETEXTURED.get();
  }

  public static class Serializer implements RecipeSerializer<ShapedRetexturedRecipe> {
    public static final MapCodec<ShapedRetexturedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      ShapedRecipe.Serializer.CODEC.forGetter(r -> (ShapedRecipe) r),
      Ingredient.CODEC.fieldOf("texture").forGetter(ShapedRetexturedRecipe::getTexture),
      Codec.BOOL.optionalFieldOf("match_all", false).forGetter(r -> r.matchAll)
    ).apply(instance, ShapedRetexturedRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRetexturedRecipe> STREAM_CODEC = StreamCodec.composite(
      ShapedRecipe.Serializer.STREAM_CODEC, r -> (ShapedRecipe) r,
      Ingredient.CONTENTS_STREAM_CODEC, ShapedRetexturedRecipe::getTexture,
      ByteBufCodecs.BOOL, r -> r.matchAll,
      ShapedRetexturedRecipe::new
    );

    @Override
    public MapCodec<ShapedRetexturedRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ShapedRetexturedRecipe> streamCodec() {
      return STREAM_CODEC;
    }
  }
}
