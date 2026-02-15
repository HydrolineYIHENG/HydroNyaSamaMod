package cn.hydcraft.hydronyasama.objrender.fabric.v120;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

public final class ObjModelResourceHandler120 implements ModelResourceProvider {
  private static final String MOD_ID = "hydronyasama";
  private static final Set<String> SUPPORTED_MODEL_PATHS =
      Set.of(
          "block/ad_board",
          "block/cuball_lamp",
          "block/fluorescent_light",
          "block/fluorescent_light_flock",
          "block/holo_jet_rev",
          "block/led_plate",
          "block/mosaic_light_mono",
          "block/mosaic_light_mono_small",
          "block/mosaic_light_multi",
          "block/mosaic_light_multi_small",
          "block/pillar_body",
          "block/pillar_head",
          "block/platform_light_full",
          "block/platform_light_half",
          "block/platform_plate_full",
          "block/platform_plate_half",
          "block/station_board",
          "block/station_lamp",
          "block/adsorption_lamp_large",
          "block/adsorption_lamp_mono",
          "block/adsorption_lamp_multi",
          "item/ad_board",
          "item/cuball_lamp",
          "item/fluorescent_light",
          "item/fluorescent_light_flock",
          "item/holo_jet_rev",
          "item/led_plate",
          "item/mosaic_light_mono",
          "item/mosaic_light_mono_small",
          "item/mosaic_light_multi",
          "item/mosaic_light_multi_small",
          "item/pillar_body",
          "item/pillar_head",
          "item/platform_light_full",
          "item/platform_light_half",
          "item/platform_plate_full",
          "item/platform_plate_half",
          "item/station_board",
          "item/station_lamp",
          "item/adsorption_lamp_large",
          "item/adsorption_lamp_mono",
          "item/adsorption_lamp_multi");

  private final ResourceManager resourceManager;
  private final Map<ResourceLocation, Optional<UnbakedModel>> cache = new ConcurrentHashMap<>();

  public ObjModelResourceHandler120(ResourceManager resourceManager) {
    this.resourceManager = resourceManager;
  }

  public static void register() {
    ModelLoadingRegistry.INSTANCE.registerResourceProvider(ObjModelResourceHandler120::new);
  }

  @Override
  public @Nullable UnbakedModel loadModelResource(
      ResourceLocation resourceId, ModelProviderContext context) throws ModelProviderException {
    if (!MOD_ID.equals(resourceId.getNamespace())) {
      return null;
    }
    if (!SUPPORTED_MODEL_PATHS.contains(resourceId.getPath())) {
      return null;
    }
    Optional<UnbakedModel> cached = cache.get(resourceId);
    if (cached != null) {
      return cached.orElse(null);
    }
    UnbakedModel loaded = ObjUnbakedModel120.tryLoad(resourceManager, resourceId);
    Optional<UnbakedModel> boxed = Optional.ofNullable(loaded);
    cache.put(resourceId, boxed);
    return boxed.orElse(null);
  }
}
