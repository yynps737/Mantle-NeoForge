package slimeknights.mantle.fluid;

import net.neoforged.neoforge.fluids.FluidType;

/**
 * Fluid type whose color and textures are determined by the model.
 * Implements {@link slimeknights.mantle.fluid.texture.ClientTextureFluidType} via RegisterClientExtensionsEvent.
 */
public class TextureFluidType extends FluidType {
  public TextureFluidType(Properties properties) {
    super(properties);
  }
}
