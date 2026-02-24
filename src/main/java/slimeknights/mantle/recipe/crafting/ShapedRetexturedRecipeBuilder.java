package slimeknights.mantle.recipe.crafting;

import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "fromShaped")
public class ShapedRetexturedRecipeBuilder {
  private final ShapedRecipeBuilder parent;
  private Ingredient texture = null;
  private char textureKey = '\0';
  private boolean matchAll = false;

  /**
   * Sets the texture source to the given ingredient
   * @param texture Ingredient to use for texture
   * @return Builder instance
   */
  public ShapedRetexturedRecipeBuilder setSource(Ingredient texture) {
    this.texture = texture;
    this.textureKey = '\0';
    return this;
  }

  /**
   * Sets the texture source to the given tag
   * @param tag Tag to use for texture
   * @return Builder instance
   */
  public ShapedRetexturedRecipeBuilder setSource(TagKey<Item> tag) {
    return setSource(Ingredient.of(tag));
  }

  /** Sets the texture source to a key from the recipe's key map */
  public ShapedRetexturedRecipeBuilder setSource(char textureKey) {
    this.textureKey = textureKey;
    this.texture = null;
    return this;
  }

  /**
   * Sets the match first property on the recipe.
   * If set, the recipe uses the first ingredient match for the texture. If unset, all items that match the ingredient must be the same or no texture is applied
   * @return Builder instance
   */
  public ShapedRetexturedRecipeBuilder setMatchAll() {
    this.matchAll = true;
    return this;
  }

  /**
   * Builds the recipe with the default name using the given consumer
   * @param output Recipe output
   */
  public void build(RecipeOutput output) {
    this.validate();
    parent.save(wrapOutput(output));
  }

  /**
   * Builds the recipe using the given consumer
   * @param output   Recipe output
   * @param location Recipe location
   */
  public void build(RecipeOutput output, ResourceLocation location) {
    this.validate();
    parent.save(wrapOutput(output), location);
  }

  /**
   * Ensures this recipe can be built
   * @throws IllegalStateException If the recipe cannot be built
   */
  private void validate() {
    if (texture == null && textureKey == '\0') {
      throw new IllegalStateException("No texture defined for texture recipe");
    }
  }

  /** Resolves the texture ingredient from either the direct ingredient or the key map */
  private Ingredient resolveTexture() {
    if (texture != null) {
      return texture;
    }
    // resolve from the parent builder's key map (accessible via AT)
    Ingredient resolved = parent.key.get(textureKey);
    if (resolved == null) {
      throw new IllegalStateException("Texture key '" + textureKey + "' not found in recipe key map");
    }
    return resolved;
  }

  /** Creates a RecipeOutput wrapper that intercepts the shaped recipe and wraps it as a retextured recipe */
  private RecipeOutput wrapOutput(RecipeOutput original) {
    Ingredient resolvedTexture = resolveTexture();
    boolean all = this.matchAll;
    return new RecipeOutput() {
      @Override
      public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        original.accept(id, new ShapedRetexturedRecipe((ShapedRecipe) recipe, resolvedTexture, all), advancement, conditions);
      }

      @Override
      public Advancement.Builder advancement() {
        return original.advancement();
      }
    };
  }
}
