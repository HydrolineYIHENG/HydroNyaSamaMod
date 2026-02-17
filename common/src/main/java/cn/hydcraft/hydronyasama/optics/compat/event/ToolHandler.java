package cn.hydcraft.hydronyasama.optics.compat.event;

import cn.hydcraft.hydronyasama.optics.compat.legacy.LEDPlate;
import cn.hydcraft.hydronyasama.optics.compat.legacy.LegacyOpticsUnit;
import cn.hydcraft.hydronyasama.optics.compat.legacy.OpticsLegacyService;
import cn.hydcraft.hydronyasama.optics.compat.legacy.RGBLight;
import cn.hydcraft.hydronyasama.optics.compat.legacy.StationLamp;
import cn.hydcraft.hydronyasama.optics.compat.legacy.TextWall;
import cn.hydcraft.hydronyasama.optics.compat.tool.NSOConv;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Compatibility event hub for optics tool inspect actions. */
public final class ToolHandler {
  private static final ToolHandler INSTANCE = new ToolHandler();

  private final CopyOnWriteArrayList<Consumer<String>> inspectCallbacks = new CopyOnWriteArrayList<>();

  private ToolHandler() {}

  public static ToolHandler instance() {
    return INSTANCE;
  }

  public void onInspect(Consumer<String> callback) {
    if (callback != null) {
      inspectCallbacks.add(callback);
    }
  }

  public void publishInspect(String message) {
    String safe = message == null ? "" : message;
    for (Consumer<String> callback : inspectCallbacks) {
      callback.accept(safe);
    }
  }

  public String inspect(LegacyOpticsUnit unit) {
    if (unit == null) {
      return "[NSO] missing";
    }
    String text;
    if (unit instanceof StationLamp) {
      StationLamp lamp = (StationLamp) unit;
      text = NSOConv.inspectColor(lamp.lightColor());
    } else if (unit instanceof RGBLight) {
      RGBLight light = (RGBLight) unit;
      text = NSOConv.inspectColor(light.lightColor());
    } else if (unit instanceof LEDPlate) {
      LEDPlate plate = (LEDPlate) unit;
      text = NSOConv.inspectForegroundBackground(plate.foreground(), plate.background());
    } else if (unit instanceof TextWall) {
      text = "[NSO] Text: " + ((TextWall) unit).text();
    } else {
      OpticsLegacyService service = OpticsLegacyService.getInstance();
      text =
          "[NSO] "
              + unit.legacyId()
              + " color="
              + NSOConv.inspectColor(service.color(unit.endpoint(), 0xFFFFFFFF));
    }
    publishInspect(text);
    return text;
  }
}
