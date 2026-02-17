package cn.hydcraft.hydronyasama.core.nsasm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Cross-version NSASM compatibility runtime with a small executable instruction subset. */
public final class NsasmEngine {
  public ExecutionResult execute(String source) {
    if (source == null || source.trim().isEmpty()) {
      return new ExecutionResult(false, "empty program", Collections.<String>emptyList());
    }
    List<String> outputs = new ArrayList<>();
    Map<String, Double> registers = new LinkedHashMap<>();
    String[] lines = source.replace("\r", "").split("\n");
    int lineNo = 0;
    try {
      for (String raw : lines) {
        lineNo++;
        String line = trimComment(raw);
        if (line.isEmpty()) {
          continue;
        }
        String[] tokens = line.trim().split("\\s+");
        String op = tokens[0].toLowerCase(Locale.ROOT);
        if ("nop".equals(op)) {
          continue;
        }
        if ("halt".equals(op) || "end".equals(op)) {
          break;
        }
        if ("set".equals(op) || "let".equals(op)) {
          requireArgCount(tokens, 3, lineNo, op);
          registers.put(tokens[1], parseValue(tokens[2], registers));
          continue;
        }
        if ("mov".equals(op)) {
          requireArgCount(tokens, 3, lineNo, op);
          registers.put(tokens[1], parseValue(tokens[2], registers));
          continue;
        }
        if ("add".equals(op) || "sub".equals(op) || "mul".equals(op) || "div".equals(op)) {
          requireArgCount(tokens, 3, lineNo, op);
          String target = tokens[1];
          double left = registers.getOrDefault(target, 0.0D);
          double right = parseValue(tokens[2], registers);
          if ("add".equals(op)) {
            registers.put(target, left + right);
          } else if ("sub".equals(op)) {
            registers.put(target, left - right);
          } else if ("mul".equals(op)) {
            registers.put(target, left * right);
          } else {
            if (Math.abs(right) < 1.0E-9D) {
              throw new IllegalArgumentException("line " + lineNo + ": div by zero");
            }
            registers.put(target, left / right);
          }
          continue;
        }
        if ("print".equals(op) || "echo".equals(op)) {
          String body = line.length() <= op.length() ? "" : line.substring(op.length()).trim();
          outputs.add(resolvePrintable(body, registers));
          continue;
        }
        throw new IllegalArgumentException("line " + lineNo + ": unsupported opcode '" + op + "'");
      }
    } catch (RuntimeException ex) {
      return new ExecutionResult(false, ex.getMessage(), new ArrayList<>(outputs), registers);
    }
    if (outputs.isEmpty()) {
      outputs.add("ok");
    }
    return new ExecutionResult(true, null, new ArrayList<>(outputs), registers);
  }

  private static String trimComment(String line) {
    String out = line == null ? "" : line.trim();
    int hash = out.indexOf('#');
    if (hash >= 0) {
      out = out.substring(0, hash).trim();
    }
    int slash = out.indexOf("//");
    if (slash >= 0) {
      out = out.substring(0, slash).trim();
    }
    return out;
  }

  private static void requireArgCount(String[] tokens, int expected, int lineNo, String op) {
    if (tokens.length < expected) {
      throw new IllegalArgumentException(
          "line " + lineNo + ": opcode '" + op + "' expects " + (expected - 1) + " args");
    }
  }

  private static double parseValue(String token, Map<String, Double> registers) {
    Double reg = registers.get(token);
    if (reg != null) {
      return reg;
    }
    try {
      return Double.parseDouble(token);
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException("unknown symbol '" + token + "'");
    }
  }

  private static String resolvePrintable(String body, Map<String, Double> registers) {
    if (body == null || body.isEmpty()) {
      return "";
    }
    if ((body.startsWith("\"") && body.endsWith("\""))
        || (body.startsWith("'") && body.endsWith("'"))) {
      return body.substring(1, body.length() - 1);
    }
    Double reg = registers.get(body);
    if (reg != null) {
      if (Math.abs(reg - Math.rint(reg)) < 1.0E-9D) {
        return Long.toString(Math.round(reg));
      }
      return String.format(Locale.ROOT, "%.6f", reg);
    }
    return body;
  }

  public static final class ExecutionResult {
    private final boolean success;
    private final String error;
    private final List<String> outputLines;
    private final Map<String, Double> registers;

    public ExecutionResult(boolean success, String error, List<String> outputLines) {
      this(success, error, outputLines, Collections.<String, Double>emptyMap());
    }

    public ExecutionResult(
        boolean success, String error, List<String> outputLines, Map<String, Double> registers) {
      this.success = success;
      this.error = error;
      this.outputLines = outputLines;
      this.registers = new LinkedHashMap<>(registers);
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

    public Map<String, Double> getRegisters() {
      return Collections.unmodifiableMap(registers);
    }
  }
}
