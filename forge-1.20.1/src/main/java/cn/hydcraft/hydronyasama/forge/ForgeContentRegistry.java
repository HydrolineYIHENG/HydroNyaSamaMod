package cn.hydcraft.hydronyasama.forge;

import cn.hydcraft.hydronyasama.BeaconProviderMod;
import cn.hydcraft.hydronyasama.content.LegacyContentIds;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

final class ForgeContentRegistry {
  private static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(ForgeRegistries.BLOCKS, BeaconProviderMod.MOD_ID);
  private static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(ForgeRegistries.ITEMS, BeaconProviderMod.MOD_ID);
  private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
      DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BeaconProviderMod.MOD_ID);
  private static final List<RegistryObject<Item>> CORE_TAB_ITEMS = new ArrayList<>();
  private static final List<RegistryObject<Item>> BUILDING_TAB_ITEMS = new ArrayList<>();
  private static final List<RegistryObject<Item>> ELECTRICITY_TAB_ITEMS = new ArrayList<>();
  private static final List<RegistryObject<Item>> OPTICS_TAB_ITEMS = new ArrayList<>();
  private static final List<RegistryObject<Item>> TELECOM_TAB_ITEMS = new ArrayList<>();
  private static final List<RegistryObject<Block>> TELECOM_RENDER_BLOCKS = new ArrayList<>();
  private static final List<RegistryObject<Block>> OPTICS_TEXT_BLOCKS = new ArrayList<>();
  private static final Set<String> OPTICS_OBJ_BLOCK_IDS =
      Collections.unmodifiableSet(
          new HashSet<>(
              Arrays.asList(
                  "ad_board",
                  "cuball_lamp",
                  "fluorescent_light",
                  "fluorescent_light_flock",
                  "holo_jet_rev",
                  "led_plate",
                  "mosaic_light_mono",
                  "mosaic_light_mono_small",
                  "mosaic_light_multi",
                  "mosaic_light_multi_small",
                  "pillar_body",
                  "pillar_head",
                  "platform_light_full",
                  "platform_light_half",
                  "platform_plate_full",
                  "platform_plate_half",
                  "station_board",
                  "station_lamp",
                  "adsorption_lamp_large",
                  "adsorption_lamp_mono",
                  "adsorption_lamp_multi")));
  private static final Set<String> OPTICS_TEXT_BLOCK_IDS =
      Collections.unmodifiableSet(
          new HashSet<>(
              Arrays.asList(
                  "text_wall",
                  "text_wall_lit",
                  "guide_board_np",
                  "guide_board_sp",
                  "guide_board_dp",
                  "guide_board_np_lit",
                  "guide_board_sp_lit",
                  "guide_board_dp_lit")));
  private static RegistryObject<Block> telecomNodeBlock;
  private static RegistryObject<BlockEntityType<TelecomNodeBlockEntity>> telecomNodeBlockEntityType;
  private static RegistryObject<BlockEntityType<TelecomRenderBlockEntity>>
      telecomRenderBlockEntityType;
  private static RegistryObject<BlockEntityType<OpticsTextBlockEntity>> opticsTextBlockEntityType;
  private static RegistryObject<Item> probeItem;

  private ForgeContentRegistry() {}

  static void register(IEventBus modBus) {
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
    for (String id : LegacyContentIds.ELECTRICITY_BLOCK_IDS) {
      registerStoneBlock(id, ELECTRICITY_TAB_ITEMS);
    }
    for (String id : LegacyContentIds.OPTICS_BLOCK_IDS) {
      registerOpticsBlock(id, OPTICS_TAB_ITEMS);
    }
    for (String id : LegacyContentIds.TELECOM_BLOCK_IDS) {
      registerTelecomBlock(id, TELECOM_TAB_ITEMS);
    }
    telecomRenderBlockEntityType =
        BLOCK_ENTITY_TYPES.register(
            "telecom_render",
            () ->
                BlockEntityType.Builder.of(
                        TelecomRenderBlockEntity::new,
                        TELECOM_RENDER_BLOCKS.stream()
                            .map(RegistryObject::get)
                            .toArray(Block[]::new))
                    .build(null));
    opticsTextBlockEntityType =
        BLOCK_ENTITY_TYPES.register(
            "optics_text",
            () ->
                BlockEntityType.Builder.of(
                        OpticsTextBlockEntity::new,
                        OPTICS_TEXT_BLOCKS.stream().map(RegistryObject::get).toArray(Block[]::new))
                    .build(null));
    telecomNodeBlock =
        BLOCKS.register(
            "telecom_node",
            () ->
                new TelecomNodeBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                        .strength(2.0F)
                        .noOcclusion()));
    RegistryObject<Item> telecomNodeItem =
        ITEMS.register(
            "telecom_node", () -> new BlockItem(telecomNodeBlock.get(), new Item.Properties()));
    CORE_TAB_ITEMS.add(telecomNodeItem);
    telecomNodeBlockEntityType =
        BLOCK_ENTITY_TYPES.register(
            "telecom_node",
            () ->
                BlockEntityType.Builder.of(TelecomNodeBlockEntity::new, telecomNodeBlock.get())
                    .build(null));
    probeItem = ITEMS.register("probe", () -> new ProbeItem(new Item.Properties()));
    BUILDING_TAB_ITEMS.add(probeItem);
    TELECOM_TAB_ITEMS.add(
        ITEMS.register("connector", () -> new ConnectorItem(new Item.Properties())));
    TELECOM_TAB_ITEMS.add(
        ITEMS.register("dev_editor", () -> new DevEditorItem(new Item.Properties())));
    TELECOM_TAB_ITEMS.add(
        ITEMS.register("ngtablet", () -> new NgTabletItem(new Item.Properties())));
    TELECOM_TAB_ITEMS.add(ITEMS.register("nyagame_mr", () -> new Item(new Item.Properties())));
    BLOCKS.register(modBus);
    ITEMS.register(modBus);
    BLOCK_ENTITY_TYPES.register(modBus);
  }

  static List<RegistryObject<Item>> coreTabItems() {
    return Collections.unmodifiableList(CORE_TAB_ITEMS);
  }

  static List<RegistryObject<Item>> buildingTabItems() {
    return Collections.unmodifiableList(BUILDING_TAB_ITEMS);
  }

  static List<RegistryObject<Item>> electricityTabItems() {
    return Collections.unmodifiableList(ELECTRICITY_TAB_ITEMS);
  }

  static List<RegistryObject<Item>> opticsTabItems() {
    return Collections.unmodifiableList(OPTICS_TAB_ITEMS);
  }

  static List<RegistryObject<Item>> telecomTabItems() {
    return Collections.unmodifiableList(TELECOM_TAB_ITEMS);
  }

  static Item coreIconItem() {
    return CORE_TAB_ITEMS.isEmpty() ? Items.BRICK : CORE_TAB_ITEMS.get(0).get();
  }

  static Item buildingIconItem() {
    return BUILDING_TAB_ITEMS.isEmpty() ? Items.BRICK : BUILDING_TAB_ITEMS.get(0).get();
  }

  static Item electricityIconItem() {
    return ELECTRICITY_TAB_ITEMS.isEmpty() ? Items.BRICK : ELECTRICITY_TAB_ITEMS.get(0).get();
  }

  static Item opticsIconItem() {
    return OPTICS_TAB_ITEMS.isEmpty() ? Items.BRICK : OPTICS_TAB_ITEMS.get(0).get();
  }

  static Item telecomIconItem() {
    return TELECOM_TAB_ITEMS.isEmpty() ? Items.BRICK : TELECOM_TAB_ITEMS.get(0).get();
  }

  static Item probeItem() {
    return probeItem == null ? Items.AIR : probeItem.get();
  }

  static BlockEntityType<TelecomNodeBlockEntity> telecomNodeBlockEntityType() {
    return telecomNodeBlockEntityType.get();
  }

  static BlockEntityType<TelecomRenderBlockEntity> telecomRenderBlockEntityType() {
    return telecomRenderBlockEntityType.get();
  }

  static BlockEntityType<OpticsTextBlockEntity> opticsTextBlockEntityType() {
    return opticsTextBlockEntityType.get();
  }

  private static void registerStoneBlock(String id, List<RegistryObject<Item>> tabItems) {
    RegistryObject<Block> block =
        BLOCKS.register(id, () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)));
    RegistryObject<Item> item =
        ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    tabItems.add(item);
  }

  private static void registerDerivedBlock(String id, List<RegistryObject<Item>> tabItems) {
    RegistryObject<Block> block = BLOCKS.register(id, () -> createDerivedBlock(id));
    RegistryObject<Item> item =
        ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    tabItems.add(item);
  }

  private static void registerTelecomBlock(String id, List<RegistryObject<Item>> tabItems) {
    RegistryObject<Block> block =
        BLOCKS.register(
            id,
            () ->
                new TelecomRenderBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                        .strength(2.0F)
                        .noOcclusion()));
    RegistryObject<Item> item =
        ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    TELECOM_RENDER_BLOCKS.add(block);
    tabItems.add(item);
  }

  private static void registerOpticsBlock(String id, List<RegistryObject<Item>> tabItems) {
    RegistryObject<Block> block =
        BLOCKS.register(
            id,
            () ->
                OPTICS_TEXT_BLOCK_IDS.contains(id)
                    ? new OpticsTextPanelBlock(
                        BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion())
                    : OPTICS_OBJ_BLOCK_IDS.contains(id)
                        ? new ObjCollisionBlock(
                            BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion(), id)
                        : new Block(BlockBehaviour.Properties.copy(Blocks.STONE)));
    RegistryObject<Item> item =
        ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    if (OPTICS_TEXT_BLOCK_IDS.contains(id)) {
      OPTICS_TEXT_BLOCKS.add(block);
    }
    tabItems.add(item);
  }

  private static Block createDerivedBlock(String id) {
    BlockBehaviour.Properties properties = BlockBehaviour.Properties.copy(Blocks.STONE);
    if (id.endsWith("_stairs")) {
      return new StairBlock(Blocks.STONE.defaultBlockState(), properties) {};
    }
    if (id.endsWith("_strip")) {
      return new LegacyStripBlock(Blocks.STONE.defaultBlockState(), properties.noOcclusion());
    }
    if (id.endsWith("_vslab")) {
      return new LegacyVSlabBlock(Blocks.STONE.defaultBlockState(), properties.noOcclusion());
    }
    if (id.endsWith("_vstrip")) {
      return new LegacyVStripBlock(properties.noOcclusion());
    }
    if (id.endsWith("_edge")) {
      return new LegacyEdgeBlock(properties.noOcclusion());
    }
    if (id.endsWith("_railing")) {
      return new LegacyRailingBlock(properties.noOcclusion(), false);
    }
    if (id.endsWith("_roof")) {
      return new LegacyRailingBlock(properties.noOcclusion(), true);
    }
    if (id.endsWith("_slab")) {
      return new SlabBlock(properties);
    }
    if (id.endsWith("_carpet")) {
      return new CarpetBlock(properties);
    }
    if (id.endsWith("_pane")) {
      return new IronBarsBlock(properties);
    }
    if (id.endsWith("_wall")) {
      return new WallBlock(properties);
    }
    if (id.endsWith("_fence_gate")) {
      return new FenceGateBlock(properties, WoodType.OAK);
    }
    if (id.endsWith("_fence")) {
      return new FenceBlock(properties);
    }
    return new Block(properties);
  }
}
