package cn.hydcraft.hydronyasama.objrender.fabric.v120;

import cn.hydcraft.hydronyasama.objrender.api.ObjRenderClientBootstrap;

public final class ObjRenderClientBootstrap120 implements ObjRenderClientBootstrap {
  @Override
  public void initialize() {
    ObjModelResourceHandler120.register();
  }
}
