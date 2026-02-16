package cn.hydcraft.hydronyasama.objrender.fabric.v116;

import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public final class ObjModelOption116 {
  private final boolean useAmbientOcclusion;
  private final boolean flipV;
  private final boolean doubleSided;
  private final int rotateY;
  private final @Nullable BlockModel.GuiLight guiLight;
  private final @Nullable ResourceLocation particle;
  private final Map<String, ResourceLocation> materialTextures;

  private ObjModelOption116(
      boolean useAmbientOcclusion,
      boolean flipV,
      boolean doubleSided,
      int rotateY,
      @Nullable BlockModel.GuiLight guiLight,
      @Nullable ResourceLocation particle,
      Map<String, ResourceLocation> materialTextures) {
    this.useAmbientOcclusion = useAmbientOcclusion;
    this.flipV = flipV;
    this.doubleSided = doubleSided;
    this.rotateY = rotateY;
    this.guiLight = guiLight;
    this.particle = particle;
    this.materialTextures = materialTextures;
  }

  public static ObjModelOption116 parse(JsonObject modelJson) {
    boolean ambient = GsonHelper.getAsBoolean(modelJson, "ambientocclusion", true);
    boolean flipV =
        GsonHelper.getAsBoolean(
            modelJson, "flip-v", GsonHelper.getAsBoolean(modelJson, "flip_v", false));
    boolean doubleSided =
        GsonHelper.getAsBoolean(
            modelJson,
            "double_sided",
            GsonHelper.getAsBoolean(modelJson, "doubleSided", false));
    int rotateY =
        Math.floorMod(
            GsonHelper.getAsInt(
                modelJson,
                "rotate_y",
                GsonHelper.getAsInt(modelJson, "rotation_y", 0)),
            360);

    BlockModel.GuiLight guiLight = null;
    if (modelJson.has("gui_light") && modelJson.get("gui_light").isJsonPrimitive()) {
      guiLight = BlockModel.GuiLight.getByName(modelJson.get("gui_light").getAsString());
    }

    ResourceLocation particle = null;
    Map<String, ResourceLocation> materialTextures = new HashMap<>();
    if (modelJson.has("textures") && modelJson.get("textures").isJsonObject()) {
      JsonObject textures = modelJson.getAsJsonObject("textures");
      for (Map.Entry<String, com.google.gson.JsonElement> entry : textures.entrySet()) {
        String key = entry.getKey();
        if (!entry.getValue().isJsonPrimitive()) {
          continue;
        }
        String texture = entry.getValue().getAsString();
        if (texture.isEmpty() || texture.charAt(0) == '#') {
          continue;
        }
        if ("particle".equals(key)) {
          particle = new ResourceLocation(texture);
        } else {
          materialTextures.put(key, new ResourceLocation(texture));
        }
      }
      if (textures.has("particle") && textures.get("particle").isJsonPrimitive()) {
        String texture = textures.get("particle").getAsString();
        if (!texture.isEmpty() && texture.charAt(0) != '#') {
          particle = new ResourceLocation(texture);
        }
      }
    }
    if (particle == null && modelJson.has("particle") && modelJson.get("particle").isJsonPrimitive()) {
      String particleRaw = modelJson.get("particle").getAsString();
      if (!particleRaw.isEmpty()) {
        particle = new ResourceLocation(particleRaw);
      }
    }
    return new ObjModelOption116(
        ambient,
        flipV,
        doubleSided,
        rotateY,
        guiLight,
        particle,
        Collections.unmodifiableMap(materialTextures));
  }

  public boolean useAmbientOcclusion() {
    return useAmbientOcclusion;
  }

  public boolean flipV() {
    return flipV;
  }

  public boolean doubleSided() {
    return doubleSided;
  }

  public int rotateY() {
    return rotateY;
  }

  public @Nullable BlockModel.GuiLight guiLight() {
    return guiLight;
  }

  public @Nullable ResourceLocation particle() {
    return particle;
  }

  public Map<String, ResourceLocation> materialTextures() {
    return materialTextures;
  }
}


