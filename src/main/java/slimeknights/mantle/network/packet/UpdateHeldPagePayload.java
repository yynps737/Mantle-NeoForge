package slimeknights.mantle.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookHelper;

/**
 * Payload to update the page in a book in the players hand
 */
public record UpdateHeldPagePayload(InteractionHand hand, String page) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<UpdateHeldPagePayload> TYPE =
    new CustomPacketPayload.Type<>(Mantle.getResource("update_held_page"));

  public static final StreamCodec<ByteBuf, UpdateHeldPagePayload> STREAM_CODEC =
    StreamCodec.composite(
      ByteBufCodecs.idMapper(i -> InteractionHand.values()[i], InteractionHand::ordinal),
      UpdateHeldPagePayload::hand,
      ByteBufCodecs.stringUtf8(100),
      UpdateHeldPagePayload::page,
      UpdateHeldPagePayload::new
    );

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  /**
   * Handles this payload on the server side
   */
  public static void handle(UpdateHeldPagePayload payload, IPayloadContext context) {
    context.enqueueWork(() -> {
      Player player = context.player();
      if (payload.page != null) {
        ItemStack stack = player.getItemInHand(payload.hand);
        if (!stack.isEmpty()) {
          BookHelper.writeSavedPageToBook(stack, payload.page);
        }
      }
    });
  }
}
