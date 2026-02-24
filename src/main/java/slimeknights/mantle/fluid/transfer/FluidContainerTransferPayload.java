package slimeknights.mantle.fluid.transfer;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Payload to sync fluid container transfer
 */
public record FluidContainerTransferPayload(Set<Item> items) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<FluidContainerTransferPayload> TYPE =
    new CustomPacketPayload.Type<>(Mantle.getResource("fluid_container_transfer"));

  // Stream codec for Item using registry ID
  private static final StreamCodec<RegistryFriendlyByteBuf, Item> ITEM_STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM);

  // Stream codec for List<Item>
  private static final StreamCodec<RegistryFriendlyByteBuf, List<Item>> ITEM_LIST_CODEC =
    ITEM_STREAM_CODEC.apply(ByteBufCodecs.list());

  // Stream codec for the payload - converts between List and Set
  public static final StreamCodec<RegistryFriendlyByteBuf, FluidContainerTransferPayload> STREAM_CODEC =
    ITEM_LIST_CODEC.map(
      list -> new FluidContainerTransferPayload(new HashSet<>(list)),
      payload -> List.copyOf(payload.items)
    );

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  /**
   * Handles this payload on the client side
   */
  public static void handle(FluidContainerTransferPayload payload, IPayloadContext context) {
    context.enqueueWork(() -> {
      FluidContainerTransferManager.INSTANCE.setContainerItems(payload.items);
    });
  }
}
