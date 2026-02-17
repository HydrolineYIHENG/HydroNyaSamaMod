package cn.hydcraft.hydronyasama.core.compat.nsasm;

import cn.hydcraft.hydronyasama.core.nsasm.NsasmEngine;

/** Legacy-named NSASM compatibility facade. */
public final class NSASM {
  private final NsasmEngine engine;

  public NSASM() {
    this.engine = new NsasmEngine();
  }

  public NsasmEngine.ExecutionResult execute(String source) {
    return engine.execute(source);
  }
}
