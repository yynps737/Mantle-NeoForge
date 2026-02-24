package slimeknights.mantle.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Recipe that has an output other than an {@link ItemStack}
 * @param <T>  Recipe input type
 */
public interface ICustomOutputRecipe<T extends RecipeInput> extends ICommonRecipe<T> {
  /** @deprecated Item stack output not supported */
  @Override
  @Deprecated
  default ItemStack getResultItem(HolderLookup.Provider registries) {
    return ItemStack.EMPTY;
  }

  /** @deprecated Item stack output not supported */
  @Override
  @Deprecated
  default ItemStack assemble(T input, HolderLookup.Provider registries) {
    return ItemStack.EMPTY;
  }
}
