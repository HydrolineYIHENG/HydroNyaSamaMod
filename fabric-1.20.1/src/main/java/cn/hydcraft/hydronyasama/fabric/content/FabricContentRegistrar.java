package cn.hydcraft.hydronyasama.fabric.content;

import cn.hydcraft.hydronyasama.core.content.ModContent;
import cn.hydcraft.hydronyasama.core.registry.ContentId;
import cn.hydcraft.hydronyasama.core.registry.ContentRegistrar;
import java.util.HashMap;
import java.util.Map;
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
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class FabricContentRegistrar implements ContentRegistrar {

    public static final ResourceKey<CreativeModeTab> TAB_CORE_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, new ResourceLocation(ModContent.MOD_GROUP_ID, "core"));
    public static final ResourceKey<CreativeModeTab> TAB_BUILDING_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, new ResourceLocation(ModContent.MOD_GROUP_ID, "building"));

    static {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_CORE_KEY, FabricItemGroup.builder()
                .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(ModContent.MOD_GROUP_ID, "nsdn_logo"))))
                .title(Component.translatable("itemGroup." + ModContent.MOD_GROUP_ID + ".core"))
                .build());
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_BUILDING_KEY, FabricItemGroup.builder()
                .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(ModContent.MOD_GROUP_ID, "anti_static_floor_block"))))
                .title(Component.translatable("itemGroup." + ModContent.MOD_GROUP_ID + ".building"))
                .build());
    }

    private final Map<ContentId, Block> blockIndex = new HashMap<>();

    @Override
    public Object registerBlock(BlockDefinition definition) {
        ResourceLocation id = new ResourceLocation(definition.id.namespace(), definition.id.path());
        Block block = createBlock(definition);
        Registry.register(BuiltInRegistries.BLOCK, id, block);
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
        Item item = new BlockItem(block, new Item.Properties());
        Registry.register(BuiltInRegistries.ITEM, id, item);
        
        ResourceKey<CreativeModeTab> tabKey;
        if ("core".equals(definition.contentGroup)) {
            tabKey = TAB_CORE_KEY;
        } else if ("building".equals(definition.contentGroup)) {
            tabKey = TAB_BUILDING_KEY;
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
                return new FenceGateBlock(props, net.minecraft.world.level.block.state.properties.WoodType.OAK); // 1.20 requires WoodType
            case "pane":
                return new IronBarsBlock(props) {};
            default:
                // Fallback for unknown kinds (like custom ones) to simple block to avoid crash
                return new Block(props);
        }
    }
}
