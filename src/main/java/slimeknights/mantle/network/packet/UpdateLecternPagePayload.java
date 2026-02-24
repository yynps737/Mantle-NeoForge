package slimeknights.mantle.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookHelper;
import slimeknights.mantle.util.BlockEntityHelper;

/**
 * Payload to update the book page in a lectern
 */
public record UpdateLecternPagePayload(BlockPos pos, String page) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<UpdateLecternPagePayload> TYPE =
    new CustomPacketPayload.Type<>(Mantle.getResource("update_lectern_page"));

  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateLecternPagePayload> STREAM_CODEC =
    StreamCodec.composite(
      BlockPos.STREAM_CODEC,
      UpdateLecternPagePayload::pos,
      ByteBufCodecs.stringUtf8(100),
      UpdateLecternPagePayload::page,
      UpdateLecternPagePayload::new
    );

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  /**
   * Handles this payload on the server side
   */
  public static void handle(UpdateLecternPagePayload payload, IPayloadContext context) {
    context.enqueueWork(() -> {
      Player player = context.player();
      if (payload.page != null) {
        Level world = player.level();
        BlockEntityHelper.get(LecternBlockEntity.class, world, payload.pos).ifPresent(te -> {
          ItemStack stack = te.getBook();
          if (!stack.isEmpty()) {
            BookHelper.writeSavedPageToBook(stack, payload.page);
          }
        });
      }
    });
  }
}
