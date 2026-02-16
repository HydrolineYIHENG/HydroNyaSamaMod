package cn.hydcraft.hydronyasama.objrender.fabric.v116;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class ObjModelResourceHandler116 implements ModelResourceProvider {
  private static final String MOD_ID = "hydronyasama";
  private static final Logger LOGGER = LogManager.getLogger("HydroNyaSama-OBJ-116");

  private final ResourceManager resourceManager;
  private final Map<ResourceLocation, Optional<UnbakedModel>> cache = new ConcurrentHashMap<>();

  public ObjModelResourceHandler116(ResourceManager resourceManager) {
    this.resourceManager = resourceManager;
  }

  public static void register() {
    ModelLoadingRegistry.INSTANCE.registerResourceProvider(ObjModelResourceHandler116::new);
  }

  @Override
  public @Nullable UnbakedModel loadModelResource(
      ResourceLocation resourceId, ModelProviderContext context) throws ModelProviderException {
    if (!MOD_ID.equals(resourceId.getNamespace())) {
      return null;
    }
    Optional<UnbakedModel> cached = cache.get(resourceId);
    if (cached != null) {
      return cached.orElse(null);
    }

    UnbakedModel loaded;
    try {
      loaded = tryLoadByParentChain(resourceId);
    } catch (Throwable t) {
      // Do not break whole resource reload because of one OBJ entry.
      LOGGER.error("[obj-116] model load failed id={}", resourceId, t);
      loaded = null;
    }
    Optional<UnbakedModel> boxed = Optional.ofNullable(loaded);
    cache.put(resourceId, boxed);
    return boxed.orElse(null);
  }

  private @Nullable UnbakedModel tryLoadByParentChain(ResourceLocation modelId)
      throws ModelProviderException {
    JsonObject modelJson = readModelJson(modelId);
    if (modelJson == null) {
      return null;
    }

    UnbakedModel direct = tryLoadSupportedObj(modelJson);
    if (direct != null) {
      return direct;
    }

    List<JsonObject> chain = new ArrayList<>();
    Set<ResourceLocation> visited = new HashSet<>();

    JsonObject current = modelJson;
    ResourceLocation parent = getParentLocation(current);
    while (parent != null) {
      chain.add(current);
      if (!visited.add(parent)) {
        return null;
      }

      current = readModelJson(parent);
      if (current == null) {
        return null;
      }

      UnbakedModel parentObj = tryLoadSupportedObj(current);
      if (parentObj != null) {
        chain.add(current);
        JsonObject merged = mergeParentChain(chain);
        return tryLoadSupportedObj(merged);
      }

      parent = getParentLocation(current);
    }
    return null;
  }

  private @Nullable UnbakedModel tryLoadSupportedObj(JsonObject modelJson)
      throws ModelProviderException {
    if (!ObjUnbakedModel116.isForgeObjModel(modelJson)) {
      return null;
    }
    ResourceLocation modelLocation = ObjUnbakedModel116.getObjModelLocation(modelJson);
    if (!isSupportedObjModelLocation(modelLocation)) {
      return null;
    }
    LOGGER.info("[obj-116] obj hit obj={}", modelLocation);
    JsonObject baseJson = modelJson;
    if (modelLocation != null && isStationLampPart(modelLocation)) {
      baseJson = copyJsonObject(modelJson);
      JsonObject textures =
          baseJson.has("textures") && baseJson.get("textures").isJsonObject()
              ? baseJson.getAsJsonObject("textures")
              : new JsonObject();
      applyStationLampMaterialTextureOverrides(textures, modelLocation);
      baseJson.add("textures", textures);
      baseJson.addProperty("double_sided", true);
    }
    UnbakedModel baseModel = ObjUnbakedModel116.tryLoadFromModelJson(resourceManager, baseJson);
    if (baseModel == null || modelLocation == null) {
      return baseModel;
    }
    List<UnbakedModel> layers = new ArrayList<>();
    layers.add(baseModel);

    ResourceLocation lightLocation = toLightObjModelLocation(modelLocation);
    if (lightLocation != null && resourceManager.hasResource(lightLocation)) {
      UnbakedModel lightModel =
          loadExtraLayer(modelJson, lightLocation, "hydronyasama:block/light_base", 0);
      if (lightModel != null) {
        layers.add(lightModel);
      }
    }

    if (isStationLampMidObj(modelLocation)) {
      addStationLampLayers(modelJson, layers);
    }

    return layers.size() == 1 ? baseModel : new CombinedObjUnbakedModel116(layers);
  }

  private static boolean isSupportedObjModelLocation(@Nullable ResourceLocation modelLocation) {
    if (modelLocation == null || !MOD_ID.equals(modelLocation.getNamespace())) {
      return false;
    }
    String path = modelLocation.getPath();
    return path.startsWith("models/blocks/") && path.endsWith(".obj");
  }

  private static @Nullable ResourceLocation toLightObjModelLocation(
      ResourceLocation modelLocation) {
    String path = modelLocation.getPath();
    if (!path.endsWith("_base.obj")) {
      return null;
    }
    String lightPath = path.substring(0, path.length() - "_base.obj".length()) + "_light.obj";
    return new ResourceLocation(modelLocation.getNamespace(), lightPath);
  }

  private @Nullable UnbakedModel loadExtraLayer(
      JsonObject baseModelJson, ResourceLocation modelLocation, String particleTexture, int rotateY)
      throws ModelProviderException {
    JsonObject layerJson = copyJsonObject(baseModelJson);
    layerJson.addProperty("model", modelLocation.toString());
    if (rotateY != 0) {
      layerJson.addProperty("rotate_y", rotateY);
    }
    JsonObject textures =
        layerJson.has("textures") && layerJson.get("textures").isJsonObject()
            ? layerJson.getAsJsonObject("textures")
            : new JsonObject();
    textures.addProperty("particle", particleTexture);
    applyStationLampMaterialTextureOverrides(textures, modelLocation);
    layerJson.add("textures", textures);
    if (isStationLampPart(modelLocation)) {
      layerJson.addProperty("double_sided", true);
    }
    return ObjUnbakedModel116.tryLoadFromModelJson(resourceManager, layerJson);
  }

  private void addStationLampLayers(JsonObject modelJson, List<UnbakedModel> layers)
      throws ModelProviderException {
    ResourceLocation[] parts =
        new ResourceLocation[] {
          new ResourceLocation(MOD_ID, "models/blocks/station_lamp_top.obj"),
          new ResourceLocation(MOD_ID, "models/blocks/station_lamp_end.obj"),
          new ResourceLocation(MOD_ID, "models/blocks/station_lamp_logo.obj"),
          new ResourceLocation(MOD_ID, "models/blocks/station_lamp_back.obj")
        };
    for (ResourceLocation part : parts) {
      if (!resourceManager.hasResource(part)) {
        continue;
      }
      if (isStationLampFacePart(part)) {
        for (int rotateY : new int[] {0, 90, 180, 270}) {
          UnbakedModel layer =
              loadExtraLayer(modelJson, part, stationLampParticleTexture(part), rotateY);
          if (layer != null) {
            layers.add(layer);
          }
        }
      } else {
        UnbakedModel layer = loadExtraLayer(modelJson, part, stationLampParticleTexture(part), 0);
        if (layer != null) {
          layers.add(layer);
        }
      }
    }
  }

  private static boolean isStationLampMidObj(ResourceLocation modelLocation) {
    return MOD_ID.equals(modelLocation.getNamespace())
        && "models/blocks/station_lamp_mid.obj".equals(modelLocation.getPath());
  }

  private static String stationLampParticleTexture(ResourceLocation modelLocation) {
    String path = modelLocation.getPath();
    if (path.endsWith("station_lamp_logo.obj")) {
      return "hydronyasama:block/station_lamp_logo";
    }
    if (path.endsWith("station_lamp_back.obj")) {
      return "hydronyasama:block/station_lamp_back";
    }
    return "hydronyasama:block/station_lamp_base";
  }

  private static void applyStationLampMaterialTextureOverrides(
      JsonObject textures, ResourceLocation modelLocation) {
    String path = modelLocation.getPath();
    if (path.endsWith("station_lamp_logo.obj")) {
      textures.addProperty("2", "hydronyasama:block/station_lamp_logo");
      return;
    }
    if (path.endsWith("station_lamp_back.obj")) {
      textures.addProperty("1", "hydronyasama:block/station_lamp_back");
      return;
    }
    if (path.endsWith("station_lamp_mid.obj")
        || path.endsWith("station_lamp_top.obj")
        || path.endsWith("station_lamp_end.obj")) {
      textures.addProperty("0", "hydronyasama:block/station_lamp_base");
    }
  }

  private static boolean isStationLampPart(ResourceLocation modelLocation) {
    return MOD_ID.equals(modelLocation.getNamespace())
        && modelLocation.getPath().startsWith("models/blocks/station_lamp_");
  }

  private static boolean isStationLampFacePart(ResourceLocation modelLocation) {
    String path = modelLocation.getPath();
    return path.endsWith("station_lamp_logo.obj") || path.endsWith("station_lamp_back.obj");
  }

  private @Nullable JsonObject readModelJson(ResourceLocation modelId)
      throws ModelProviderException {
    for (ResourceLocation jsonLocation : candidateModelJsonLocations(modelId)) {
      if (!resourceManager.hasResource(jsonLocation)) {
        continue;
      }
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(
                  resourceManager.getResource(jsonLocation).getInputStream(),
                  StandardCharsets.UTF_8))) {
        return GsonHelper.parse(reader);
      } catch (IOException e) {
        throw new ModelProviderException("Failed to read model json: " + modelId, e);
      }
    }
    return null;
  }

  private static List<ResourceLocation> candidateModelJsonLocations(ResourceLocation modelId) {
    String path = modelId.getPath();
    List<ResourceLocation> out = new ArrayList<>(4);
    if (path.startsWith("models/")) {
      out.add(new ResourceLocation(modelId.getNamespace(), path + ".json"));
      return out;
    }
    out.add(new ResourceLocation(modelId.getNamespace(), "models/" + path + ".json"));
    if (!path.startsWith("block/") && !path.startsWith("item/")) {
      out.add(new ResourceLocation(modelId.getNamespace(), "models/block/" + path + ".json"));
      out.add(new ResourceLocation(modelId.getNamespace(), "models/item/" + path + ".json"));
    }
    return out;
  }

  private static @Nullable ResourceLocation getParentLocation(JsonObject modelJson) {
    if (!modelJson.has("parent") || !modelJson.get("parent").isJsonPrimitive()) {
      return null;
    }
    try {
      return new ResourceLocation(modelJson.getAsJsonPrimitive("parent").getAsString());
    } catch (RuntimeException ignored) {
      return null;
    }
  }

  private static JsonObject mergeParentChain(List<JsonObject> chain) {
    JsonObject merged = new JsonObject();
    for (int i = chain.size() - 1; i >= 0; i--) {
      JsonObject part = chain.get(i);
      part.entrySet()
          .forEach(entry -> merged.add(entry.getKey(), copyJsonElement(entry.getValue())));
    }
    return merged;
  }

  private static JsonObject copyJsonObject(JsonObject source) {
    return new JsonParser().parse(source.toString()).getAsJsonObject();
  }

  private static com.google.gson.JsonElement copyJsonElement(com.google.gson.JsonElement source) {
    return new JsonParser().parse(source.toString());
  }
}
