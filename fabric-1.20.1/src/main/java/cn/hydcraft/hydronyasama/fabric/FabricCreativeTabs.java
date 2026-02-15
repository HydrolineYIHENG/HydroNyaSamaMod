package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.BeaconProviderMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

final class FabricCreativeTabs {
  private static CreativeModeTab coreTab;
  private static CreativeModeTab buildingTab;

  private FabricCreativeTabs() {}

  static void init() {
    if (coreTab != null && buildingTab != null) {
      return;
    }
    FabricContentRegistry.init();
    coreTab =
        Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            new ResourceLocation(BeaconProviderMod.MOD_ID, "hydronyasama_core"),
            FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.hydronyasama_core"))
                .icon(FabricContentRegistry::coreIcon)
                .displayItems(
                    (parameters, entries) -> {
                      for (var item : FabricContentRegistry.coreTabItems()) {
                        entries.accept(item);
                      }
                    })
                .build());
    buildingTab =
        Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            new ResourceLocation(BeaconProviderMod.MOD_ID, "hydronyasama_building"),
            FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.hydronyasama_building"))
                .icon(FabricContentRegistry::buildingIcon)
                .displayItems(
                    (parameters, entries) -> {
                      for (var item : FabricContentRegistry.buildingTabItems()) {
                        entries.accept(item);
                      }
                    })
                .build());
  }
}
