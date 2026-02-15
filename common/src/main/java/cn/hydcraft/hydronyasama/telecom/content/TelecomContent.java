package cn.hydcraft.hydronyasama.telecom.content;

import cn.hydcraft.hydronyasama.core.registry.ContentId;
import cn.hydcraft.hydronyasama.core.registry.ContentRegistrar;
import java.util.Arrays;
import java.util.List;

public final class TelecomContent {

    private static final String GROUP = "telecom";

    private static final List<String> BLOCK_IDS = Arrays.asList(
            "nspga_t0c0i8o8r0",
            "nspga_t4c4i8o8r0",
            "nspga_f011nhv1",
            "nspga_f211nhv1",
            "nspga_f344nhv1",
            "nsasm_box",
            "signal_box",
            "signal_box_sender",
            "signal_box_getter",
            "tri_state_signal_box",
            "signal_box_input",
            "signal_box_output",
            "signal_box_rx",
            "signal_box_tx",
            "rs_latch",
            "timer",
            "delayer"
    );

    private TelecomContent() {
    }

    public static void register(ContentRegistrar registrar) {
        for (String idPath : BLOCK_IDS) {
            registerSimpleBlock(registrar, idPath, "iron", 0);
        }
        registerToolItem(registrar, "connector", "connector_tool");
        registerToolItem(registrar, "dev_editor", "dev_editor_tool");
        registerToolItem(registrar, "ngtablet", "ngtablet_tool");
        registerToolItem(registrar, "nyagame_mr", "simple_item");
    }

    private static void registerSimpleBlock(
            ContentRegistrar registrar,
            String idPath,
            String material,
            int lightLevel
    ) {
        ContentId id = ContentId.of("hydronyasama", idPath);
        String kind = lightLevel > 0 ? "simple_glass_block" : "simple_block";
        registrar.registerBlock(new ContentRegistrar.BlockDefinition(
                id,
                GROUP,
                kind,
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

    private static void registerToolItem(ContentRegistrar registrar, String idPath, String kind) {
        registrar.registerItem(new ContentRegistrar.ItemDefinition(
                ContentId.of("hydronyasama", idPath),
                GROUP,
                kind,
                null
        ));
    }
}
