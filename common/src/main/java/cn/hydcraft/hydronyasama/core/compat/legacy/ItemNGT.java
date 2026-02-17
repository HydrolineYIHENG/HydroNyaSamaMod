package cn.hydcraft.hydronyasama.core.compat.legacy;

import cn.hydcraft.hydronyasama.core.compat.network.NGTPacket;
import cn.hydcraft.hydronyasama.core.compat.nsasm.NSASM;
import cn.hydcraft.hydronyasama.core.nsasm.NsasmEngine;

public final class ItemNGT {
  private String code = "";

  public ItemNGT setCode(String code) {
    this.code = code == null ? "" : code;
    return this;
  }

  public String getCode() {
    return code;
  }

  public String encodePacket(String targetId) {
    return new NGTPacket(targetId, code).encode();
  }

  public ItemNGT applyPacket(String payload) {
    NGTPacket packet = NGTPacket.decode(payload);
    this.code = packet.code;
    return this;
  }

  public NsasmEngine.ExecutionResult run(NSASM nsasm) {
    NSASM engine = nsasm == null ? new NSASM() : nsasm;
    return engine.execute(code);
  }
}
