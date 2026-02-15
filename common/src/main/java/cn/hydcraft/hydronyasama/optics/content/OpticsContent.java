package cn.hydcraft.hydronyasama.optics.content;

import cn.hydcraft.hydronyasama.core.registry.ContentId;
import cn.hydcraft.hydronyasama.core.registry.ContentRegistrar;
import java.util.Arrays;
import java.util.List;

public final class OpticsContent {

    private static final String GROUP = "optics";

    private static final List<String> BLOCK_IDS = Arrays.asList(
            "adsorption_lamp",
            "fluorescent_lamp",
            "spot_light",
            "holo_jet_rev",
            "led_plate",
            "platform_plate_full",
            "platform_plate_half",
            "station_lamp",
            "ad_board",
            "station_board",
            "text_wall",
            "text_wall_lit",
            "guide_board_np",
            "guide_board_sp",
            "guide_board_dp",
            "guide_board_np_lit",
            "guide_board_sp_lit",
            "guide_board_dp_lit",
            "light_beam_0",
            "light_beam_1",
            "pillar_head",
            "pillar_body",
            "adsorption_lamp_large",
            "adsorption_lamp_mono",
            "adsorption_lamp_multi",
            "fluorescent_light",
            "fluorescent_light_flock",
            "mosaic_light_mono",
            "mosaic_light_mono_small",
            "mosaic_light_multi",
            "mosaic_light_multi_small",
            "platform_light_full",
            "platform_light_half",
            "cuball_lamp"
    );

    private OpticsContent() {
    }

    public static void register(ContentRegistrar registrar) {
        for (String idPath : BLOCK_IDS) {
            int lightLevel = isLitBlock(idPath) ? 15 : 0;
            String material = lightLevel > 0 ? "glass" : "rock";
            registerSimpleBlock(registrar, idPath, material, lightLevel);
        }
    }

    private static boolean isLitBlock(String idPath) {
        return idPath.contains("light")
                || idPath.contains("lamp")
                || idPath.contains("beam")
                || idPath.endsWith("_lit")
                || "spot_light".equals(idPath)
                || "holo_jet_rev".equals(idPath)
                || "led_plate".equals(idPath);
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
}
