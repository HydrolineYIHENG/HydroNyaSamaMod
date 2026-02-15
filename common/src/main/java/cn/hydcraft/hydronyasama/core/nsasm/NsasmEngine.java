package cn.hydcraft.hydronyasama.core.nsasm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Minimal cross-version NSASM placeholder used during phased migration. */
public final class NsasmEngine {
  private final List<String> outputs = new ArrayList<>();

  public ExecutionResult execute(String source) {
    if (source == null || source.trim().isEmpty()) {
      return new ExecutionResult(false, "empty program", Collections.<String>emptyList());
    }
    outputs.clear();
    outputs.add("NSASM baseline runtime is in compatibility mode.");
    outputs.add("Program accepted but advanced opcodes are not enabled yet.");
    return new ExecutionResult(true, null, new ArrayList<>(outputs));
  }

  public static final class ExecutionResult {
    private final boolean success;
    private final String error;
    private final List<String> outputLines;

    public ExecutionResult(boolean success, String error, List<String> outputLines) {
      this.success = success;
      this.error = error;
      this.outputLines = outputLines;
    }

    public boolean isSuccess() {
      return success;
    }

    public String getError() {
      return error;
    }

    public List<String> getOutputLines() {
      return outputLines;
    }
  }
}
