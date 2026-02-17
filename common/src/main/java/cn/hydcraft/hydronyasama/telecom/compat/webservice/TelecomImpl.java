package cn.hydcraft.hydronyasama.telecom.compat.webservice;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import cn.hydcraft.hydronyasama.telecom.runtime.TelecomNgScriptEngine;
import java.util.List;

/** Default implementation for the legacy-compatible telecom facade. */
public final class TelecomImpl implements ITelecom {
  private final TelecomCommService service;

  public TelecomImpl() {
    this(TelecomCommService.getInstance());
  }

  public TelecomImpl(TelecomCommService service) {
    this.service = service == null ? TelecomCommService.getInstance() : service;
  }

  @Override
  public String runScript(String endpoint, String blockPath, String code) {
    List<String> logs = TelecomNgScriptEngine.run(code, endpoint, blockPath, service);
    return String.join("\n", logs);
  }

  @Override
  public String snapshot(String endpoint, String blockPath) {
    return TelecomCommService.describeSnapshot(service.snapshot(endpoint, blockPath));
  }

  @Override
  public String setNspgaProfile(
      String endpoint,
      String blockPath,
      int ioCount,
      List<String> inputs,
      List<String> outputs,
      String code) {
    service.setNspgaIoCount(endpoint, blockPath, ioCount);
    service.setNspgaInputs(endpoint, blockPath, inputs);
    service.setNspgaOutputs(endpoint, blockPath, outputs);
    service.setNspgaCode(endpoint, blockPath, code);
    return TelecomCommService.describeNspgaProfile(service.nspgaProfile(endpoint, blockPath));
  }

  @Override
  public TelecomCommService service() {
    return service;
  }
}
