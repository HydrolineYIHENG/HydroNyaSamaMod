package cn.hydcraft.hydronyasama.telecom.compat.renderer;

/**
 * Compatibility renderer descriptor for legacy NyaGameMR item.
 *
 * <p>Actual rendering is now delegated to version-specific item model pipelines.
 */
public final class RenderNyaGameMR {
  private final String modelId;

  public RenderNyaGameMR() {
    this("hydronyasama:item/nyagame_mr");
  }

  public RenderNyaGameMR(String modelId) {
    this.modelId = modelId == null || modelId.isEmpty() ? "hydronyasama:item/nyagame_mr" : modelId;
  }

  public String modelId() {
    return modelId;
  }
}
