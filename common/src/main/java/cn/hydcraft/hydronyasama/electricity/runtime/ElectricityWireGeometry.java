package cn.hydcraft.hydronyasama.electricity.runtime;

import cn.hydcraft.hydronyasama.core.physics.Vec3;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loader-agnostic wire geometry sampling logic migrated from legacy electricity rendering code.
 *
 * <p>This class intentionally outputs abstract line segments. Loader-side renderers can transform
 * these segments into quads/meshes in version-specific pipelines.
 */
public final class ElectricityWireGeometry {

  public static final class Segment {
    public final Vec3 from;
    public final Vec3 to;

    public Segment(Vec3 from, Vec3 to) {
      this.from = from;
      this.to = to;
    }
  }

  private ElectricityWireGeometry() {}

  public static List<Segment> buildCableSegments(Vec3 from, Vec3 to) {
    Vec3 delta = subtract(to, from);
    double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
    double drop = 0.05D * horizontal;
    Vec3 adjustedTo = Math.abs(delta.y) < 0.0625D ? new Vec3(to.x, to.y + 0.0625D, to.z) : to;
    return buildCatenarySegments(from, adjustedTo, false, drop);
  }

  public static List<Segment> buildHardCableSegments(Vec3 from, Vec3 to) {
    return Collections.singletonList(new Segment(from, to));
  }

  public static List<Segment> buildPillarSegments(Vec3 from, Vec3 to) {
    return Collections.singletonList(new Segment(from, to));
  }

  public static List<Segment> buildRailCatenarySegments(Vec3 from, Vec3 to) {
    Vec3 delta = subtract(to, from);
    double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
    int steps = Math.max(1, (int) Math.floor(horizontal));
    return buildLinearSegments(from, to, steps);
  }

  public static List<Segment> buildCatenarySegments(
      Vec3 from, Vec3 to, boolean half, double dropDistance) {
    double horizontal = horizontalDistance(from, to);
    if (horizontal <= 1e-6D) {
      return Collections.singletonList(new Segment(from, to));
    }

    int steps = horizontal < 64.0D ? 128 : Math.max(2, (int) Math.floor(horizontal * 2.0D));
    if (half) {
      steps = Math.max(1, steps / 2);
    }

    double stepHorizontal = horizontal / steps;
    ElectricityCatenary catenary =
        new ElectricityCatenary(0.0D, to.y - from.y, horizontal, dropDistance);

    Vec3 horizontalDirection = normalizeHorizontal(from, to);
    List<Vec3> points = new ArrayList<>(steps + 1);
    points.add(from);

    for (int i = 1; i <= steps; i++) {
      double h = stepHorizontal * i;
      double x = from.x + horizontalDirection.x * h;
      double z = from.z + horizontalDirection.z * h;
      double y = from.y + catenary.apply((float) h);
      points.add(new Vec3(x, y, z));
    }

    return toSegments(points);
  }

  private static List<Segment> buildLinearSegments(Vec3 from, Vec3 to, int steps) {
    List<Vec3> points = new ArrayList<>(steps + 1);
    for (int i = 0; i <= steps; i++) {
      double t = (double) i / (double) steps;
      points.add(
          new Vec3(
              from.x + (to.x - from.x) * t,
              from.y + (to.y - from.y) * t,
              from.z + (to.z - from.z) * t));
    }
    return toSegments(points);
  }

  private static List<Segment> toSegments(List<Vec3> points) {
    if (points.size() < 2) {
      return Collections.emptyList();
    }
    List<Segment> segments = new ArrayList<>(points.size() - 1);
    for (int i = 1; i < points.size(); i++) {
      segments.add(new Segment(points.get(i - 1), points.get(i)));
    }
    return segments;
  }

  private static double horizontalDistance(Vec3 from, Vec3 to) {
    double dx = to.x - from.x;
    double dz = to.z - from.z;
    return Math.sqrt(dx * dx + dz * dz);
  }

  private static Vec3 normalizeHorizontal(Vec3 from, Vec3 to) {
    double dx = to.x - from.x;
    double dz = to.z - from.z;
    double length = Math.sqrt(dx * dx + dz * dz);
    if (length <= 1e-6D) {
      return new Vec3(0.0D, 0.0D, 0.0D);
    }
    return new Vec3(dx / length, 0.0D, dz / length);
  }

  private static Vec3 subtract(Vec3 left, Vec3 right) {
    return new Vec3(left.x - right.x, left.y - right.y, left.z - right.z);
  }
}
