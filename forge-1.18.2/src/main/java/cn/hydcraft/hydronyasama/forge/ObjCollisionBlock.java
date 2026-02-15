package cn.hydcraft.hydronyasama.forge;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Collision shape driven by OBJ vertex bounds for optics OBJ blocks. */
public final class ObjCollisionBlock extends Block {
  private static final Map<String, String> MODEL_BY_BLOCK_ID = createModelMap();
  private static final Map<String, VoxelShape> SHAPE_CACHE = new ConcurrentHashMap<>();

  private final VoxelShape shape;

  public ObjCollisionBlock(BlockBehaviour.Properties properties, String blockId) {
    super(properties);
    String modelFile = MODEL_BY_BLOCK_ID.get(blockId);
    this.shape =
        modelFile == null ? Shapes.block() : SHAPE_CACHE.computeIfAbsent(modelFile, ObjCollisionBlock::loadShapeFromObj);
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return shape;
  }

  @Override
  public VoxelShape getCollisionShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return shape;
  }

  @Override
  public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
    return shape;
  }

  private static VoxelShape loadShapeFromObj(String modelFile) {
    String path = "assets/hydronyasama/models/blocks/" + modelFile;
    InputStream stream = ObjCollisionBlock.class.getClassLoader().getResourceAsStream(path);
    if (stream == null) {
      return Shapes.block();
    }

    double minX = Double.POSITIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY;
    double minZ = Double.POSITIVE_INFINITY;
    double maxX = Double.NEGATIVE_INFINITY;
    double maxY = Double.NEGATIVE_INFINITY;
    double maxZ = Double.NEGATIVE_INFINITY;

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.startsWith("v ")) {
          continue;
        }
        String[] parts = line.trim().split("\\s+");
        if (parts.length < 4) {
          continue;
        }
        double x = normalizeToBlockCoord(parse(parts[1]));
        double y = normalizeToBlockCoord(parse(parts[2]));
        double z = normalizeToBlockCoord(parse(parts[3]));

        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        minZ = Math.min(minZ, z);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
        maxZ = Math.max(maxZ, z);
      }
    } catch (Exception ignored) {
      return Shapes.block();
    }

    if (!Double.isFinite(minX)
        || !Double.isFinite(minY)
        || !Double.isFinite(minZ)
        || !Double.isFinite(maxX)
        || !Double.isFinite(maxY)
        || !Double.isFinite(maxZ)) {
      return Shapes.block();
    }
    if (maxX - minX < 1.0E-4D || maxY - minY < 1.0E-4D || maxZ - minZ < 1.0E-4D) {
      return Shapes.block();
    }

    return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
  }

  private static double parse(String raw) {
    return Double.parseDouble(raw);
  }

  private static double normalizeToBlockCoord(double objCoord) {
    double value = objCoord / 16.0D + 0.5D;
    return clamp(value, 0.0D, 1.0D);
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  private static Map<String, String> createModelMap() {
    Map<String, String> map = new HashMap<>();
    map.put("ad_board", "ad_board_base.obj");
    map.put("cuball_lamp", "cuball_lamp_base.obj");
    map.put("fluorescent_light", "fluorescent_light_base.obj");
    map.put("fluorescent_light_flock", "fluorescent_light_flock_base.obj");
    map.put("holo_jet_rev", "holo_jet_rev_base.obj");
    map.put("led_plate", "led_plate_base.obj");
    map.put("mosaic_light_mono", "mosaic_light_mono_base.obj");
    map.put("mosaic_light_mono_small", "mosaic_light_mono_small_base.obj");
    map.put("mosaic_light_multi", "mosaic_light_multi_base.obj");
    map.put("mosaic_light_multi_small", "mosaic_light_multi_small_base.obj");
    map.put("pillar_body", "pillar_body_base.obj");
    map.put("pillar_head", "pillar_head_base.obj");
    map.put("platform_light_full", "platform_light_full_base.obj");
    map.put("platform_light_half", "platform_light_half_base.obj");
    map.put("platform_plate_full", "platform_light_full_base.obj");
    map.put("platform_plate_half", "platform_light_half_base.obj");
    map.put("station_board", "station_board.obj");
    map.put("station_lamp", "station_lamp_mid.obj");
    map.put("adsorption_lamp_large", "adsorption_lamp_large_base.obj");
    map.put("adsorption_lamp_mono", "adsorption_lamp_mono_base.obj");
    map.put("adsorption_lamp_multi", "adsorption_lamp_multi_base.obj");
    return Collections.unmodifiableMap(map);
  }
}
