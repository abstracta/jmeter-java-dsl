package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestRun {

  private final long id;
  private String url;

  @JsonCreator
  private TestRun(@JsonProperty("id") long id) {
    this.id = id;
  }

  public void setTest(Test test) {
    this.url = test.getProject().getUrl() + "/masters/" + id;
  }

  public long getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }

}
