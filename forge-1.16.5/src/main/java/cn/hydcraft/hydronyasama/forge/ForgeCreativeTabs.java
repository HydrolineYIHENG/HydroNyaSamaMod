package cn.hydcraft.hydronyasama.forge;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class ForgeCreativeTabs {
  static final CreativeModeTab HYDRONYASAMA_CORE =
      new CreativeModeTab(CreativeModeTab.TABS.length, "hydronyasama_core") {
        @Override
        public ItemStack makeIcon() {
          ItemStack stack = ForgeContentRegistry.coreIcon();
          return stack.isEmpty() ? new ItemStack(Items.BRICK) : stack;
        }
      };
  static final CreativeModeTab HYDRONYASAMA_BUILDING =
      new CreativeModeTab(CreativeModeTab.TABS.length + 1, "hydronyasama_building") {
        @Override
        public ItemStack makeIcon() {
          ItemStack stack = ForgeContentRegistry.buildingIcon();
          return stack.isEmpty() ? new ItemStack(Items.BRICK) : stack;
        }
      };

  private ForgeCreativeTabs() {}

  static void init() {
    HYDRONYASAMA_CORE.getRecipeFolderName();
    HYDRONYASAMA_BUILDING.getRecipeFolderName();
  }
}
