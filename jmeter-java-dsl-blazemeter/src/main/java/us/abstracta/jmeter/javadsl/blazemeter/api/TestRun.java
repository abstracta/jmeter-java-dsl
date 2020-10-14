package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestRun {

  public final long id;
  public String url;

  @JsonCreator
  private TestRun(@JsonProperty("id") long id) {
    this.id = id;
  }

  public void setTest(Test test) {
    this.url = test.project.url + "/masters/" + id;
  }

}
