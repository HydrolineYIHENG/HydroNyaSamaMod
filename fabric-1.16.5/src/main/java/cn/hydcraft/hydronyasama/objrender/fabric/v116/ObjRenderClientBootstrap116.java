package cn.hydcraft.hydronyasama.objrender.fabric.v116;

import cn.hydcraft.hydronyasama.objrender.api.ObjRenderClientBootstrap;

public final class ObjRenderClientBootstrap116 implements ObjRenderClientBootstrap {
  @Override
  public void initialize() {
    ObjModelResourceHandler116.register();
  }
}
