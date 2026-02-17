package cn.hydcraft.hydronyasama.core.compat.legacy;

import cn.hydcraft.hydronyasama.telecom.compat.tool.NGTEditor;
import java.util.function.Consumer;

public final class GuiNGT {
  private final NGTEditor editor = new NGTEditor();

  public GuiNGT setCode(String code) {
    editor.setCode(code);
    return this;
  }

  public String getCode() {
    return editor.getCode();
  }

  public GuiNGT onConfirm(Consumer<String> callback) {
    editor.setOnConfirm(callback);
    return this;
  }

  public void confirm() {
    editor.confirm();
  }
}
