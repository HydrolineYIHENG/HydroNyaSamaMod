package cn.hydcraft.hydronyasama.core.compat.legacy;

public interface IItemBase {
  default String legacyItemId() {
    return "";
  }
}