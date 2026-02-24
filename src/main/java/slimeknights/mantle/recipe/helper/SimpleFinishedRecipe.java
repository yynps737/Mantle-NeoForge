package slimeknights.mantle.recipe.helper;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Helper to save a simple recipe with no advancement to a RecipeOutput.
 * Replaces the old FinishedRecipe-based SimpleFinishedRecipe.
 */
public class SimpleFinishedRecipe {
  /**
   * Saves a recipe with the given serializer that has no special data or advancement.
   * Useful for {@link SimpleRecipeSerializer} based recipes.
   * @param output     Recipe output to save to
   * @param id         Recipe ID
   * @param recipe     The recipe instance to save
   */
  public static void save(RecipeOutput output, ResourceLocation id, Recipe<?> recipe) {
    output.accept(id, recipe, null);
  }
}
