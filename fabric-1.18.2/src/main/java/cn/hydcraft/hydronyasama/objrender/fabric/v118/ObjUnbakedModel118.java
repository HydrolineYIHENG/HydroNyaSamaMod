package cn.hydcraft.hydronyasama.objrender.fabric.v118;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import de.javagl.obj.FloatTuple;
import de.javagl.obj.Mtl;
import de.javagl.obj.MtlReader;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjSplitting;
import de.javagl.obj.ObjUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.nio.charset.StandardCharsets;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

public final class ObjUnbakedModel118 implements UnbakedModel {
  private static final ResourceLocation FORGE_OBJ_LOADER = new ResourceLocation("forge", "obj");
  private static final Material MISSING_MATERIAL =
      new Material(InventoryMenu.BLOCK_ATLAS, MissingTextureAtlasSprite.getLocation());

  private final Obj obj;
  private final Map<String, Mtl> mtlMap;
  private final ObjModelOption118 option;

  private ObjUnbakedModel118(Obj obj, Map<String, Mtl> mtlMap, ObjModelOption118 option) {
    this.obj = obj;
    this.mtlMap = mtlMap;
    this.option = option;
  }

  public static @Nullable UnbakedModel tryLoad(ResourceManager resourceManager, ResourceLocation resourceId)
      throws ModelProviderException {
    JsonObject modelJson = readModelJson(resourceManager, resourceId);
    return tryLoadFromModelJson(resourceManager, modelJson);
  }

  public static boolean isForgeObjModel(@Nullable JsonObject modelJson) {
    if (modelJson == null || !modelJson.has("loader") || !modelJson.get("loader").isJsonPrimitive()) {
      return false;
    }
    try {
      ResourceLocation loaderId = new ResourceLocation(modelJson.getAsJsonPrimitive("loader").getAsString());
      return FORGE_OBJ_LOADER.equals(loaderId);
    } catch (RuntimeException ignored) {
      return false;
    }
  }

  public static @Nullable ResourceLocation getObjModelLocation(@Nullable JsonObject modelJson) {
    if (modelJson == null || !modelJson.has("model") || !modelJson.get("model").isJsonPrimitive()) {
      return null;
    }
    try {
      return new ResourceLocation(modelJson.getAsJsonPrimitive("model").getAsString());
    } catch (RuntimeException ignored) {
      return null;
    }
  }

  public static @Nullable UnbakedModel tryLoadFromModelJson(
      ResourceManager resourceManager, @Nullable JsonObject modelJson) throws ModelProviderException {
    if (!isForgeObjModel(modelJson)) {
      return null;
    }
    ResourceLocation modelLocation = getObjModelLocation(modelJson);
    if (modelLocation == null) {
      return null;
    }
    ObjModelOption118 option = ObjModelOption118.parse(modelJson);
    return loadObjModel(resourceManager, modelLocation, option);
  }

  private static @Nullable JsonObject readModelJson(ResourceManager resourceManager, ResourceLocation modelId)
      throws ModelProviderException {
    ResourceLocation jsonLocation =
        new ResourceLocation(modelId.getNamespace(), "models/" + modelId.getPath() + ".json");
    if (!resourceManager.hasResource(jsonLocation)) {
      return null;
    }
    try (var reader =
        new BufferedReader(
            new InputStreamReader(resourceManager.getResource(jsonLocation).getInputStream(), StandardCharsets.UTF_8))) {
      return GsonHelper.parse(reader);
    } catch (IOException e) {
      throw new ModelProviderException("Failed to read model json: " + modelId, e);
    }
  }

  private static @Nullable UnbakedModel loadObjModel(
      ResourceManager resourceManager, ResourceLocation modelLocation, ObjModelOption118 option)
      throws ModelProviderException {
    if (!resourceManager.hasResource(modelLocation)) {
      return null;
    }
    try (var reader =
        new BufferedReader(
            new InputStreamReader(resourceManager.getResource(modelLocation).getInputStream(), StandardCharsets.UTF_8))) {
      Obj source = ObjReader.read(reader);
      Obj obj = ObjUtils.convertToRenderable(ObjUtils.triangulate(source));
      Map<String, Mtl> mtlMap = loadMtl(resourceManager, modelLocation, obj.getMtlFileNames());
      return new ObjUnbakedModel118(obj, mtlMap, option);
    } catch (IOException e) {
      throw new ModelProviderException("Failed to read obj model: " + modelLocation, e);
    }
  }

  private static Map<String, Mtl> loadMtl(
      ResourceManager resourceManager, ResourceLocation modelLocation, List<String> mtlNames) {
    Map<String, Mtl> materialMap = new HashMap<>();
    int slash = modelLocation.getPath().lastIndexOf('/');
    String modelDir = slash >= 0 ? modelLocation.getPath().substring(0, slash) : "";
    for (String mtlName : mtlNames) {
      ResourceLocation mtlLocation =
          modelDir.isEmpty()
              ? new ResourceLocation(modelLocation.getNamespace(), mtlName)
              : new ResourceLocation(modelLocation.getNamespace(), modelDir + "/" + mtlName);
      if (!resourceManager.hasResource(mtlLocation)) {
        continue;
      }
      try (var reader =
          new BufferedReader(
              new InputStreamReader(resourceManager.getResource(mtlLocation).getInputStream(), StandardCharsets.UTF_8))) {
        for (Mtl mtl : MtlReader.read(reader)) {
          materialMap.put(mtl.getName(), mtl);
        }
      } catch (Exception ignored) {
        // Keep rendering with particle fallback if mtl parsing fails.
      }
    }
    return materialMap;
  }

  @Override
  public Collection<ResourceLocation> getDependencies() {
    return List.of();
  }

  @Override
  public Collection<Material> getMaterials(
      Function<ResourceLocation, UnbakedModel> resolver, Set<Pair<String, String>> unresolvedTextureReferences) {
    Set<Material> materials = new HashSet<>();
    Material particle = getParticleMaterial();
    if (particle != null) {
      materials.add(particle);
    }
    for (Mtl mtl : mtlMap.values()) {
      if (mtl == null || mtl.getMapKd() == null || mtl.getMapKd().isBlank()) {
        continue;
      }
      String texturePath = normalizeTexturePath(mtl.getMapKd());
      if (texturePath != null) {
        materials.add(new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(texturePath)));
      }
    }
    return materials;
  }

  @Override
  public BakedModel bake(
      ModelBakery modelBaker,
      Function<Material, TextureAtlasSprite> textureGetter,
      ModelState modelState,
      ResourceLocation modelLocation) {
    Renderer renderer = RendererAccess.INSTANCE.getRenderer();
    if (renderer == null) {
      return Minecraft.getInstance().getModelManager().getMissingModel();
    }

    MeshBuilder builder = renderer.meshBuilder();
    QuadEmitter emitter = builder.getEmitter();
    Material particleMaterial = getParticleMaterial();

    Map<String, Obj> materialGroups = ObjSplitting.splitByMaterialGroups(obj);
    materialGroups.forEach(
        (materialName, groupObj) -> {
          for (int i = 0; i < groupObj.getNumFaces(); i++) {
            emitFace(emitter, textureGetter, modelState, particleMaterial, materialName, groupObj.getFace(i), groupObj);
          }
        });

    BlockModel.GuiLight guiLight = option.guiLight();
    boolean usesBlockLight = guiLight == null || guiLight.lightLikeBlock();
    TextureAtlasSprite particle = textureGetter.apply(particleMaterial);
    return new ObjMeshBakedModel118(builder.build(), particle, option.useAmbientOcclusion(), usesBlockLight);
  }

  private Material getParticleMaterial() {
    if (option.particle() == null) {
      return MISSING_MATERIAL;
    }
    return new Material(InventoryMenu.BLOCK_ATLAS, option.particle());
  }

  private void emitFace(
      QuadEmitter emitter,
      Function<Material, TextureAtlasSprite> textureGetter,
      ModelState modelState,
      Material particleMaterial,
      String materialName,
      ObjFace face,
      Obj model) {
    if (face.getNumVertices() < 3) {
      return;
    }
    emitSingleFace(
        emitter, textureGetter, modelState, particleMaterial, materialName, face, model, false);
    if (option.doubleSided()) {
      emitSingleFace(
          emitter, textureGetter, modelState, particleMaterial, materialName, face, model, true);
    }
  }

  private void emitSingleFace(
      QuadEmitter emitter,
      Function<Material, TextureAtlasSprite> textureGetter,
      ModelState modelState,
      Material particleMaterial,
      String materialName,
      ObjFace face,
      Obj model,
      boolean reverse) {
    emitVertex(emitter, 0, mappedFaceVertex(0, face, reverse), modelState, face, model, reverse);
    emitVertex(emitter, 1, mappedFaceVertex(1, face, reverse), modelState, face, model, reverse);
    emitVertex(emitter, 2, mappedFaceVertex(2, face, reverse), modelState, face, model, reverse);
    emitVertex(emitter, 3, mappedFaceVertex(3, face, reverse), modelState, face, model, reverse);

    int bakeFlags = MutableQuadView.BAKE_NORMALIZED;
    if (option.flipV()) {
      bakeFlags |= MutableQuadView.BAKE_FLIP_V;
    }
    if (modelState.isUvLocked()) {
      bakeFlags |= MutableQuadView.BAKE_LOCK_UV;
    }

    Material sprite = getMaterialTexture(materialName, particleMaterial);
    emitter.spriteBake(0, textureGetter.apply(sprite), bakeFlags);
    emitter.spriteColor(0, -1, -1, -1, -1);
    emitter.emit();
  }

  private static int mappedFaceVertex(int emitIndex, ObjFace face, boolean reverse) {
    int[] order = reverse ? new int[] {3, 2, 1, 0} : new int[] {0, 1, 2, 3};
    int idx = order[emitIndex];
    return idx >= face.getNumVertices() ? 2 : idx;
  }

  private Material getMaterialTexture(String materialName, Material particleMaterial) {
    ResourceLocation overrideTexture = option.materialTextures().get(materialName);
    if (overrideTexture != null) {
      return new Material(InventoryMenu.BLOCK_ATLAS, overrideTexture);
    }
    Mtl mtl = mtlMap.get(materialName);
    if (mtl == null || mtl.getMapKd() == null || mtl.getMapKd().isBlank()) {
      return particleMaterial;
    }
    String texturePath = normalizeTexturePath(mtl.getMapKd());
    if (texturePath == null) {
      return particleMaterial;
    }
    return new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(texturePath));
  }

  private static @Nullable String normalizeTexturePath(String raw) {
    String value = raw.trim().replace('\\', '/');
    if (value.isEmpty()) {
      return null;
    }
    if (value.contains(":")) {
      if (value.endsWith(".png")) {
        return value.substring(0, value.length() - 4);
      }
      return value;
    }
    if (value.startsWith("./")) {
      value = value.substring(2);
    }
    if (value.startsWith("textures/")) {
      value = value.substring("textures/".length());
    }
    if (value.endsWith(".png")) {
      value = value.substring(0, value.length() - 4);
    }
    return "hydronyasama:" + value;
  }

  private void emitVertex(
      QuadEmitter emitter,
      int emitIndex,
      int faceVertexIndex,
      ModelState modelState,
      ObjFace face,
      Obj model,
      boolean reverse) {
    FloatTuple vertexTuple = model.getVertex(face.getVertexIndex(faceVertexIndex));
    float x = vertexTuple.getX() / 16.0F + 0.5F;
    float y = vertexTuple.getY() / 16.0F + 0.5F;
    float z = vertexTuple.getZ() / 16.0F + 0.5F;
    float[] rotated = rotateAroundCenterY(x, y, z, option.rotateY());
    x = rotated[0];
    y = rotated[1];
    z = rotated[2];
    emitter.pos(emitIndex, x, y, z);

    if (face.containsNormalIndices()) {
      FloatTuple normalTuple = model.getNormal(face.getNormalIndex(faceVertexIndex));
      float nx = normalTuple.getX();
      float ny = normalTuple.getY();
      float nz = normalTuple.getZ();
      if (reverse) {
        nx = -nx;
        ny = -ny;
        nz = -nz;
      }
      float[] rotatedNormal = rotateNormalY(nx, ny, nz, option.rotateY());
      nx = rotatedNormal[0];
      ny = rotatedNormal[1];
      nz = rotatedNormal[2];
      emitter.normal(emitIndex, nx, ny, nz);
    } else {
      emitter.normal(emitIndex, 0.0F, reverse ? -1.0F : 1.0F, 0.0F);
    }

    if (face.containsTexCoordIndices()) {
      FloatTuple uvTuple = model.getTexCoord(face.getTexCoordIndex(faceVertexIndex));
      emitter.sprite(emitIndex, 0, new Vec2(uvTuple.getX(), uvTuple.getY()));
    } else {
      emitter.sprite(emitIndex, 0, Vec2.ZERO);
    }
  }

  private static float[] rotateAroundCenterY(float x, float y, float z, int degrees) {
    int normalized = Math.floorMod(degrees, 360);
    if (normalized == 0) {
      return new float[] {x, y, z};
    }
    float localX = x - 0.5F;
    float localZ = z - 0.5F;
    float rotatedX;
    float rotatedZ;
    switch (normalized) {
      case 90:
        rotatedX = -localZ;
        rotatedZ = localX;
        break;
      case 180:
        rotatedX = -localX;
        rotatedZ = -localZ;
        break;
      case 270:
        rotatedX = localZ;
        rotatedZ = -localX;
        break;
      default:
        return new float[] {x, y, z};
    }
    return new float[] {rotatedX + 0.5F, y, rotatedZ + 0.5F};
  }

  private static float[] rotateNormalY(float x, float y, float z, int degrees) {
    int normalized = Math.floorMod(degrees, 360);
    if (normalized == 0) {
      return new float[] {x, y, z};
    }
    switch (normalized) {
      case 90:
        return new float[] {-z, y, x};
      case 180:
        return new float[] {-x, y, -z};
      case 270:
        return new float[] {z, y, -x};
      default:
        return new float[] {x, y, z};
    }
  }
}

