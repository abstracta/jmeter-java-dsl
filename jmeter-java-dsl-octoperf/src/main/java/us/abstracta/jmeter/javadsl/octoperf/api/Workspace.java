package us.abstracta.jmeter.javadsl.octoperf.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Workspace {

  private final String id;
  private String baseUrl;

  @JsonCreator
  private Workspace(@JsonProperty("id") String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setBaseAppUrl(String baseAppUrl) {
    baseUrl = baseAppUrl + "/workspace/" + id;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

}
