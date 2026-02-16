package cn.hydcraft.hydronyasama.objrender.fabric.v120;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

final class CombinedObjUnbakedModel120 implements UnbakedModel {
  private final List<UnbakedModel> delegates;

  CombinedObjUnbakedModel120(List<UnbakedModel> delegates) {
    this.delegates = List.copyOf(delegates);
  }

  @Override
  public Collection<ResourceLocation> getDependencies() {
    Set<ResourceLocation> all = new LinkedHashSet<>();
    for (UnbakedModel delegate : delegates) {
      all.addAll(delegate.getDependencies());
    }
    return all;
  }

  @Override
  public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
    for (UnbakedModel delegate : delegates) {
      delegate.resolveParents(resolver);
    }
  }

  @Override
  public @Nullable BakedModel bake(
      ModelBaker baker,
      Function<Material, net.minecraft.client.renderer.texture.TextureAtlasSprite> textureGetter,
      ModelState modelState,
      ResourceLocation location) {
    List<BakedModel> baked = new ArrayList<>(delegates.size());
    for (UnbakedModel delegate : delegates) {
      BakedModel bakedModel = delegate.bake(baker, textureGetter, modelState, location);
      if (bakedModel != null) {
        baked.add(bakedModel);
      }
    }
    if (baked.isEmpty()) {
      return null;
    }
    if (baked.size() == 1) {
      return baked.get(0);
    }
    return new CombinedObjBakedModel120(baked);
  }
}
