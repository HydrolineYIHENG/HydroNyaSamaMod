package cn.hydcraft.hydronyasama.core.content;

import cn.hydcraft.hydronyasama.building.content.BuildingContent;
import cn.hydcraft.hydronyasama.core.registry.ContentRegistrar;

public final class ModContent {

    public static final String MOD_GROUP_ID = "hydronyasama";

    private ModContent() {
    }

    public static void bootstrap(ContentRegistrar registrar) {
        CoreDecorContent.register(registrar);
        BuildingContent.register(registrar);
    }
}

