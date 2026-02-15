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
  static final CreativeModeTab HYDRONYASAMA_ELECTRICITY =
      new CreativeModeTab(CreativeModeTab.TABS.length + 2, "hydronyasama_electricity") {
        @Override
        public ItemStack makeIcon() {
          ItemStack stack = ForgeContentRegistry.electricityIcon();
          return stack.isEmpty() ? new ItemStack(Items.BRICK) : stack;
        }
      };
  static final CreativeModeTab HYDRONYASAMA_OPTICS =
      new CreativeModeTab(CreativeModeTab.TABS.length + 3, "hydronyasama_optics") {
        @Override
        public ItemStack makeIcon() {
          ItemStack stack = ForgeContentRegistry.opticsIcon();
          return stack.isEmpty() ? new ItemStack(Items.BRICK) : stack;
        }
      };
  static final CreativeModeTab HYDRONYASAMA_TELECOM =
      new CreativeModeTab(CreativeModeTab.TABS.length + 4, "hydronyasama_telecom") {
        @Override
        public ItemStack makeIcon() {
          ItemStack stack = ForgeContentRegistry.telecomIcon();
          return stack.isEmpty() ? new ItemStack(Items.BRICK) : stack;
        }
      };

  private ForgeCreativeTabs() {}

  static void init() {
    HYDRONYASAMA_CORE.getRecipeFolderName();
    HYDRONYASAMA_BUILDING.getRecipeFolderName();
    HYDRONYASAMA_ELECTRICITY.getRecipeFolderName();
    HYDRONYASAMA_OPTICS.getRecipeFolderName();
    HYDRONYASAMA_TELECOM.getRecipeFolderName();
  }
}
