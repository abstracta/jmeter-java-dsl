package us.abstracta.jmeter.javadsl.octoperf.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class Provider {

  private final String id;
  private final Map<String, Region> regions;

  @JsonCreator
  public Provider(@JsonProperty("id") String id,
      @JsonProperty("regions") Map<String, Region> regions) {
    this.id = id;
    this.regions = regions;
  }

  public String getId() {
    return id;
  }

  public Map<String, Region> getRegions() {
    return regions;
  }

  public static class Region {

  }

}
