package cn.hydcraft.hydronyasama.mtr;

import java.lang.reflect.Method;
import mtr.data.RailwayData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reflection-based helper for invoking {@code RailwayData#getInstance(Object)} without depending on
 * intermediary-only Minecraft parameter types at compile time.
 */
public final class MtrRailwayDataAccess {
  private static final Logger LOGGER = LoggerFactory.getLogger(MtrRailwayDataAccess.class);
  private static final Method GET_INSTANCE = resolveGetInstance();

  private MtrRailwayDataAccess() {}

  public static RailwayData resolve(Object level) {
    if (GET_INSTANCE == null || level == null) {
      return null;
    }
    try {
      return (RailwayData) GET_INSTANCE.invoke(null, level);
    } catch (ReflectiveOperationException exception) {
      LOGGER.debug("Failed to invoke RailwayData#getInstance", exception);
      return null;
    }
  }

  private static Method resolveGetInstance() {
    try {
      for (Method method : RailwayData.class.getDeclaredMethods()) {
        if (method.getName().equals("getInstance") && method.getParameterCount() == 1) {
          method.setAccessible(true);
          return method;
        }
      }
    } catch (Throwable throwable) {
      LOGGER.debug("Unable to resolve RailwayData#getInstance", throwable);
    }
    LOGGER.warn("RailwayData#getInstance method not found; MTR gateway will stay unavailable.");
    return null;
  }
}
