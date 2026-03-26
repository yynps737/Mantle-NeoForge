package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.mapping.EnumMapLoadable;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.Map;

/** Special loadable for display contexts due to {@link ItemDisplayContext} being an extensible enum */
public enum DisplayContextLoadable implements ResourceLocationLoadable<ItemDisplayContext> {
  INSTANCE;

  @Override
  public ItemDisplayContext fromKey(ResourceLocation name, String key, TypedMap context) {
    // ItemDisplayContext is a StringRepresentable enum, look up by serialized name.
    // Vanilla values use just the path (e.g. "gui"), custom extensible enum values use "namespace:path" (e.g. "tconstruct:table").
    String path = name.getPath();
    String full = name.toString();
    for (ItemDisplayContext value : ItemDisplayContext.values()) {
      String serialized = value.getSerializedName();
      if (serialized.equals(path) || serialized.equals(full)) {
        return value;
      }
    }
    throw new JsonSyntaxException("Unable to parse " + key + " as the ItemDisplayContext does not contain ID " + name);
  }

  @Override
  public ResourceLocation getKey(ItemDisplayContext object) {
    String serialized = object.getSerializedName();
    // Vanilla values have plain names like "gui"; custom extensible enum values use "namespace:path".
    if (serialized.contains(":")) {
      return ResourceLocation.parse(serialized);
    }
    return ResourceLocation.withDefaultNamespace(serialized);
  }

  @Override
  public ItemDisplayContext decode(FriendlyByteBuf buffer, TypedMap context) {
    return ItemDisplayContext.BY_ID.apply(buffer.readByte());
  }

  @Override
  public void encode(FriendlyByteBuf buffer, ItemDisplayContext value) {
    buffer.writeByte(value.getId());
  }

  @Override
  public <V> Loadable<Map<ItemDisplayContext,V>> mapWithValues(Loadable<V> valueLoadable, int minSize) {
    return new EnumMapLoadable<>(ItemDisplayContext.class, this, valueLoadable, minSize);
  }
}
