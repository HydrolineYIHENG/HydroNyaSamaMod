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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

final class ForgeContentRegistry {
  private static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(ForgeRegistries.BLOCKS, BeaconProviderMod.MOD_ID);
  private static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(ForgeRegistries.ITEMS, BeaconProviderMod.MOD_ID);
  private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
      DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, BeaconProviderMod.MOD_ID);
  private static final List<RegistryObject<Item>> CORE_ITEMS = new ArrayList<>();
  private static final List<RegistryObject<Item>> BUILDING_ITEMS = new ArrayList<>();
  private static final List<RegistryObject<Item>> ELECTRICITY_ITEMS = new ArrayList<>();
  private static final List<RegistryObject<Item>> OPTICS_ITEMS = new ArrayList<>();
  private static final List<RegistryObject<Item>> TELECOM_ITEMS = new ArrayList<>();
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
  private static RegistryObject<Block> telecomNodeBlock;
  private static RegistryObject<BlockEntityType<TelecomNodeBlockEntity>> telecomNodeBlockEntityType;
  private static RegistryObject<Item> probeItem;

  private ForgeContentRegistry() {}

  static void register(IEventBus modBus) {
    for (String id : LegacyContentIds.CORE_BLOCK_IDS) {
      registerStoneBlock(id, ForgeCreativeTabs.HYDRONYASAMA_CORE, CORE_ITEMS);
    }
    for (String id : LegacyContentIds.BUILDING_BLOCK_IDS) {
      registerStoneBlock(id, ForgeCreativeTabs.HYDRONYASAMA_BUILDING, BUILDING_ITEMS);
    }
    for (String id : LegacyContentIds.BUILDING_BATCH1_DERIVED_BLOCK_IDS) {
      registerDerivedBlock(id, ForgeCreativeTabs.HYDRONYASAMA_BUILDING, BUILDING_ITEMS);
    }
    for (String id : LegacyContentIds.BUILDING_BATCH2_DERIVED_BLOCK_IDS) {
      registerDerivedBlock(id, ForgeCreativeTabs.HYDRONYASAMA_BUILDING, BUILDING_ITEMS);
    }
    for (String id : LegacyContentIds.ELECTRICITY_BLOCK_IDS) {
      registerStoneBlock(id, ForgeCreativeTabs.HYDRONYASAMA_ELECTRICITY, ELECTRICITY_ITEMS);
    }
    for (String id : LegacyContentIds.OPTICS_BLOCK_IDS) {
      registerOpticsBlock(id, ForgeCreativeTabs.HYDRONYASAMA_OPTICS, OPTICS_ITEMS);
    }
    for (String id : LegacyContentIds.TELECOM_BLOCK_IDS) {
      registerTelecomBlock(id, ForgeCreativeTabs.HYDRONYASAMA_TELECOM, TELECOM_ITEMS);
    }
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
            "telecom_node",
            () ->
                new BlockItem(
                    telecomNodeBlock.get(),
                    new Item.Properties().tab(ForgeCreativeTabs.HYDRONYASAMA_CORE)));
    CORE_ITEMS.add(telecomNodeItem);
    telecomNodeBlockEntityType =
        BLOCK_ENTITY_TYPES.register(
            "telecom_node",
            () ->
                BlockEntityType.Builder.of(TelecomNodeBlockEntity::new, telecomNodeBlock.get())
                    .build(null));
    probeItem =
        ITEMS.register(
            "probe",
            () ->
                new ProbeItem(new Item.Properties().tab(ForgeCreativeTabs.HYDRONYASAMA_BUILDING)));
    BUILDING_ITEMS.add(probeItem);
    TELECOM_ITEMS.add(
        ITEMS.register(
            "connector",
            () ->
                new ConnectorItem(
                    new Item.Properties().tab(ForgeCreativeTabs.HYDRONYASAMA_TELECOM))));
    TELECOM_ITEMS.add(
        ITEMS.register(
            "dev_editor",
            () ->
                new DevEditorItem(
                    new Item.Properties().tab(ForgeCreativeTabs.HYDRONYASAMA_TELECOM))));
    TELECOM_ITEMS.add(
        ITEMS.register(
            "ngtablet",
            () ->
                new NgTabletItem(
                    new Item.Properties().tab(ForgeCreativeTabs.HYDRONYASAMA_TELECOM))));
    TELECOM_ITEMS.add(
        ITEMS.register(
            "nyagame_mr",
            () -> new Item(new Item.Properties().tab(ForgeCreativeTabs.HYDRONYASAMA_TELECOM))));
    BLOCKS.register(modBus);
    ITEMS.register(modBus);
    BLOCK_ENTITY_TYPES.register(modBus);
  }

  static ItemStack coreIcon() {
    return iconFor(CORE_ITEMS);
  }

  static ItemStack buildingIcon() {
    return iconFor(BUILDING_ITEMS);
  }

  static ItemStack electricityIcon() {
    return iconFor(ELECTRICITY_ITEMS);
  }

  static ItemStack opticsIcon() {
    return iconFor(OPTICS_ITEMS);
  }

  static ItemStack telecomIcon() {
    return iconFor(TELECOM_ITEMS);
  }

  static Item probeItem() {
    return probeItem == null ? Items.AIR : probeItem.get();
  }

  static BlockEntityType<TelecomNodeBlockEntity> telecomNodeBlockEntityType() {
    return telecomNodeBlockEntityType.get();
  }

  private static ItemStack iconFor(List<RegistryObject<Item>> items) {
    return items.isEmpty() ? new ItemStack(Items.BRICK) : new ItemStack(items.get(0).get());
  }

  private static void registerStoneBlock(
      String id,
      net.minecraft.world.item.CreativeModeTab tab,
      List<RegistryObject<Item>> tabItems) {
    RegistryObject<Block> block =
        BLOCKS.register(id, () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)));
    RegistryObject<Item> item =
        ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
    tabItems.add(item);
  }

  private static void registerDerivedBlock(
      String id,
      net.minecraft.world.item.CreativeModeTab tab,
      List<RegistryObject<Item>> tabItems) {
    RegistryObject<Block> block = BLOCKS.register(id, () -> createDerivedBlock(id));
    RegistryObject<Item> item =
        ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
    tabItems.add(item);
  }

  private static void registerOpticsBlock(
      String id,
      net.minecraft.world.item.CreativeModeTab tab,
      List<RegistryObject<Item>> tabItems) {
    RegistryObject<Block> block =
        BLOCKS.register(
            id,
            () ->
                OPTICS_OBJ_BLOCK_IDS.contains(id)
                    ? new ObjCollisionBlock(
                        BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion(), id)
                    : new Block(BlockBehaviour.Properties.copy(Blocks.STONE)));
    RegistryObject<Item> item =
        ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
    tabItems.add(item);
  }

  private static void registerTelecomBlock(
      String id,
      net.minecraft.world.item.CreativeModeTab tab,
      List<RegistryObject<Item>> tabItems) {
    RegistryObject<Block> block =
        BLOCKS.register(
            id,
            () ->
                new TelecomInteractiveBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                        .strength(2.0F)
                        .noOcclusion()));
    RegistryObject<Item> item =
        ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
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
      return new WoolCarpetBlock(DyeColor.WHITE, properties);
    }
    if (id.endsWith("_pane")) {
      return new IronBarsBlock(properties) {};
    }
    if (id.endsWith("_wall")) {
      return new WallBlock(properties);
    }
    if (id.endsWith("_fence_gate")) {
      return new FenceGateBlock(properties);
    }
    if (id.endsWith("_fence")) {
      return new FenceBlock(properties);
    }
    return new Block(properties);
  }
}
