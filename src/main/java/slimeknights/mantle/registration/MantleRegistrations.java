package slimeknights.mantle.registration;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.block.entity.MantleHangingSignBlockEntity;
import slimeknights.mantle.block.entity.MantleSignBlockEntity;

/**
 * Various objects registered under Mantle
 */
public class MantleRegistrations {
  private MantleRegistrations() {}

  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MantleSignBlockEntity>> SIGN =
    DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, Mantle.getResource("sign"));

  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MantleHangingSignBlockEntity>> HANGING_SIGN =
    DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, Mantle.getResource("hanging_sign"));
}
