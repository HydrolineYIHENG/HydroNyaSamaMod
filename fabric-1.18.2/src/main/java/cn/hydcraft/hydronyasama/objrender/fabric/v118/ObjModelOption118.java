package cn.hydcraft.hydronyasama.objrender.fabric.v118;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public record ObjModelOption118(
    boolean useAmbientOcclusion,
    boolean flipV,
    @Nullable BlockModel.GuiLight guiLight,
    @Nullable ResourceLocation particle) {

  public static ObjModelOption118 parse(JsonObject modelJson) {
    boolean ambient = GsonHelper.getAsBoolean(modelJson, "ambientocclusion", true);
    boolean flipV =
        GsonHelper.getAsBoolean(
            modelJson, "flip-v", GsonHelper.getAsBoolean(modelJson, "flip_v", false));

    BlockModel.GuiLight guiLight = null;
    if (modelJson.has("gui_light") && modelJson.get("gui_light").isJsonPrimitive()) {
      guiLight = BlockModel.GuiLight.getByName(modelJson.get("gui_light").getAsString());
    }

    ResourceLocation particle = null;
    if (modelJson.has("textures") && modelJson.get("textures").isJsonObject()) {
      JsonObject textures = modelJson.getAsJsonObject("textures");
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
    return new ObjModelOption118(ambient, flipV, guiLight, particle);
  }
}

