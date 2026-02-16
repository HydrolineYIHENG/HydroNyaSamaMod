package cn.hydcraft.hydronyasama.fabric.content;

import cn.hydcraft.hydronyasama.core.content.ModContent;
import cn.hydcraft.hydronyasama.core.registry.ContentId;
import cn.hydcraft.hydronyasama.core.registry.ContentRegistrar;
import cn.hydcraft.hydronyasama.fabric.ConnectorItem;
import cn.hydcraft.hydronyasama.fabric.DevEditorItem;
import cn.hydcraft.hydronyasama.fabric.NgTabletItem;
import cn.hydcraft.hydronyasama.fabric.LegacyEdgeBlock;
import cn.hydcraft.hydronyasama.fabric.LegacyRailingBlock;
import cn.hydcraft.hydronyasama.fabric.LegacyStripBlock;
import cn.hydcraft.hydronyasama.fabric.LegacyVSlabBlock;
import cn.hydcraft.hydronyasama.fabric.LegacyVStripBlock;
import cn.hydcraft.hydronyasama.fabric.ObjCollisionBlock;
import cn.hydcraft.hydronyasama.fabric.TelecomRenderBlock;
import cn.hydcraft.hydronyasama.fabric.TelecomRenderBlockEntity;
import cn.hydcraft.hydronyasama.fabric.ThinPanelBlock;
import cn.hydcraft.hydronyasama.fabric.ThinPanelGlassBlock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class FabricContentRegistrar implements ContentRegistrar {

    public static final ResourceKey<CreativeModeTab> TAB_CORE_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, new ResourceLocation(ModContent.MOD_GROUP_ID, "core"));
    public static final ResourceKey<CreativeModeTab> TAB_BUILDING_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, new ResourceLocation(ModContent.MOD_GROUP_ID, "building"));
    public static final ResourceKey<CreativeModeTab> TAB_ELECTRICITY_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, new ResourceLocation(ModContent.MOD_GROUP_ID, "electricity"));
    public static final ResourceKey<CreativeModeTab> TAB_OPTICS_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, new ResourceLocation(ModContent.MOD_GROUP_ID, "optics"));
    public static final ResourceKey<CreativeModeTab> TAB_TELECOM_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, new ResourceLocation(ModContent.MOD_GROUP_ID, "telecom"));

    static {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_CORE_KEY, FabricItemGroup.builder()
                .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(ModContent.MOD_GROUP_ID, "nsdn_logo"))))
                .title(Component.translatable("itemGroup." + ModContent.MOD_GROUP_ID + ".core"))
                .build());
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_BUILDING_KEY, FabricItemGroup.builder()
                .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(ModContent.MOD_GROUP_ID, "anti_static_floor_block"))))
                .title(Component.translatable("itemGroup." + ModContent.MOD_GROUP_ID + ".building"))
                .build());
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_ELECTRICITY_KEY, FabricItemGroup.builder()
                .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(ModContent.MOD_GROUP_ID, "catenary_long"))))
                .title(Component.translatable("itemGroup." + ModContent.MOD_GROUP_ID + ".electricity"))
                .build());
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_OPTICS_KEY, FabricItemGroup.builder()
                .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(ModContent.MOD_GROUP_ID, "spot_light"))))
                .title(Component.translatable("itemGroup." + ModContent.MOD_GROUP_ID + ".optics"))
                .build());
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_TELECOM_KEY, FabricItemGroup.builder()
                .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(ModContent.MOD_GROUP_ID, "signal_box"))))
                .title(Component.translatable("itemGroup." + ModContent.MOD_GROUP_ID + ".telecom"))
                .build());
    }

    private static final List<Block> TELECOM_RENDER_BLOCKS = new CopyOnWriteArrayList<>();
    private static final Set<String> OPTICS_OBJ_BLOCK_IDS = Set.of(
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
            "adsorption_lamp_multi");
    private static final Set<String> OPTICS_THIN_NO_OCCLUSION_BLOCK_IDS = Set.of(
            "adsorption_lamp",
            "adsorption_lamp_mid",
            "adsorption_lamp_up",
            "fluorescent_lamp",
            "spot_light",
            "text_wall",
            "text_wall_lit",
            "guide_board_np",
            "guide_board_sp",
            "guide_board_dp",
            "guide_board_np_lit",
            "guide_board_sp_lit",
            "guide_board_dp_lit");
    private static BlockEntityType<TelecomRenderBlockEntity> telecomRenderBlockEntityType;
    private final Map<ContentId, Block> blockIndex = new HashMap<>();

    @Override
    public Object registerBlock(BlockDefinition definition) {
        ResourceLocation id = new ResourceLocation(definition.id.namespace(), definition.id.path());
        Block block = createBlock(definition);
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        if ("telecom".equals(definition.contentGroup) && block instanceof TelecomRenderBlock) {
            TELECOM_RENDER_BLOCKS.add(block);
        }
        blockIndex.put(definition.id, block);
        return block;
    }

    public static void finalizeTelecomRenderRegistry() {
        if (telecomRenderBlockEntityType != null || TELECOM_RENDER_BLOCKS.isEmpty()) {
            return;
        }
        telecomRenderBlockEntityType =
            Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                new ResourceLocation(ModContent.MOD_GROUP_ID, "telecom_render"),
                BlockEntityType.Builder.of(
                        TelecomRenderBlockEntity::new,
                        TELECOM_RENDER_BLOCKS.toArray(new Block[0]))
                    .build(null));
    }

    public static BlockEntityType<TelecomRenderBlockEntity> telecomRenderBlockEntityType() {
        return telecomRenderBlockEntityType;
    }

    @Override
    public Object registerItem(ItemDefinition definition) {
        ResourceLocation id = new ResourceLocation(definition.id.namespace(), definition.id.path());
        Item item;
        if ("block_item".equals(definition.kind)) {
            if (definition.blockId == null) {
                throw new IllegalArgumentException("blockId is required for block_item");
            }
            Block block = blockIndex.get(definition.blockId);
            if (block == null) {
                throw new IllegalStateException("Block not registered: " + definition.blockId);
            }
            item = new BlockItem(block, new Item.Properties());
        } else if ("connector_tool".equals(definition.kind)) {
            item = new ConnectorItem(new Item.Properties());
        } else if ("dev_editor_tool".equals(definition.kind)) {
            item = new DevEditorItem(new Item.Properties());
        } else if ("ngtablet_tool".equals(definition.kind)) {
            item = new NgTabletItem(new Item.Properties());
        } else if ("simple_item".equals(definition.kind)) {
            item = new Item(new Item.Properties());
        } else {
            throw new IllegalArgumentException("Unsupported item kind: " + definition.kind);
        }
        Registry.register(BuiltInRegistries.ITEM, id, item);

        ResourceKey<CreativeModeTab> tabKey;
        if ("core".equals(definition.contentGroup)) {
            tabKey = TAB_CORE_KEY;
        } else if ("building".equals(definition.contentGroup)) {
            tabKey = TAB_BUILDING_KEY;
        } else if ("electricity".equals(definition.contentGroup)) {
            tabKey = TAB_ELECTRICITY_KEY;
        } else if ("optics".equals(definition.contentGroup)) {
            tabKey = TAB_OPTICS_KEY;
        } else if ("telecom".equals(definition.contentGroup)) {
            tabKey = TAB_TELECOM_KEY;
        } else {
            tabKey = TAB_CORE_KEY; // Fallback
        }
        ItemGroupEvents.modifyEntriesEvent(tabKey).register(content -> content.accept(item));
        return item;
    }

    private Block createBlock(BlockDefinition definition) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.copy(Blocks.STONE);
        if ("glass".equals(definition.material)) {
            props = BlockBehaviour.Properties.copy(Blocks.GLASS).lightLevel((state) -> definition.lightLevel);
        } else if ("iron".equals(definition.material)) {
            props = BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK);
        }

        Block baseBlock = null;
        if (definition.baseBlockId != null) {
            baseBlock = blockIndex.get(definition.baseBlockId);
            if (baseBlock != null) {
                props = BlockBehaviour.Properties.copy(baseBlock);
            }
        }

        if ("telecom".equals(definition.contentGroup) && "simple_block".equals(definition.kind)) {
            return new TelecomRenderBlock(props.noOcclusion());
        }
        if ("optics".equals(definition.contentGroup)
                && "simple_block".equals(definition.kind)
                && OPTICS_OBJ_BLOCK_IDS.contains(definition.id.path())) {
            return new ObjCollisionBlock(props.noOcclusion(), definition.id.path());
        }
        if ("optics".equals(definition.contentGroup)
                && OPTICS_THIN_NO_OCCLUSION_BLOCK_IDS.contains(definition.id.path())) {
            props =
                    props
                            .noOcclusion()
                            .isViewBlocking((state, world, pos) -> false)
                            .isSuffocating((state, world, pos) -> false)
                            .isRedstoneConductor((state, world, pos) -> false);
            if ("simple_glass_block".equals(definition.kind)) {
                return new ThinPanelGlassBlock(props);
            }
            return new ThinPanelBlock(props);
        }

        switch (definition.kind) {
            case "cube":
            case "simple_block":
                return new Block(props);
            case "simple_glass_block":
                return new GlassBlock(props);
            case "slab":
                return new SlabBlock(props);
            case "carpet":
                return new CarpetBlock(props);
            case "edge":
                return new LegacyEdgeBlock(props.noOcclusion());
            case "roof":
                return new LegacyRailingBlock(props.noOcclusion(), true);
            case "strip":
                if (baseBlock == null) baseBlock = Blocks.STONE;
                return new LegacyStripBlock(baseBlock.defaultBlockState(), props.noOcclusion());
            case "v_slab":
            case "v_strip":
            case "vslab":
                if (baseBlock == null) baseBlock = Blocks.STONE;
                return new LegacyVSlabBlock(baseBlock.defaultBlockState(), props.noOcclusion());
            case "vstrip":
                return new LegacyVStripBlock(props.noOcclusion());
            case "stairs":
                if (baseBlock == null) baseBlock = Blocks.STONE;
                return new StairBlock(baseBlock.defaultBlockState(), props) {};
            case "wall":
                return new WallBlock(props);
            case "fence":
            case "railing":
                return new LegacyRailingBlock(props.noOcclusion(), false);
            case "fence_gate":
                return new FenceGateBlock(props, net.minecraft.world.level.block.state.properties.WoodType.OAK); // 1.20 requires WoodType
            case "pane":
                return new IronBarsBlock(props) {};
            default:
                // Fallback for unknown kinds (like custom ones) to simple block to avoid crash
                return new Block(props);
        }
    }
}


