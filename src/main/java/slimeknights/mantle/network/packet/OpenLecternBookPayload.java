package slimeknights.mantle.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.item.ILecternBookItem;

/**
 * Payload to open a book on a lectern
 */
public record OpenLecternBookPayload(BlockPos pos, ItemStack book) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<OpenLecternBookPayload> TYPE =
    new CustomPacketPayload.Type<>(Mantle.getResource("open_lectern_book"));

  public static final StreamCodec<RegistryFriendlyByteBuf, OpenLecternBookPayload> STREAM_CODEC =
    StreamCodec.composite(
      BlockPos.STREAM_CODEC,
      OpenLecternBookPayload::pos,
      ItemStack.OPTIONAL_STREAM_CODEC,
      OpenLecternBookPayload::book,
      OpenLecternBookPayload::new
    );

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  /**
   * Handles this payload on the client side
   */
  public static void handle(OpenLecternBookPayload payload, IPayloadContext context) {
    context.workHandler().execute(() -> {
      if (payload.book.getItem() instanceof ILecternBookItem) {
        ((ILecternBookItem) payload.book.getItem()).openLecternScreenClient(payload.pos, payload.book);
      }
    });
  }
}
