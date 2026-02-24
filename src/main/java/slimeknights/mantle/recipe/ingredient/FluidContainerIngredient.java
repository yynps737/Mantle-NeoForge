package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
// ItemHandlerHelper.copyStackWithSize removed in 1.21.1, use stack.copyWithCount() instead
import slimeknights.mantle.Mantle;
import slimeknights.mantle.registration.MantleIngredientTypes;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/** Ingredient that matches a container of fluid */
@SuppressWarnings("unused")  // API
public class FluidContainerIngredient implements ICustomIngredient {
  public static final ResourceLocation ID = Mantle.getResource("fluid_container");

  /** Custom MapCodec for complex serialization logic */
  public static final MapCodec<FluidContainerIngredient> CODEC = new MapCodec<>() {
    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
      return Stream.of(ops.createString("fluid"), ops.createString("display"));
    }

    @Override
    public <T> DataResult<FluidContainerIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
      try {
        // Convert to JsonObject for parsing
        JsonObject json = new JsonObject();
        input.entries().forEach(pair -> {
          String key = ops.getStringValue(pair.getFirst()).result().orElse(null);
          if (key != null && !key.equals("neoforge:ingredient_type") && !key.equals("type")) {
            JsonElement element = ops.convertTo(JsonOps.INSTANCE, pair.getSecond());
            json.add(key, element);
          }
        });

        // Parse fluid ingredient
        FluidIngredient fluidIngredient;
        if (json.has("fluid") && !json.get("fluid").isJsonPrimitive()) {
          fluidIngredient = FluidIngredient.LOADABLE.getIfPresent(json, "fluid");
        } else {
          fluidIngredient = FluidIngredient.LOADABLE.convert(json, "fluid");
        }

        // Parse optional display using Codec (fromJson removed in 1.21.1)
        Ingredient display = null;
        if (json.has("display")) {
          display = Ingredient.CODEC.parse(JsonOps.INSTANCE, JsonHelper.getElement(json, "display"))
            .getOrThrow(msg -> new RuntimeException("Failed to parse display ingredient: " + msg));
        }

        return DataResult.success(new FluidContainerIngredient(fluidIngredient, display));
      } catch (Exception e) {
        return DataResult.error(() -> "Failed to decode FluidContainerIngredient: " + e.getMessage());
      }
    }

    @Override
    public <T> RecordBuilder<T> encode(FluidContainerIngredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
      try {
        // Serialize fluid ingredient
        JsonElement fluidElement = input.fluidIngredient.serialize();
        if (fluidElement.isJsonObject()) {
          JsonObject fluidObj = fluidElement.getAsJsonObject();
          for (var entry : fluidObj.entrySet()) {
            T value = JsonOps.INSTANCE.convertTo(ops, entry.getValue());
            prefix.add(entry.getKey(), value);
          }
        } else {
          T fluidValue = JsonOps.INSTANCE.convertTo(ops, fluidElement);
          prefix.add("fluid", fluidValue);
        }

        // Serialize optional display using Codec (toJson removed in 1.21.1)
        if (input.display != null) {
          JsonElement displayElement = Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, input.display)
            .getOrThrow(msg -> new RuntimeException("Failed to encode display ingredient: " + msg));
          T displayValue = JsonOps.INSTANCE.convertTo(ops, displayElement);
          prefix.add("display", displayValue);
        }

        return prefix;
      } catch (Exception e) {
        return prefix.withErrorsFrom(DataResult.error(() -> "Failed to encode FluidContainerIngredient: " + e.getMessage()));
      }
    }
  };

  /** StreamCodec for network synchronization */
  public static final StreamCodec<RegistryFriendlyByteBuf, FluidContainerIngredient> STREAM_CODEC =
    StreamCodec.of(
      (buf, ingredient) -> {
        FluidIngredient.LOADABLE.encode(buf, ingredient.fluidIngredient);
        if (ingredient.display != null) {
          buf.writeBoolean(true);
          Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient.display);
        } else {
          buf.writeBoolean(false);
        }
      },
      buf -> {
        FluidIngredient fluidIngredient = FluidIngredient.LOADABLE.decode(buf);
        Ingredient display = null;
        if (buf.readBoolean()) {
          display = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        }
        return new FluidContainerIngredient(fluidIngredient, display);
      }
    );

  /** IngredientType for registration */
  public static final IngredientType<FluidContainerIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  /** Ingredient to use for matching */
  private final FluidIngredient fluidIngredient;
  /** Internal ingredient to display the ingredient recipe viewers */
  @Nullable
  private final Ingredient display;

  protected FluidContainerIngredient(FluidIngredient fluidIngredient, @Nullable Ingredient display) {
    this.fluidIngredient = fluidIngredient;
    this.display = display;
  }

  /** Creates an instance from a fluid ingredient with a display container */
  public static FluidContainerIngredient fromIngredient(FluidIngredient ingredient, Ingredient display) {
    return new FluidContainerIngredient(ingredient, display);
  }

  /** Creates an instance from a fluid ingredient with no display, not recommended */
  public static FluidContainerIngredient fromIngredient(FluidIngredient ingredient) {
    return new FluidContainerIngredient(ingredient, null);
  }

  /** Creates an instance from a fluid ingredient with a display container */
  public static FluidContainerIngredient fromFluid(FluidObject<?> fluid) {
    return fromIngredient(fluid.ingredient(FluidType.BUCKET_VOLUME), Ingredient.of(fluid));
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null || stack.isEmpty()) return false;
    // first, must have a fluid capability
    var cap = stack.getCapability(Capabilities.FluidHandler.ITEM);
    if (cap == null || cap.getTanks() != 1) return false;

    // second, must contain enough fluid
    FluidStack contained = cap.getFluidInTank(0);
    if (contained.isEmpty() || !fluidIngredient.test(contained.getFluid())
        || fluidIngredient.getAmount(contained.getFluid()) != contained.getAmount()) {
      return false;
    }

    // so far so good, from this point on we are forced to make copies as we need to try draining, so copy and fetch the copy's cap
    ItemStack copy = stack.copyWithCount(1);
    var copyCap = copy.getCapability(Capabilities.FluidHandler.ITEM);
    if (copyCap == null) return false;

    // alright, we know it has the fluid, the question is just whether draining the fluid will give us the desired result
    Fluid fluid = copyCap.getFluidInTank(0).getFluid();
    int amount = fluidIngredient.getAmount(fluid);
    FluidStack drained = copyCap.drain(amount, FluidAction.EXECUTE);
    // we need an exact match, and we need the resulting container item to be the same as the item stack's container item
    return drained.getFluid() == fluid && drained.getAmount() == amount
        && ItemStack.matches(stack.getCraftingRemainingItem(), copyCap.getContainer());
  }

  @Override
  public Stream<ItemStack> getItems() {
    // no container? unfortunately hard to display this recipe so show nothing
    if (display == null) {
      return Stream.empty();
    }
    return Stream.of(display.getItems());
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return MantleIngredientTypes.FLUID_CONTAINER.get();
  }
}
