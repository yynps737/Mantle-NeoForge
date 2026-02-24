package slimeknights.mantle.recipe.helper;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Recipe serializer that logs network exceptions before throwing them.
 * In 1.21.1+, logging is implemented directly in codec()/streamCodec() implementations.
 * @param <T>  Recipe class
 */
public interface LoggingRecipeSerializer<T extends Recipe<?>> extends RecipeSerializer<T> {}
