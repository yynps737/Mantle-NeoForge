package slimeknights.mantle.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;

/**
 * Payload to drop the book as item from lectern
 */
public record DropLecternBookPayload(BlockPos pos) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<DropLecternBookPayload> TYPE =
    new CustomPacketPayload.Type<>(Mantle.getResource("drop_lectern_book"));

  public static final StreamCodec<RegistryFriendlyByteBuf, DropLecternBookPayload> STREAM_CODEC =
    StreamCodec.composite(
      BlockPos.STREAM_CODEC,
      DropLecternBookPayload::pos,
      DropLecternBookPayload::new
    );

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  /**
   * Handles this payload on the server side
   */
  @SuppressWarnings("deprecation")
  public static void handle(DropLecternBookPayload payload, IPayloadContext context) {
    context.workHandler().execute(() -> {
      context.player().ifPresent(p -> {
        if (!(p instanceof ServerPlayer player)) {
          return;
        }

        ServerLevel world = player.serverLevel();
        if (!world.hasChunkAt(payload.pos)) {
          return;
        }

        BlockState state = world.getBlockState(payload.pos);

        if (state.getBlock() instanceof LecternBlock && state.getValue(LecternBlock.HAS_BOOK)) {
          BlockEntity te = world.getBlockEntity(payload.pos);
          if (te instanceof LecternBlockEntity lecternTe) {
            ItemStack book = lecternTe.getBook().copy();
            if (!book.isEmpty()) {
              if (!player.addItem(book)) {
                player.drop(book, false, false);
              }

              lecternTe.clearContent();

              // fix lectern state
              world.setBlock(payload.pos, state.setValue(LecternBlock.POWERED, false).setValue(LecternBlock.HAS_BOOK, false), 3);
              world.updateNeighborsAt(payload.pos.below(), state.getBlock());
            }
          }
        }
      });
    });
  }
}
