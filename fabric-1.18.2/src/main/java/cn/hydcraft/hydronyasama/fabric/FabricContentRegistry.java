package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.BeaconProviderMod;
import cn.hydcraft.hydronyasama.content.LegacyContentIds;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

final class FabricContentRegistry {
  private static Block telecomNodeBlock = Blocks.AIR;
  private static BlockEntityType<TelecomNodeBlockEntity> telecomNodeBlockEntityType;
  private static Item probeItem = Items.AIR;
  private static boolean initialized;

  private FabricContentRegistry() {}

  static void init() {
    if (initialized) {
      return;
    }
    initialized = true;

    for (String id : LegacyContentIds.CORE_BLOCK_IDS) {
      registerStoneBlock(id, FabricCreativeTabs.HYDRONYASAMA_CORE);
    }
    for (String id : LegacyContentIds.BUILDING_BLOCK_IDS) {
      registerStoneBlock(id, FabricCreativeTabs.HYDRONYASAMA_BUILDING);
    }
    for (String id : LegacyContentIds.BUILDING_BATCH1_DERIVED_BLOCK_IDS) {
      registerDerivedBlock(id, FabricCreativeTabs.HYDRONYASAMA_BUILDING);
    }
    for (String id : LegacyContentIds.BUILDING_BATCH2_DERIVED_BLOCK_IDS) {
      registerDerivedBlock(id, FabricCreativeTabs.HYDRONYASAMA_BUILDING);
    }
    registerTelecomNode();
    registerStandaloneItem(
        "probe",
        new ProbeItem(new Item.Properties().tab(FabricCreativeTabs.HYDRONYASAMA_BUILDING)),
        true);
  }

  static ItemStack coreIcon() {
    return iconFor(LegacyContentIds.CORE_BLOCK_IDS.get(0));
  }

  static ItemStack buildingIcon() {
    return iconFor(LegacyContentIds.BUILDING_BLOCK_IDS.get(0));
  }

  static Item probeItem() {
    return probeItem;
  }

  static BlockEntityType<TelecomNodeBlockEntity> telecomNodeBlockEntityType() {
    return telecomNodeBlockEntityType;
  }

  private static ItemStack iconFor(String id) {
    Item item = Registry.ITEM.get(new ResourceLocation(BeaconProviderMod.MOD_ID, id));
    return item == null || item == Items.AIR ? new ItemStack(Items.BRICK) : new ItemStack(item);
  }

  private static void registerStoneBlock(String id, net.minecraft.world.item.CreativeModeTab tab) {
    registerBlock(id, new Block(BlockBehaviour.Properties.copy(Blocks.STONE)), tab);
  }

  private static void registerDerivedBlock(
      String id, net.minecraft.world.item.CreativeModeTab tab) {
    BlockBehaviour.Properties properties = BlockBehaviour.Properties.copy(Blocks.STONE);
    Block block;
    if (id.endsWith("_stairs")) {
      block = new StairBlock(Blocks.STONE.defaultBlockState(), properties) {};
    } else if (id.endsWith("_strip")) {
      block = new LegacyStripBlock(Blocks.STONE.defaultBlockState(), properties.noOcclusion());
    } else if (id.endsWith("_vslab")) {
      block = new LegacyVSlabBlock(Blocks.STONE.defaultBlockState(), properties.noOcclusion());
    } else if (id.endsWith("_vstrip")) {
      block = new LegacyVStripBlock(properties.noOcclusion());
    } else if (id.endsWith("_edge")) {
      block = new LegacyEdgeBlock(properties.noOcclusion());
    } else if (id.endsWith("_railing")) {
      block = new LegacyRailingBlock(properties.noOcclusion(), false);
    } else if (id.endsWith("_roof")) {
      block = new LegacyRailingBlock(properties.noOcclusion(), true);
    } else if (id.endsWith("_slab")) {
      block = new SlabBlock(properties);
    } else if (id.endsWith("_carpet")) {
      block = new CarpetBlock(properties);
    } else if (id.endsWith("_pane")) {
      block = new IronBarsBlock(properties);
    } else if (id.endsWith("_wall")) {
      block = new WallBlock(properties);
    } else if (id.endsWith("_fence_gate")) {
      block = new FenceGateBlock(properties);
    } else if (id.endsWith("_fence")) {
      block = new FenceBlock(properties);
    } else {
      block = new Block(properties);
    }
    registerBlock(id, block, tab);
  }

  private static void registerBlock(
      String id, Block block, net.minecraft.world.item.CreativeModeTab tab) {
    ResourceLocation key = new ResourceLocation(BeaconProviderMod.MOD_ID, id);
    Block registeredBlock = Registry.register(Registry.BLOCK, key, block);
    Registry.register(
        Registry.ITEM, key, new BlockItem(registeredBlock, new Item.Properties().tab(tab)));
  }

  private static void registerTelecomNode() {
    ResourceLocation key = new ResourceLocation(BeaconProviderMod.MOD_ID, "telecom_node");
    telecomNodeBlock =
        Registry.register(
            Registry.BLOCK,
            key,
            new TelecomNodeBlock(
                BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2.0F).noOcclusion()));
    Registry.register(
        Registry.ITEM,
        key,
        new BlockItem(
            telecomNodeBlock, new Item.Properties().tab(FabricCreativeTabs.HYDRONYASAMA_CORE)));
    telecomNodeBlockEntityType =
        Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            key,
            FabricBlockEntityTypeBuilder.create(TelecomNodeBlockEntity::new, telecomNodeBlock)
                .build(null));
  }

  private static void registerStandaloneItem(String id, Item item, boolean markAsProbe) {
    ResourceLocation key = new ResourceLocation(BeaconProviderMod.MOD_ID, id);
    Item registered = Registry.register(Registry.ITEM, key, item);
    if (markAsProbe) {
      probeItem = registered;
    }
  }
}
