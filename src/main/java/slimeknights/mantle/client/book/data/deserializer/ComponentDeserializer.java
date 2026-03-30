package slimeknights.mantle.client.book.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.lang.reflect.Type;

/**
 * Deserializer for {@link Component} that handles both string literals and full JSON component objects.
 * Strings are converted to literal text components, while objects are parsed using the component codec.
 */
public class ComponentDeserializer implements JsonDeserializer<Component> {
  @Override
  public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    if (json == null || json.isJsonNull()) {
      return null;
    }
    return ComponentSerialization.FLAT_CODEC.parse(JsonOps.INSTANCE, json)
      .getOrThrow(msg -> new JsonParseException("Failed to parse Component: " + msg));
  }
}
