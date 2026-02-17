package cn.hydcraft.hydronyasama.telecom.compat.webservice;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import java.util.List;

/** Legacy-compatible telecom facade for external integrations. */
public interface ITelecom {
  String runScript(String endpoint, String blockPath, String code);

  String snapshot(String endpoint, String blockPath);

  String setNspgaProfile(
      String endpoint,
      String blockPath,
      int ioCount,
      List<String> inputs,
      List<String> outputs,
      String code);

  TelecomCommService service();
}
