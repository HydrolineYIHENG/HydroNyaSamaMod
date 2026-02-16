package cn.hydcraft.hydronyasama.electricity.runtime;

/** Catenary curve solver used by legacy wire rendering logic. */
public final class ElectricityCatenary {

  private final float u;
  private final float x1;
  private final float k;

  public ElectricityCatenary(double yFrom, double yTo, double distance, double drop) {
    this((float) yFrom, (float) yTo, (float) distance, (float) drop);
  }

  public ElectricityCatenary(float yFrom, float yTo, float distance, float drop) {
    float baseY;
    float spanY;
    if (yFrom < yTo) {
      baseY = yFrom;
      spanY = yTo - yFrom;
    } else {
      baseY = yTo;
      spanY = yFrom - yTo;
    }

    if (spanY < 1e-4F) {
      spanY = 1e-4F;
    }

    float cableLength = calcCableLength(spanY, drop, distance);
    float l1 =
        -drop * cableLength / spanY
            + (float) Math.sqrt(drop * (spanY + drop) * (cableLength * cableLength - spanY * spanY))
                / spanY;
    float solvedU = 2.0F * drop / (l1 * l1 - drop * drop);
    float solvedX1 = ElectricityMath.asinh(solvedU * l1) / solvedU;
    float solvedK = baseY - drop - 1.0F / solvedU;

    if (yFrom > yTo) {
      solvedX1 =
          solvedX1
              + distance
              - ElectricityMath.acosh(solvedU * (yTo - solvedK)) / solvedU
              - solvedX1;
    }

    this.u = solvedU;
    this.x1 = solvedX1;
    this.k = solvedK;
  }

  public float apply(float x) {
    return (float) (Math.cosh(u * (x - x1)) / u + k);
  }

  public float derivative(float x) {
    return (float) Math.sinh(u * (x - x1));
  }

  public static float calcCableLength(float spanY, float drop, float distance) {
    float lower = (float) Math.sqrt(distance * distance + spanY * spanY);
    float upper = 2.0F * drop + distance + spanY;
    float current = (upper + lower) / 2.0F;

    int guard = 0;
    while ((upper - lower) > 1e-4F && ++guard < 100) {
      if (calcSpanDistance(spanY, current, drop) > distance) {
        upper = current;
      } else {
        lower = current;
      }
      current = (upper + lower) / 2.0F;
    }
    return current;
  }

  public static float calcSpanDistance(float spanY, float cableLength, float drop) {
    float q =
        2.0F
            * (float)
                Math.sqrt(drop * (spanY + drop) * (cableLength * cableLength - spanY * spanY));
    float left =
        ((cableLength * cableLength - spanY * spanY) * (spanY + 2.0F * drop) - cableLength * q)
            / (spanY * spanY);
    float right =
        ElectricityMath.atanh((spanY * spanY) / (cableLength * (spanY + 2.0F * drop) - q));
    return left * right;
  }
}
