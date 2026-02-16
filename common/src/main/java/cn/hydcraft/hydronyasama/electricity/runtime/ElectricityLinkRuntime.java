package cn.hydcraft.hydronyasama.electricity.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Loader-agnostic connection runtime for electricity nodes/endpoints.
 *
 * <p>Migrated from the legacy sender/target wiring pattern and simplified for cross-version use.
 */
public final class ElectricityLinkRuntime {

  public enum LinkKind {
    WIRE,
    CABLE,
    PILLAR,
    CATENARY
  }

  public static final class LinkNodeState {
    private final String id;
    private final LinkKind kind;
    private String senderId;
    private String targetId;

    private LinkNodeState(String id, LinkKind kind) {
      this.id = id;
      this.kind = kind;
    }

    public String id() {
      return id;
    }

    public LinkKind kind() {
      return kind;
    }

    public String senderId() {
      return senderId;
    }

    public String targetId() {
      return targetId;
    }
  }

  private final Map<String, LinkNodeState> nodes = new HashMap<>();

  public LinkNodeState registerNode(String id, LinkKind kind) {
    String key = requireNonEmpty(id, "id");
    LinkNodeState state = new LinkNodeState(key, Objects.requireNonNull(kind, "kind"));
    nodes.put(key, state);
    return state;
  }

  public void unregisterNode(String id) {
    nodes.remove(id);
    for (LinkNodeState state : nodes.values()) {
      if (id != null && id.equals(state.senderId)) {
        state.senderId = null;
      }
      if (id != null && id.equals(state.targetId)) {
        state.targetId = null;
      }
    }
  }

  public LinkNodeState node(String id) {
    return nodes.get(id);
  }

  public void setSender(String nodeId, String senderId) {
    LinkNodeState state = requireNode(nodeId);
    state.senderId = normalizeNullable(senderId);
  }

  public void setTarget(String nodeId, String targetId) {
    LinkNodeState state = requireNode(nodeId);
    state.targetId = normalizeNullable(targetId);
  }

  public void clearLinks(String nodeId) {
    LinkNodeState state = requireNode(nodeId);
    state.senderId = null;
    state.targetId = null;
  }

  private LinkNodeState requireNode(String id) {
    LinkNodeState state = nodes.get(id);
    if (state == null) {
      throw new IllegalArgumentException("unknown node: " + id);
    }
    return state;
  }

  private static String requireNonEmpty(String value, String field) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(field + " is empty");
    }
    return value;
  }

  private static String normalizeNullable(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
