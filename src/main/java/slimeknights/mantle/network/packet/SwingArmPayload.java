package slimeknights.mantle.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.OffhandCooldownTracker;

/** Payload to tell a client to swing an entity arm, as the vanilla one resets cooldown */
public record SwingArmPayload(int entityId, InteractionHand hand) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<SwingArmPayload> TYPE =
    new CustomPacketPayload.Type<>(Mantle.getResource("swing_arm"));

  public static final StreamCodec<ByteBuf, SwingArmPayload> STREAM_CODEC =
    StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      SwingArmPayload::entityId,
      ByteBufCodecs.idMapper(i -> InteractionHand.values()[i], InteractionHand::ordinal),
      SwingArmPayload::hand,
      SwingArmPayload::new
    );

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  /**
   * Handles this payload on the client side
   */
  public static void handle(SwingArmPayload payload, IPayloadContext context) {
    context.enqueueWork(() -> {
      Level level = Minecraft.getInstance().level;
      if (level != null) {
        Entity entity = level.getEntity(payload.entityId);
        if (entity instanceof LivingEntity) {
          OffhandCooldownTracker.swingHand((LivingEntity) entity, payload.hand, false);
        }
      }
    });
  }
}
