package cn.hydcraft.hydronyasama.core.content;

import cn.hydcraft.hydronyasama.building.content.BuildingContent;
import cn.hydcraft.hydronyasama.core.registry.ContentRegistrar;
import cn.hydcraft.hydronyasama.electricity.content.ElectricityContent;
import cn.hydcraft.hydronyasama.optics.content.OpticsContent;
import cn.hydcraft.hydronyasama.telecom.content.TelecomContent;

public final class ModContent {

    public static final String MOD_GROUP_ID = "hydronyasama";

    private ModContent() {
    }

    public static void bootstrap(ContentRegistrar registrar) {
        CoreDecorContent.register(registrar);
        BuildingContent.register(registrar);
        ElectricityContent.register(registrar);
        OpticsContent.register(registrar);
        TelecomContent.register(registrar);
    }
}

