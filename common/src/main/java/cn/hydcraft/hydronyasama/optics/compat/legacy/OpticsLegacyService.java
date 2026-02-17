package cn.hydcraft.hydronyasama.optics.compat.legacy;

import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.AdBoardRenderer;
import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.GuideBoardRenderer;
import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.HoloJetRevRenderer;
import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.LEDPlateRenderer;
import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.LightRenderer;
import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.PillarHeadRenderer;
import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.PlatformPlateRenderer;
import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.StationBoardRenderer;
import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.StationLampRenderer;
import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.TextWallRenderer;
import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.TileRendererProfile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/** Shared state service for legacy optics compatibility objects. */
public final class OpticsLegacyService {
  private static final OpticsLegacyService INSTANCE = new OpticsLegacyService();

  private final AtomicLong nextId = new AtomicLong(1L);
  private final Map<String, String> textByEndpoint = new HashMap<String, String>();
  private final Map<String, Integer> colorByEndpoint = new HashMap<String, Integer>();
  private final Map<String, Integer> backColorByEndpoint = new HashMap<String, Integer>();
  private final Map<String, Double> powerByEndpoint = new HashMap<String, Double>();
  private final Map<String, TileRendererProfile> rendererByType = new HashMap<String, TileRendererProfile>();

  private OpticsLegacyService() {
    rendererByType.put("ad_board", new AdBoardRenderer());
    rendererByType.put("guide_board", new GuideBoardRenderer());
    rendererByType.put("holo_jet_rev", new HoloJetRevRenderer());
    rendererByType.put("led_plate", new LEDPlateRenderer());
    rendererByType.put("light", new LightRenderer());
    rendererByType.put("pillar_head", new PillarHeadRenderer());
    rendererByType.put("platform_plate", new PlatformPlateRenderer());
    rendererByType.put("station_board", new StationBoardRenderer());
    rendererByType.put("station_lamp", new StationLampRenderer());
    rendererByType.put("text_wall", new TextWallRenderer());
  }

  public static OpticsLegacyService getInstance() {
    return INSTANCE;
  }

  public synchronized String nextEndpoint(String legacyId) {
    String base = legacyId == null || legacyId.trim().isEmpty() ? "optics" : legacyId.trim();
    return base + "#" + nextId.getAndIncrement();
  }

  public synchronized void setText(String endpoint, String text) {
    textByEndpoint.put(normalize(endpoint), text == null ? "" : text);
  }

  public synchronized String text(String endpoint) {
    String value = textByEndpoint.get(normalize(endpoint));
    return value == null ? "" : value;
  }

  public synchronized void setColor(String endpoint, int color) {
    colorByEndpoint.put(normalize(endpoint), Integer.valueOf(color));
  }

  public synchronized int color(String endpoint, int fallback) {
    Integer value = colorByEndpoint.get(normalize(endpoint));
    return value == null ? fallback : value.intValue();
  }

  public synchronized void setBackColor(String endpoint, int color) {
    backColorByEndpoint.put(normalize(endpoint), Integer.valueOf(color));
  }

  public synchronized int backColor(String endpoint, int fallback) {
    Integer value = backColorByEndpoint.get(normalize(endpoint));
    return value == null ? fallback : value.intValue();
  }

  public synchronized void setPower(String endpoint, double powerMilliWatt) {
    powerByEndpoint.put(normalize(endpoint), Double.valueOf(powerMilliWatt));
  }

  public synchronized double power(String endpoint, double fallback) {
    Double value = powerByEndpoint.get(normalize(endpoint));
    return value == null ? fallback : value.doubleValue();
  }

  public synchronized TileRendererProfile rendererProfile(String type) {
    String key = type == null ? "" : type.trim();
    TileRendererProfile profile = rendererByType.get(key);
    return profile == null ? rendererByType.get("text_wall") : profile;
  }

  private static String normalize(String endpoint) {
    if (endpoint == null || endpoint.trim().isEmpty()) {
      throw new IllegalArgumentException("endpoint is empty");
    }
    return endpoint.trim();
  }
}
