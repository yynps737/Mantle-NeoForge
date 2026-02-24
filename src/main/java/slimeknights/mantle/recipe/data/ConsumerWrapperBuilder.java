package slimeknights.mantle.recipe.data;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a recipe output wrapper which adds conditions to recipes.
 * In 1.21.1, this is implemented via {@link RecipeOutput#withConditions(ICondition...)}.
 */
@SuppressWarnings("unused")  // API
public class ConsumerWrapperBuilder {
  private final List<ICondition> conditions = new ArrayList<>();

  private ConsumerWrapperBuilder() {}

  /**
   * Creates a wrapper builder
   * @return Default builder
   */
  public static ConsumerWrapperBuilder wrap() {
    return new ConsumerWrapperBuilder();
  }

  /**
   * Adds a conditional to the consumer
   * @param condition Condition to add
   * @return Added condition
   */
  @CanIgnoreReturnValue
  public ConsumerWrapperBuilder addCondition(ICondition condition) {
    conditions.add(condition);
    return this;
  }

  /**
   * Builds the wrapped recipe output with the added conditions
   * @param output Base recipe output
   * @return Wrapped recipe output with conditions
   */
  public RecipeOutput build(RecipeOutput output) {
    if (conditions.isEmpty()) {
      return output;
    }
    return output.withConditions(conditions.toArray(new ICondition[0]));
  }
}
