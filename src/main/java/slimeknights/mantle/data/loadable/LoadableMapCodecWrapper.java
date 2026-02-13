package slimeknights.mantle.data.loadable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.stream.Stream;

/**
 * Simplified wrapper to bridge Mantle's Loadable system with Mojang's MapCodec for NeoForge 1.21.1+
 * This preserves Mantle's existing Loadable infrastructure while satisfying NeoForge's ingredient type requirements.
 */
public class LoadableMapCodecWrapper<T> extends MapCodec<T> {
  private final RecordLoadable<T> loadable;

  public LoadableMapCodecWrapper(RecordLoadable<T> loadable) {
    this.loadable = loadable;
  }

  @Override
  public <O> Stream<O> keys(DynamicOps<O> ops) {
    // Return empty - loadable handles field management internally
    return Stream.empty();
  }

  @Override
  public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
    try {
      // Convert to JsonObject using JsonOps as intermediate
      JsonObject json = new JsonObject();

      input.entries().forEach(pair -> {
        String key = ops.getStringValue(pair.getFirst()).result().orElse(null);
        if (key != null && !key.equals("neoforge:ingredient_type") && !key.equals("type")) {
          // Convert value to JsonElement via JsonOps
          JsonElement element = ops.convertTo(JsonOps.INSTANCE, pair.getSecond());
          json.add(key, element);
        }
      });

      // Use loadable to deserialize
      T result = loadable.deserialize(json);
      return DataResult.success(result);
    } catch (Exception e) {
      return DataResult.error(() -> "Failed to decode via Loadable: " + e.getMessage());
    }
  }

  @Override
  public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
    try {
      JsonObject json = new JsonObject();
      loadable.serialize(input, json);

      // Convert each JsonElement back to target ops
      for (var entry : json.entrySet()) {
        O value = JsonOps.INSTANCE.convertTo(ops, entry.getValue());
        prefix.add(entry.getKey(), value);
      }

      return prefix;
    } catch (Exception e) {
      return prefix.withErrorsFrom(DataResult.error(() -> "Failed to encode via Loadable: " + e.getMessage()));
    }
  }
}
