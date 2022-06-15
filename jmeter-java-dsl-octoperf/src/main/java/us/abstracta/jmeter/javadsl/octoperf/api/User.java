package us.abstracta.jmeter.javadsl.octoperf.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

  private final String id;

  @JsonCreator
  private User(@JsonProperty("id") String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

}
