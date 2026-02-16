package cn.hydcraft.hydronyasama.objrender.fabric.v120;

import com.google.common.base.Suppliers;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ObjMeshBakedModel120 implements BakedModel, FabricBakedModel {
  private final Mesh mesh;
  private final TextureAtlasSprite particleIcon;
  private final boolean useAmbientOcclusion;
  private final boolean usesBlockLight;
  private final Supplier<List<BakedQuad>[]> quadCache;

  public ObjMeshBakedModel120(
      Mesh mesh,
      TextureAtlasSprite particleIcon,
      boolean useAmbientOcclusion,
      boolean usesBlockLight) {
    this.mesh = mesh;
    this.particleIcon = particleIcon;
    this.useAmbientOcclusion = useAmbientOcclusion;
    this.usesBlockLight = usesBlockLight;
    this.quadCache = Suppliers.memoize(() -> ModelHelper.toQuadLists(mesh));
  }

  @Override
  public boolean isVanillaAdapter() {
    return false;
  }

  @Override
  public void emitBlockQuads(
      BlockAndTintGetter blockView,
      BlockState state,
      BlockPos pos,
      Supplier<RandomSource> randomSupplier,
      RenderContext context) {
    context.meshConsumer().accept(mesh);
  }

  @Override
  public void emitItemQuads(
      ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
    context.meshConsumer().accept(mesh);
  }

  @Override
  public @NotNull List<BakedQuad> getQuads(
      @Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
    return quadCache.get()[ModelHelper.toFaceIndex(direction)];
  }

  @Override
  public boolean useAmbientOcclusion() {
    return useAmbientOcclusion;
  }

  @Override
  public boolean isGui3d() {
    return true;
  }

  @Override
  public boolean usesBlockLight() {
    return usesBlockLight;
  }

  @Override
  public boolean isCustomRenderer() {
    return false;
  }

  @Override
  public TextureAtlasSprite getParticleIcon() {
    return particleIcon;
  }

  @Override
  public ItemTransforms getTransforms() {
    return ItemTransforms.NO_TRANSFORMS;
  }

  @Override
  public ItemOverrides getOverrides() {
    return ItemOverrides.EMPTY;
  }
}
