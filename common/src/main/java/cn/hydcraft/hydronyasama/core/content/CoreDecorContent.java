package cn.hydcraft.hydronyasama.core.content;

import cn.hydcraft.hydronyasama.core.registry.ContentId;
import cn.hydcraft.hydronyasama.core.registry.ContentRegistrar;

public final class CoreDecorContent {

    private static final String GROUP = "core";

    private CoreDecorContent() {
    }

    public static void register(ContentRegistrar registrar) {
        registerSimple(registrar, "nsb_logo", "rock", 0);
        registerSimple(registrar, "nsb_sign", "glass", 15);
        registerSimple(registrar, "nsc_logo", "rock", 0);
        registerSimple(registrar, "nsdn_logo", "rock", 0);
        registerSimple(registrar, "nse_logo", "rock", 0);
        registerSimple(registrar, "nse_sign", "glass", 15);
        registerSimple(registrar, "nso_logo", "rock", 0);
        registerSimple(registrar, "nso_sign", "glass", 15);
        registerSimple(registrar, "nsr_logo", "rock", 0);
        registerSimple(registrar, "nsr_sign", "glass", 15);
        registerSimple(registrar, "nst_logo", "rock", 0);
        registerSimple(registrar, "nst_sign", "glass", 15);
    }

    private static void registerSimple(ContentRegistrar registrar, String idPath, String material, int lightLevel) {
        ContentId id = ContentId.of("hydronyasama", idPath);
        registrar.registerBlock(new ContentRegistrar.BlockDefinition(
                id,
                GROUP,
                "simple_glass_block".equals(lightLevel > 0 ? "simple_glass_block" : "simple_block") ? "simple_glass_block" : "simple_block",
                material,
                idPath,
                lightLevel,
                null
        ));
        registrar.registerItem(new ContentRegistrar.ItemDefinition(
                id,
                GROUP,
                "block_item",
                id
        ));
    }
}

