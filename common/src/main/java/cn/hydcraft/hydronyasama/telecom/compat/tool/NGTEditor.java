package cn.hydcraft.hydronyasama.telecom.compat.tool;

import java.util.function.Consumer;

/**
 * Headless compatibility editor.
 *
 * <p>Legacy desktop Swing editor is replaced by in-game book editor and command/script flow.
 */
public final class NGTEditor {
  private String code = "";
  private Consumer<String> onConfirm;

  public NGTEditor setCode(String code) {
    this.code = code == null ? "" : code;
    return this;
  }

  public String getCode() {
    return code;
  }

  public NGTEditor setOnConfirm(Consumer<String> onConfirm) {
    this.onConfirm = onConfirm;
    return this;
  }

  public void confirm() {
    if (onConfirm != null) {
      onConfirm.accept(code);
    }
  }
}
