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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.core.Registry;
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
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class FabricContentRegistrar implements ContentRegistrar {

    public static final CreativeModeTab TAB_CORE = FabricItemGroupBuilder.create(
            new ResourceLocation(ModContent.MOD_GROUP_ID, "core"))
            .icon(() -> new ItemStack(Registry.ITEM.get(new ResourceLocation(ModContent.MOD_GROUP_ID, "nsdn_logo"))))
            .build();

    public static final CreativeModeTab TAB_BUILDING = FabricItemGroupBuilder.create(
            new ResourceLocation(ModContent.MOD_GROUP_ID, "building"))
            .icon(() -> new ItemStack(Registry.ITEM.get(new ResourceLocation(ModContent.MOD_GROUP_ID, "anti_static_floor_block"))))
            .build();
    public static final CreativeModeTab TAB_ELECTRICITY = FabricItemGroupBuilder.create(
            new ResourceLocation(ModContent.MOD_GROUP_ID, "electricity"))
            .icon(() -> new ItemStack(Registry.ITEM.get(new ResourceLocation(ModContent.MOD_GROUP_ID, "catenary_long"))))
            .build();
    public static final CreativeModeTab TAB_OPTICS = FabricItemGroupBuilder.create(
            new ResourceLocation(ModContent.MOD_GROUP_ID, "optics"))
            .icon(() -> new ItemStack(Registry.ITEM.get(new ResourceLocation(ModContent.MOD_GROUP_ID, "spot_light"))))
            .build();
    public static final CreativeModeTab TAB_TELECOM = FabricItemGroupBuilder.create(
            new ResourceLocation(ModContent.MOD_GROUP_ID, "telecom"))
            .icon(() -> new ItemStack(Registry.ITEM.get(new ResourceLocation(ModContent.MOD_GROUP_ID, "signal_box"))))
            .build();

    private final Map<ContentId, Block> blockIndex = new HashMap<>();
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

    @Override
    public Object registerBlock(BlockDefinition definition) {
        ResourceLocation id = new ResourceLocation(definition.id.namespace(), definition.id.path());
        Block block = createBlock(definition);
        Registry.register(Registry.BLOCK, id, block);
        blockIndex.put(definition.id, block);
        return block;
    }

    @Override
    public Object registerItem(ItemDefinition definition) {
        CreativeModeTab tab;
        if ("core".equals(definition.contentGroup)) {
            tab = TAB_CORE;
        } else if ("building".equals(definition.contentGroup)) {
            tab = TAB_BUILDING;
        } else if ("electricity".equals(definition.contentGroup)) {
            tab = TAB_ELECTRICITY;
        } else if ("optics".equals(definition.contentGroup)) {
            tab = TAB_OPTICS;
        } else if ("telecom".equals(definition.contentGroup)) {
            tab = TAB_TELECOM;
        } else {
            tab = TAB_CORE; // Fallback
        }
        Item item;
        if ("block_item".equals(definition.kind)) {
            if (definition.blockId == null) {
                throw new IllegalArgumentException("blockId is required for block_item");
            }
            Block block = blockIndex.get(definition.blockId);
            if (block == null) {
                throw new IllegalStateException("Block not registered: " + definition.blockId);
            }
            item = new BlockItem(block, new Item.Properties().tab(tab));
        } else if ("connector_tool".equals(definition.kind)) {
            item = new ConnectorItem(new Item.Properties().tab(tab));
        } else if ("dev_editor_tool".equals(definition.kind)) {
            item = new DevEditorItem(new Item.Properties().tab(tab));
        } else if ("ngtablet_tool".equals(definition.kind)) {
            item = new NgTabletItem(new Item.Properties().tab(tab));
        } else if ("simple_item".equals(definition.kind)) {
            item = new Item(new Item.Properties().tab(tab));
        } else {
            throw new IllegalArgumentException("Unsupported item kind: " + definition.kind);
        }
        ResourceLocation id = new ResourceLocation(definition.id.namespace(), definition.id.path());
        Registry.register(Registry.ITEM, id, item);
        return item;
    }

    private Block createBlock(BlockDefinition definition) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.copy(Blocks.STONE);
        if ("glass".equals(definition.material)) {
            props = BlockBehaviour.Properties.copy(Blocks.GLASS).lightLevel((state) -> definition.lightLevel);
        } else if ("iron".equals(definition.material)) {
            props = BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK);
        }
        if ("optics".equals(definition.contentGroup)
                && "simple_block".equals(definition.kind)
                && OPTICS_OBJ_BLOCK_IDS.contains(definition.id.path())) {
            return new ObjCollisionBlock(props.noOcclusion(), definition.id.path());
        }
        
        Block baseBlock = null;
        if (definition.baseBlockId != null) {
            baseBlock = blockIndex.get(definition.baseBlockId);
            if (baseBlock != null) {
                props = BlockBehaviour.Properties.copy(baseBlock);
            }
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
                return new FenceGateBlock(props);
            case "pane":
                return new IronBarsBlock(props) {};
            default:
                // Fallback for unknown kinds (like custom ones) to simple block to avoid crash
                return new Block(props);
        }
    }
}


