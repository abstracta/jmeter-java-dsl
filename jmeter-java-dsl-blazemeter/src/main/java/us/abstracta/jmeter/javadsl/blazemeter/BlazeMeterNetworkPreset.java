package us.abstracta.jmeter.javadsl.blazemeter;

/**
 * Network conditions BlazeMeter can emulate while running a test plan.
 *
 * @see BlazeMeterEngine#networkEmulation(BlazeMeterNetworkPreset)
 * @since 2.4
 */
public enum BlazeMeterNetworkPreset {
  INTERNET_UNLIMITED("Internet (Unlimited)"),
  WIFI("WiFi"),
  CABLE("Cable"),
  DSL("DSL"),
  LTE("LTE"),
  MOBILE_3G_GOOD("3G-good"),
  MOBILE_3G_NORMAL("3G-normal"),
  MOBILE_3G_POOR("3G-poor"),
  EDGE_GOOD("Edge-good"),
  EDGE_NORMAL("Edge-normal"),
  EDGE_POOR("Edge-poor");

  private final String name;

  BlazeMeterNetworkPreset(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
