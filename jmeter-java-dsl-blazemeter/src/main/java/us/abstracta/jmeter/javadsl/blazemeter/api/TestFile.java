package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestFile {

  private final String name;

  @JsonCreator
  public TestFile(@JsonProperty("name") String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
