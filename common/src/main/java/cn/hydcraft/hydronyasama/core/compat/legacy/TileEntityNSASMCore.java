package cn.hydcraft.hydronyasama.core.compat.legacy;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import cn.hydcraft.hydronyasama.telecom.runtime.TelecomNgScriptEngine;
import java.util.List;

public final class TileEntityNSASMCore extends LegacyTelecomTileEntity {
  private String code = "";

  public TileEntityNSASMCore() {
    super("nsasm_box");
  }

  public TileEntityNSASMCore(String endpoint) {
    super(endpoint, "nsasm_box");
  }

  public TileEntityNSASMCore setCode(String code) {
    this.code = code == null ? "" : code;
    return this;
  }

  public String getCode() {
    return code;
  }

  public List<String> execute() {
    TelecomCommService service = service();
    return TelecomNgScriptEngine.run(code, endpoint(), blockPath(), service);
  }
}
