package slimeknights.mantle.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.command.client.BookCommand;

/**
 * Payload to open a book by its resource location name
 */
public record OpenNamedBookPayload(ResourceLocation book) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<OpenNamedBookPayload> TYPE =
    new CustomPacketPayload.Type<>(Mantle.getResource("open_named_book"));

  public static final StreamCodec<FriendlyByteBuf, OpenNamedBookPayload> STREAM_CODEC =
    StreamCodec.composite(
      ResourceLocation.STREAM_CODEC,
      OpenNamedBookPayload::book,
      OpenNamedBookPayload::new
    );

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  /**
   * Handles this payload on the client side
   */
  public static void handle(OpenNamedBookPayload payload, IPayloadContext context) {
    context.workHandler().execute(() -> {
      BookData bookData = BookLoader.getBook(payload.book);
      if (bookData != null) {
        bookData.openGui(Component.literal("Book"), "", null, null);
      } else {
        ClientOnly.errorStatus(payload.book);
      }
    });
  }

  static class ClientOnly {
    static void errorStatus(ResourceLocation book) {
      BookCommand.bookNotFound(book);
    }
  }
}
