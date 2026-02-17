package cn.hydcraft.hydronyasama.optics.compat.legacy;

/** Base legacy optics descriptor for class-name compatibility. */
public class LegacyOpticsUnit {
  private final String legacyId;
  private final String endpoint;
  private final OpticsLegacyService service;

  protected LegacyOpticsUnit(String legacyId) {
    this.legacyId = legacyId;
    this.service = OpticsLegacyService.getInstance();
    this.endpoint = service.nextEndpoint(legacyId);
  }

  public String legacyId() {
    return legacyId;
  }

  public String endpoint() {
    return endpoint;
  }

  protected final OpticsLegacyService service() {
    return service;
  }
}
