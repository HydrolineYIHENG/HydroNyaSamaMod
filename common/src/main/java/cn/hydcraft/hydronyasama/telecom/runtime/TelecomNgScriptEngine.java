package cn.hydcraft.hydronyasama.telecom.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lightweight NGTablet script executor.
 *
 * <p>Legacy NGT+NSASM was desktop-editor driven. This engine keeps an in-game, loader-agnostic
 * subset so telecom automation remains usable during migration.
 */
public final class TelecomNgScriptEngine {
  private static final int MAX_STEPS_PER_TICK = 200;

  private TelecomNgScriptEngine() {}

  public static List<String> run(
      String code, String endpoint, String blockPath, TelecomCommService service) {
    List<String> logs = new ArrayList<String>();
    if (service == null) {
      logs.add("service unavailable");
      return logs;
    }
    if (code == null || code.trim().isEmpty()) {
      logs.add("code is empty");
      return logs;
    }
    if (endpoint == null || endpoint.isEmpty() || blockPath == null || blockPath.isEmpty()) {
      logs.add("no target selected, scan or click a telecom block first");
      return logs;
    }

    String[] lines = code.replace("\r", "").split("\n");
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i].trim();
      if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
        continue;
      }
      String[] parts = line.split("\\s+");
      String op = parts[0].toLowerCase();
      try {
        if ("help".equals(op)) {
          logs.add(
              "ops: help,tick,snapshot,toggle,toggle_neg,editor,input,nspga_io,nspga_inputs,nspga_outputs,nspga_code,nspga_show,nspga_clear");
        } else if ("tick".equals(op)) {
          int steps = parseInt(parts, 1, 1);
          if (steps < 1) {
            steps = 1;
          }
          if (steps > MAX_STEPS_PER_TICK) {
            steps = MAX_STEPS_PER_TICK;
          }
          for (int j = 0; j < steps; j++) {
            service.tick();
          }
          logs.add("tick " + steps);
        } else if ("snapshot".equals(op)) {
          logs.add(TelecomCommService.describeSnapshot(service.snapshot(endpoint, blockPath)));
        } else if ("toggle".equals(op)) {
          logs.add(service.handleManualUse(endpoint, blockPath, false));
        } else if ("toggle_neg".equals(op)) {
          logs.add(service.handleManualUse(endpoint, blockPath, true));
        } else if ("editor".equals(op)) {
          int mode = parseInt(parts, 1, 0);
          boolean inverter = parseInt(parts, 2, 0) != 0;
          service.applyEditorState(endpoint, blockPath, mode, inverter);
          logs.add("editor mode=" + mode + " inverter=" + (inverter ? 1 : 0));
        } else if ("input".equals(op)) {
          boolean state = parseInt(parts, 1, 0) != 0;
          service.setInputFromRedstone(endpoint, blockPath, state);
          logs.add("input=" + (state ? 1 : 0));
        } else if ("nspga_io".equals(op)) {
          int ioCount = parseInt(parts, 1, 1);
          service.setNspgaIoCount(endpoint, blockPath, ioCount);
          logs.add("nspga_io=" + Math.max(1, ioCount));
        } else if ("nspga_inputs".equals(op)) {
          service.setNspgaInputs(endpoint, blockPath, parseCsv(parts, 1));
          logs.add(
              TelecomCommService.describeNspgaProfile(service.nspgaProfile(endpoint, blockPath)));
        } else if ("nspga_outputs".equals(op)) {
          service.setNspgaOutputs(endpoint, blockPath, parseCsv(parts, 1));
          logs.add(
              TelecomCommService.describeNspgaProfile(service.nspgaProfile(endpoint, blockPath)));
        } else if ("nspga_code".equals(op)) {
          String codeLine = joinFrom(parts, 1);
          service.setNspgaCode(endpoint, blockPath, codeLine);
          logs.add(
              TelecomCommService.describeNspgaProfile(service.nspgaProfile(endpoint, blockPath)));
        } else if ("nspga_show".equals(op)) {
          logs.add(
              TelecomCommService.describeNspgaProfile(service.nspgaProfile(endpoint, blockPath)));
        } else if ("nspga_clear".equals(op)) {
          service.clearNspgaProfile(endpoint, blockPath);
          logs.add(
              TelecomCommService.describeNspgaProfile(service.nspgaProfile(endpoint, blockPath)));
        } else {
          logs.add("unknown op at line " + (i + 1) + ": " + op);
        }
      } catch (RuntimeException ex) {
        logs.add("line " + (i + 1) + " failed: " + ex.getMessage());
      }
    }

    if (logs.isEmpty()) {
      logs.add("no-op");
    }
    return logs;
  }

  private static int parseInt(String[] parts, int index, int fallback) {
    if (parts.length <= index) {
      return fallback;
    }
    return Integer.parseInt(parts[index]);
  }

  private static String joinFrom(String[] parts, int startIndex) {
    if (parts.length <= startIndex) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    for (int i = startIndex; i < parts.length; i++) {
      if (builder.length() > 0) {
        builder.append(' ');
      }
      builder.append(parts[i]);
    }
    return builder.toString();
  }

  private static List<String> parseCsv(String[] parts, int startIndex) {
    String value = joinFrom(parts, startIndex);
    if (value.isEmpty()) {
      return new ArrayList<String>();
    }
    String[] raw = value.split(",");
    return new ArrayList<String>(Arrays.asList(raw));
  }
}
