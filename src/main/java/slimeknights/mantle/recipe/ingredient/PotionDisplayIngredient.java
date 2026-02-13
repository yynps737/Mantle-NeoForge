package slimeknights.mantle.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.LoadableMapCodecWrapper;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.registration.MantleIngredientTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

/** Ingredient that shows all potion variants on the displayed item list */
public class PotionDisplayIngredient extends ItemIngredient {
  /** Loadable for serialization - preserving Mantle's loadable system */
  private static final RecordLoadable<PotionDisplayIngredient> LOADABLE = RecordLoadable.create(
    ItemsField.INSTANCE,
    TAG_FIELD,
    PotionDisplayIngredient::new
  );

  /** MapCodec for NeoForge 1.21.1+ ingredient system */
  public static final MapCodec<PotionDisplayIngredient> CODEC = new LoadableMapCodecWrapper<>(LOADABLE);

  /** StreamCodec for network synchronization */
  public static final StreamCodec<RegistryFriendlyByteBuf, PotionDisplayIngredient> STREAM_CODEC =
    StreamCodec.of(
      (buf, ingredient) -> LOADABLE.encode(buf, ingredient),
      buf -> LOADABLE.decode(buf)
    );

  /** IngredientType for registration */
  public static final IngredientType<PotionDisplayIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  protected PotionDisplayIngredient(List<Item> items, @Nullable TagKey<Item> tag) {
    super(items, tag);
  }

  /** Creates a ingredient matching a list of items */
  public static PotionDisplayIngredient of(List<ItemLike> items) {
    return new PotionDisplayIngredient(toItem(items), null);
  }

  /** Creates a ingredient matching a list of items */
  public static PotionDisplayIngredient of(ItemLike... items) {
    return of(List.of(items));
  }

  /** Creates a ingredient matching a tag */
  public static PotionDisplayIngredient of(TagKey<Item> tag) {
    return new PotionDisplayIngredient(List.of(), tag);
  }

  @Override
  public Stream<ItemStack> getItems() {
    // Get base items from parent
    Stream<ItemStack> baseItems = super.getItems();

    // For each base item, create variants with all registered potions using Holder<Potion>
    return baseItems.flatMap(baseStack ->
      BuiltInRegistries.POTION.holders()
        .map((Holder<Potion> potionHolder) -> {
          ItemStack stack = baseStack.copy();
          stack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, new PotionContents(potionHolder));
          return stack;
        })
    );
  }

  @Override
  public boolean isSimple() {
    // Simple because we don't check potion data when testing
    return true;
  }

  @Override
  public IngredientType<?> getType() {
    return MantleIngredientTypes.POTION_DISPLAY.get();
  }
}
