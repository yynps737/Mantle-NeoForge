package slimeknights.mantle.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookHelper;

/**
 * Payload to update the page in a book in the players inventory
 */
public record UpdateInventoryPagePayload(int slot, String page) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<UpdateInventoryPagePayload> TYPE =
    new CustomPacketPayload.Type<>(Mantle.getResource("update_inventory_page"));

  public static final StreamCodec<ByteBuf, UpdateInventoryPagePayload> STREAM_CODEC =
    StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      UpdateInventoryPagePayload::slot,
      ByteBufCodecs.stringUtf8(100),
      UpdateInventoryPagePayload::page,
      UpdateInventoryPagePayload::new
    );

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  /**
   * Handles this payload on the server side
   */
  public static void handle(UpdateInventoryPagePayload payload, IPayloadContext context) {
    context.enqueueWork(() -> {
      Player player = context.player();
      if (payload.page != null && payload.slot >= 0) {
        ItemStack stack = player.getInventory().getItem(payload.slot);
        if (!stack.isEmpty()) {
          BookHelper.writeSavedPageToBook(stack, payload.page);
        }
      }
    });
  }
}
