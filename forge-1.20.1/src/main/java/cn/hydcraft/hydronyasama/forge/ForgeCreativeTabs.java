package cn.hydcraft.hydronyasama.forge;

import cn.hydcraft.hydronyasama.BeaconProviderMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

final class ForgeCreativeTabs {
  private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BeaconProviderMod.MOD_ID);

  static final RegistryObject<CreativeModeTab> HYDRONYASAMA_CORE =
      CREATIVE_TABS.register(
          "hydronyasama_core",
          () ->
              CreativeModeTab.builder()
                  .title(Component.translatable("itemGroup.hydronyasama_core"))
                  .icon(() -> new ItemStack(ForgeContentRegistry.coreIconItem()))
                  .displayItems(
                      (parameters, output) -> {
                        for (var item : ForgeContentRegistry.coreTabItems()) {
                          output.accept(item.get());
                        }
                      })
                  .build());

  static final RegistryObject<CreativeModeTab> HYDRONYASAMA_BUILDING =
      CREATIVE_TABS.register(
          "hydronyasama_building",
          () ->
              CreativeModeTab.builder()
                  .title(Component.translatable("itemGroup.hydronyasama_building"))
                  .icon(() -> new ItemStack(ForgeContentRegistry.buildingIconItem()))
                  .displayItems(
                      (parameters, output) -> {
                        for (var item : ForgeContentRegistry.buildingTabItems()) {
                          output.accept(item.get());
                        }
                      })
                  .build());

  static final RegistryObject<CreativeModeTab> HYDRONYASAMA_ELECTRICITY =
      CREATIVE_TABS.register(
          "hydronyasama_electricity",
          () ->
              CreativeModeTab.builder()
                  .title(Component.translatable("itemGroup.hydronyasama_electricity"))
                  .icon(() -> new ItemStack(ForgeContentRegistry.electricityIconItem()))
                  .displayItems(
                      (parameters, output) -> {
                        for (var item : ForgeContentRegistry.electricityTabItems()) {
                          output.accept(item.get());
                        }
                      })
                  .build());

  static final RegistryObject<CreativeModeTab> HYDRONYASAMA_OPTICS =
      CREATIVE_TABS.register(
          "hydronyasama_optics",
          () ->
              CreativeModeTab.builder()
                  .title(Component.translatable("itemGroup.hydronyasama_optics"))
                  .icon(() -> new ItemStack(ForgeContentRegistry.opticsIconItem()))
                  .displayItems(
                      (parameters, output) -> {
                        for (var item : ForgeContentRegistry.opticsTabItems()) {
                          output.accept(item.get());
                        }
                      })
                  .build());

  static final RegistryObject<CreativeModeTab> HYDRONYASAMA_TELECOM =
      CREATIVE_TABS.register(
          "hydronyasama_telecom",
          () ->
              CreativeModeTab.builder()
                  .title(Component.translatable("itemGroup.hydronyasama_telecom"))
                  .icon(() -> new ItemStack(ForgeContentRegistry.telecomIconItem()))
                  .displayItems(
                      (parameters, output) -> {
                        for (var item : ForgeContentRegistry.telecomTabItems()) {
                          output.accept(item.get());
                        }
                      })
                  .build());

  private ForgeCreativeTabs() {}

  static void register(IEventBus modBus) {
    CREATIVE_TABS.register(modBus);
    HYDRONYASAMA_CORE.getId();
    HYDRONYASAMA_BUILDING.getId();
    HYDRONYASAMA_ELECTRICITY.getId();
    HYDRONYASAMA_OPTICS.getId();
    HYDRONYASAMA_TELECOM.getId();
  }
}
