package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.BeaconProviderMod;
import cn.hydcraft.hydronyasama.content.LegacyContentIds;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.block.state.properties.WoodType;

final class FabricContentRegistry {
  private static final List<Item> CORE_TAB_ITEMS = new ArrayList<>();
  private static final List<Item> BUILDING_TAB_ITEMS = new ArrayList<>();
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
      registerStoneBlock(id, CORE_TAB_ITEMS);
    }
    for (String id : LegacyContentIds.BUILDING_BLOCK_IDS) {
      registerStoneBlock(id, BUILDING_TAB_ITEMS);
    }
    for (String id : LegacyContentIds.BUILDING_BATCH1_DERIVED_BLOCK_IDS) {
      registerDerivedBlock(id, BUILDING_TAB_ITEMS);
    }
    for (String id : LegacyContentIds.BUILDING_BATCH2_DERIVED_BLOCK_IDS) {
      registerDerivedBlock(id, BUILDING_TAB_ITEMS);
    }
    registerTelecomNode();
    registerStandaloneItem("probe", new ProbeItem(new Item.Properties()), BUILDING_TAB_ITEMS, true);
  }

  static List<Item> coreTabItems() {
    return Collections.unmodifiableList(CORE_TAB_ITEMS);
  }

  static List<Item> buildingTabItems() {
    return Collections.unmodifiableList(BUILDING_TAB_ITEMS);
  }

  static ItemStack coreIcon() {
    return iconFor(CORE_TAB_ITEMS);
  }

  static ItemStack buildingIcon() {
    return iconFor(BUILDING_TAB_ITEMS);
  }

  static Item probeItem() {
    return probeItem;
  }

  static BlockEntityType<TelecomNodeBlockEntity> telecomNodeBlockEntityType() {
    return telecomNodeBlockEntityType;
  }

  private static ItemStack iconFor(List<Item> items) {
    return items.isEmpty() ? new ItemStack(Items.BRICK) : new ItemStack(items.get(0));
  }

  private static void registerStoneBlock(String id, List<Item> tabItems) {
    registerBlock(id, new Block(BlockBehaviour.Properties.copy(Blocks.STONE)), tabItems);
  }

  private static void registerDerivedBlock(String id, List<Item> tabItems) {
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
      block = new FenceGateBlock(properties, WoodType.OAK);
    } else if (id.endsWith("_fence")) {
      block = new FenceBlock(properties);
    } else {
      block = new Block(properties);
    }
    registerBlock(id, block, tabItems);
  }

  private static void registerBlock(String id, Block block, List<Item> tabItems) {
    ResourceLocation key = new ResourceLocation(BeaconProviderMod.MOD_ID, id);
    Block registeredBlock = Registry.register(BuiltInRegistries.BLOCK, key, block);
    Item item =
        Registry.register(
            BuiltInRegistries.ITEM, key, new BlockItem(registeredBlock, new Item.Properties()));
    tabItems.add(item);
  }

  private static void registerTelecomNode() {
    ResourceLocation key = new ResourceLocation(BeaconProviderMod.MOD_ID, "telecom_node");
    telecomNodeBlock =
        Registry.register(
            BuiltInRegistries.BLOCK,
            key,
            new TelecomNodeBlock(
                BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2.0F).noOcclusion()));
    Item item =
        Registry.register(
            BuiltInRegistries.ITEM, key, new BlockItem(telecomNodeBlock, new Item.Properties()));
    CORE_TAB_ITEMS.add(item);
    telecomNodeBlockEntityType =
        Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            key,
            BlockEntityType.Builder.of(TelecomNodeBlockEntity::new, telecomNodeBlock).build(null));
  }

  private static void registerStandaloneItem(
      String id, Item item, List<Item> tabItems, boolean markAsProbe) {
    ResourceLocation key = new ResourceLocation(BeaconProviderMod.MOD_ID, id);
    Item registered = Registry.register(BuiltInRegistries.ITEM, key, item);
    tabItems.add(registered);
    if (markAsProbe) {
      probeItem = registered;
    }
  }
}
