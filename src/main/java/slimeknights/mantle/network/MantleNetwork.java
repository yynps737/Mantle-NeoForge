package slimeknights.mantle.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferPayload;
import slimeknights.mantle.network.packet.DropLecternBookPayload;
import slimeknights.mantle.network.packet.OpenLecternBookPayload;
import slimeknights.mantle.network.packet.OpenNamedBookPayload;
import slimeknights.mantle.network.packet.SwingArmPayload;
import slimeknights.mantle.network.packet.UpdateHeldPagePayload;
import slimeknights.mantle.network.packet.UpdateInventoryPagePayload;
import slimeknights.mantle.network.packet.UpdateLecternPagePayload;

/**
 * Network handler for Mantle using the new NeoForge 1.21+ payload system
 */
public class MantleNetwork {
  /**
   * Network version
   * 1: 1.11.101 and before
   * 2: 1.11.102 - New predicate types, enum loadable nullable field optimization
   * 3: 1.21.1 - NeoForge CustomPacketPayload migration
   */
  private static final String VERSION = "3";

  /**
   * Registers all network payloads for Mantle
   */
  @SubscribeEvent
  public static void registerPayloads(RegisterPayloadHandlersEvent event) {
    final PayloadRegistrar registrar = event.registrar(VERSION);

    // Client-bound payloads (server -> client)
    registrar.playToClient(
      OpenLecternBookPayload.TYPE,
      OpenLecternBookPayload.STREAM_CODEC,
      OpenLecternBookPayload::handle
    );

    registrar.playToClient(
      SwingArmPayload.TYPE,
      SwingArmPayload.STREAM_CODEC,
      SwingArmPayload::handle
    );

    registrar.playToClient(
      OpenNamedBookPayload.TYPE,
      OpenNamedBookPayload.STREAM_CODEC,
      OpenNamedBookPayload::handle
    );

    registrar.playToClient(
      FluidContainerTransferPayload.TYPE,
      FluidContainerTransferPayload.STREAM_CODEC,
      FluidContainerTransferPayload::handle
    );

    // Server-bound payloads (client -> server)
    registrar.playToServer(
      UpdateHeldPagePayload.TYPE,
      UpdateHeldPagePayload.STREAM_CODEC,
      UpdateHeldPagePayload::handle
    );

    registrar.playToServer(
      UpdateInventoryPagePayload.TYPE,
      UpdateInventoryPagePayload.STREAM_CODEC,
      UpdateInventoryPagePayload::handle
    );

    registrar.playToServer(
      UpdateLecternPagePayload.TYPE,
      UpdateLecternPagePayload.STREAM_CODEC,
      UpdateLecternPagePayload::handle
    );

    registrar.playToServer(
      DropLecternBookPayload.TYPE,
      DropLecternBookPayload.STREAM_CODEC,
      DropLecternBookPayload::handle
    );
  }
}
