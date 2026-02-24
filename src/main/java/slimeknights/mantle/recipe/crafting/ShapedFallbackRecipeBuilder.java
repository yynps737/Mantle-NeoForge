package slimeknights.mantle.recipe.crafting;

import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Builder for a shaped recipe with fallbacks */
@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "fallback")
public class ShapedFallbackRecipeBuilder {
  private final ShapedRecipeBuilder base;
  private final List<ResourceLocation> alternatives = new ArrayList<>();

  /**
   * Adds a single alternative to this recipe. Any matching alternative causes this recipe to fail
   * @param location  Alternative
   * @return  Builder instance
   */
  public ShapedFallbackRecipeBuilder addAlternative(ResourceLocation location) {
    this.alternatives.add(location);
    return this;
  }

  /**
   * Adds a list of alternatives to this recipe. Any matching alternative causes this recipe to fail
   * @param locations  Alternative list
   * @return  Builder instance
   */
  public ShapedFallbackRecipeBuilder addAlternatives(Collection<ResourceLocation> locations) {
    this.alternatives.addAll(locations);
    return this;
  }

  /**
   * Builds the recipe using the output as the name
   * @param output  Recipe output
   */
  public void build(RecipeOutput output) {
    base.save(wrapOutput(output));
  }

  /**
   * Builds the recipe using the given ID
   * @param output  Recipe output
   * @param id      Recipe ID
   */
  public void build(RecipeOutput output, ResourceLocation id) {
    base.save(wrapOutput(output), id);
  }

  /** Creates a RecipeOutput wrapper that intercepts the shaped recipe and wraps it as a fallback recipe */
  private RecipeOutput wrapOutput(RecipeOutput original) {
    List<ResourceLocation> alts = this.alternatives;
    return new RecipeOutput() {
      @Override
      public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        original.accept(id, new ShapedFallbackRecipe((ShapedRecipe) recipe, alts), advancement, conditions);
      }

      @Override
      public Advancement.Builder advancement() {
        return original.advancement();
      }
    };
  }
}
