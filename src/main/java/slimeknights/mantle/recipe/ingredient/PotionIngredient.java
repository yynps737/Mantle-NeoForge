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
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.LoadableMapCodecWrapper;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.registration.MantleIngredientTypes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/** Simple ingredient checking for an item with a specific potion */
public class PotionIngredient extends ItemIngredient {
  /** Loadable for serialization - preserving Mantle's loadable system */
  private static final RecordLoadable<PotionIngredient> LOADABLE = RecordLoadable.create(
    ItemsField.INSTANCE,
    TAG_FIELD,
    Loadables.POTION.defaultField("potion", Potions.EMPTY, false, i -> i.potion),
    PotionIngredient::new
  );

  /** MapCodec for NeoForge 1.21.1+ ingredient system */
  public static final MapCodec<PotionIngredient> CODEC = new LoadableMapCodecWrapper<>(LOADABLE);

  /** StreamCodec for network synchronization */
  public static final StreamCodec<RegistryFriendlyByteBuf, PotionIngredient> STREAM_CODEC =
    StreamCodec.of(
      (buf, ingredient) -> LOADABLE.encode(buf, ingredient),
      buf -> LOADABLE.decode(buf)
    );

  /** IngredientType for registration */
  public static final IngredientType<PotionIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  private final Potion potion;

  protected PotionIngredient(List<Item> items, @Nullable TagKey<Item> itemTag, Potion potion) {
    super(items, itemTag);
    this.potion = potion;
  }

  /** Creates a potion ingredient matching a list of items */
  public static PotionIngredient of(Potion potion, List<ItemLike> items) {
    return new PotionIngredient(toItem(items), null, potion);
  }

  /** Creates a potion ingredient matching a list of items */
  public static PotionIngredient of(Potion potion, ItemLike... items) {
    return of(potion, Arrays.asList(items));
  }

  /** Creates a potion ingredient matching a tag */
  public static PotionIngredient of(Potion potion, TagKey<Item> tag) {
    return new PotionIngredient(List.of(), tag, potion);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    // stack must match item/tag, and potion must match
    if (stack == null || !super.test(stack)) {
      return false;
    }
    // Check potion using PotionContents component
    PotionContents contents = stack.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    return contents.potion().isPresent() && contents.potion().get() == potion;
  }

  @Override
  public Stream<ItemStack> getItems() {
    // Get base items and apply potion to each
    Stream<ItemStack> itemStacks = items.stream()
      .map(item -> {
        ItemStack stack = new ItemStack(item);
        stack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return stack;
      });

    if (tag != null) {
      // Add items from tag with potion applied
      Stream<ItemStack> tagStacks = BuiltInRegistries.ITEM.getTag(tag)
        .stream()
        .flatMap(named -> named.stream())
        .map(Holder::value)
        .map(item -> {
          ItemStack stack = new ItemStack(item);
          stack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, new PotionContents(potion));
          return stack;
        });
      itemStacks = Stream.concat(itemStacks, tagStacks);
    }

    return itemStacks;
  }

  @Override
  public boolean isSimple() {
    // Returns false because we check potion data components
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return MantleIngredientTypes.POTION.get();
  }
}
