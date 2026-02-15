package cn.hydcraft.hydronyasama.fabric.content;

import cn.hydcraft.hydronyasama.core.content.ModContent;
import cn.hydcraft.hydronyasama.core.registry.ContentId;
import cn.hydcraft.hydronyasama.core.registry.ContentRegistrar;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

    private final Map<ContentId, Block> blockIndex = new HashMap<>();

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
        if (!"block_item".equals(definition.kind)) {
            throw new IllegalArgumentException("Unsupported item kind: " + definition.kind);
        }
        if (definition.blockId == null) {
            throw new IllegalArgumentException("blockId is required for block_item");
        }
        Block block = blockIndex.get(definition.blockId);
        if (block == null) {
            throw new IllegalStateException("Block not registered: " + definition.blockId);
        }
        ResourceLocation id = new ResourceLocation(definition.id.namespace(), definition.id.path());
        CreativeModeTab tab;
        if ("core".equals(definition.contentGroup)) {
            tab = TAB_CORE;
        } else if ("building".equals(definition.contentGroup)) {
            tab = TAB_BUILDING;
        } else {
            tab = TAB_CORE; // Fallback
        }
        Item item = new BlockItem(block, new Item.Properties().tab(tab));
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
            case "carpet":
            case "edge":
            case "roof":
            case "strip":
            case "v_slab":
            case "v_strip":
            case "vslab":
            case "vstrip":
                return new SlabBlock(props);
            case "stairs":
                if (baseBlock == null) baseBlock = Blocks.STONE;
                return new StairBlock(baseBlock.defaultBlockState(), props) {};
            case "wall":
                return new WallBlock(props);
            case "fence":
            case "railing":
                return new FenceBlock(props);
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
