package us.abstracta.jmeter.javadsl.azure.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Location {

  private final String name;

  @JsonCreator
  public Location(@JsonProperty("name") String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
