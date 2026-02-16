package cn.hydcraft.hydronyasama.electricity.content;

import cn.hydcraft.hydronyasama.core.registry.ContentId;
import cn.hydcraft.hydronyasama.core.registry.ContentRegistrar;
import java.util.Arrays;
import java.util.List;

public final class ElectricityContent {

  private static final String GROUP = "electricity";

  private static final List<String> BLOCK_IDS =
      Arrays.asList(
          "big_pillar",
          "catenary_long",
          "catenary_short",
          "catenary_insulator",
          "catenary_insulator_rev",
          "catenary_h",
          "catenary_old_base",
          "catenary_old_body",
          "catenary_old_head",
          "hv_danger_a",
          "hv_danger_b",
          "insulator_big",
          "insulator_mid",
          "insulator_small",
          "quad_head",
          "quad_shelf",
          "quad_small",
          "quad_tri_conv",
          "tri_head",
          "tri_shelf",
          "wire_endpoint",
          "wire_node",
          "catenary_endpoint",
          "catenary_node",
          "cable_endpoint",
          "cable_node",
          "pillar_endpoint",
          "pillar_node");

  private ElectricityContent() {}

  public static void register(ContentRegistrar registrar) {
    for (String idPath : BLOCK_IDS) {
      registerSimpleBlock(registrar, idPath, "iron", 0);
    }
  }

  private static void registerSimpleBlock(
      ContentRegistrar registrar, String idPath, String material, int lightLevel) {
    ContentId id = ContentId.of("hydronyasama", idPath);
    String kind = lightLevel > 0 ? "simple_glass_block" : "simple_block";
    registrar.registerBlock(
        new ContentRegistrar.BlockDefinition(id, GROUP, kind, material, idPath, lightLevel, null));
    registrar.registerItem(new ContentRegistrar.ItemDefinition(id, GROUP, "block_item", id));
  }
}
