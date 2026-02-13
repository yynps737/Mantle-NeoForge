package slimeknights.mantle.registration;

import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.ingredient.FluidContainerIngredient;
import slimeknights.mantle.recipe.ingredient.PotionDisplayIngredient;
import slimeknights.mantle.recipe.ingredient.PotionIngredient;

/**
 * Registration for Mantle's custom ingredient types for NeoForge 1.21.1+
 */
public class MantleIngredientTypes {
  /** Deferred register for ingredient types */
  public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
    DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPE, Mantle.modId);

  // Ingredient type registrations
  public static final DeferredHolder<IngredientType<?>, IngredientType<PotionIngredient>> POTION =
    INGREDIENT_TYPES.register("potion", () -> PotionIngredient.TYPE);

  public static final DeferredHolder<IngredientType<?>, IngredientType<PotionDisplayIngredient>> POTION_DISPLAY =
    INGREDIENT_TYPES.register("potion_display", () -> PotionDisplayIngredient.TYPE);

  public static final DeferredHolder<IngredientType<?>, IngredientType<FluidContainerIngredient>> FLUID_CONTAINER =
    INGREDIENT_TYPES.register("fluid_container", () -> FluidContainerIngredient.TYPE);
}
