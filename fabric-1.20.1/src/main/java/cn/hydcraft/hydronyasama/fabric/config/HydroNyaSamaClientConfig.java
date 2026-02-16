package cn.hydcraft.hydronyasama.fabric.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class HydroNyaSamaClientConfig {
  private static final Logger LOGGER = LogManager.getLogger("HydroNyaSama-Config");
  private static final String FILE_NAME = "config.yaml";

  private static volatile ConfigValues values = ConfigValues.defaults();

  private HydroNyaSamaClientConfig() {}

  public static void load() {
    Path file = configFile();
    try {
      Files.createDirectories(file.getParent());
      if (!Files.exists(file)) {
        Files.write(file, defaultYaml().getBytes(StandardCharsets.UTF_8));
        values = ConfigValues.defaults();
        LOGGER.info("[config] created default config {}", file);
        return;
      }
      List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
      values = parse(lines);
      LOGGER.info(
          "[config] loaded {} (obj.enabled={}, obj.debug={}, obj.loadBlockModels={}, obj.loadItemModels={})",
          file,
          values.objEnabled,
          values.objDebug,
          values.objLoadBlockModels,
          values.objLoadItemModels);
    } catch (Exception e) {
      values = ConfigValues.defaults();
      LOGGER.error("[config] failed to load {}, fallback to defaults", file, e);
    }
  }

  public static ConfigValues get() {
    return values;
  }

  private static Path configFile() {
    return FabricLoader.getInstance().getConfigDir().resolve("hydronyasama").resolve(FILE_NAME);
  }

  private static String defaultYaml() {
    return String.join(
            "\n",
            "# HydroNyaSama client config",
            "# Restart game after editing this file.",
            "obj:",
            "  enabled: true",
            "  debug: false",
            "  load_block_models: true",
            "  load_item_models: false",
            "")
        + "\n";
  }

  private static ConfigValues parse(List<String> lines) throws IOException {
    boolean objEnabled = true;
    boolean objDebug = false;
    boolean objLoadBlockModels = true;
    boolean objLoadItemModels = false;

    String section = "";
    for (String rawLine : lines) {
      if (rawLine == null) {
        continue;
      }
      String noComment = stripComment(rawLine);
      if (noComment.trim().isEmpty()) {
        continue;
      }
      int indent = countIndent(noComment);
      String line = noComment.trim();
      if (indent == 0 && line.endsWith(":")) {
        section = line.substring(0, line.length() - 1).trim();
        continue;
      }
      int index = line.indexOf(':');
      if (index <= 0) {
        continue;
      }
      String key = line.substring(0, index).trim();
      String value = line.substring(index + 1).trim();
      if ("obj".equals(section)) {
        if ("enabled".equals(key)) {
          objEnabled = parseBool(value, objEnabled);
        } else if ("debug".equals(key)) {
          objDebug = parseBool(value, objDebug);
        } else if ("load_block_models".equals(key)) {
          objLoadBlockModels = parseBool(value, objLoadBlockModels);
        } else if ("load_item_models".equals(key)) {
          objLoadItemModels = parseBool(value, objLoadItemModels);
        }
      }
    }
    return new ConfigValues(objEnabled, objDebug, objLoadBlockModels, objLoadItemModels);
  }

  private static boolean parseBool(String raw, boolean fallback) {
    if ("true".equalsIgnoreCase(raw)) {
      return true;
    }
    if ("false".equalsIgnoreCase(raw)) {
      return false;
    }
    return fallback;
  }

  private static int countIndent(String line) {
    int count = 0;
    while (count < line.length() && line.charAt(count) == ' ') {
      count++;
    }
    return count;
  }

  private static String stripComment(String line) {
    int hash = line.indexOf('#');
    if (hash < 0) {
      return line;
    }
    return line.substring(0, hash);
  }

  public static final class ConfigValues {
    public final boolean objEnabled;
    public final boolean objDebug;
    public final boolean objLoadBlockModels;
    public final boolean objLoadItemModels;

    public ConfigValues(
        boolean objEnabled,
        boolean objDebug,
        boolean objLoadBlockModels,
        boolean objLoadItemModels) {
      this.objEnabled = objEnabled;
      this.objDebug = objDebug;
      this.objLoadBlockModels = objLoadBlockModels;
      this.objLoadItemModels = objLoadItemModels;
    }

    private static ConfigValues defaults() {
      return new ConfigValues(true, false, true, false);
    }
  }
}
