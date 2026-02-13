package slimeknights.mantle.recipe.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Ingredient for a NBT sensitive item from another mod, should never be used outside datagen
 * @deprecated StrictNBTIngredient was removed in NeoForge 1.21.1. Use DataComponentIngredient instead.
 */
@Deprecated(forRemoval = true)
public class NBTNameIngredient extends Ingredient {
  private final ResourceLocation name;
  @Nullable
  private final CompoundTag nbt;

  protected NBTNameIngredient(ResourceLocation name, @Nullable CompoundTag nbt) {
    super(Stream.empty());
    this.name = name;
    this.nbt = nbt;
  }

  /**
   * Creates an ingredient for the given name and NBT
   * @param name  Item name
   * @param nbt   NBT
   * @return  Ingredient
   */
  public static NBTNameIngredient from(ResourceLocation name, CompoundTag nbt) {
    return new NBTNameIngredient(name, nbt);
  }

  /**
   * Creates an ingredient for an item that must have no NBT
   * @param name  Item name
   * @return  Ingredient
   */
  public static NBTNameIngredient from(ResourceLocation name) {
    return new NBTNameIngredient(name, null);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    throw new UnsupportedOperationException();
  }

  @Override
  public JsonElement toJson() {
    throw new UnsupportedOperationException("NBTNameIngredient is deprecated. StrictNBTIngredient was removed in NeoForge 1.21.1. Use DataComponentIngredient instead.");
  }
}
