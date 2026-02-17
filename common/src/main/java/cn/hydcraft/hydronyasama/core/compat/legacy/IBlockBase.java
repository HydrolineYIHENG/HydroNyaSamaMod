package cn.hydcraft.hydronyasama.core.compat.legacy;

public interface IBlockBase {
  default String legacyBlockId() {
    return "";
  }
}