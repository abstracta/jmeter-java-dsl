package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Test {

  private final long id;
  private Project project;
  private String url;

  @JsonCreator
  private Test(@JsonProperty("id") long id) {
    this.id = id;
  }

  public void setProject(Project project) {
    this.project = project;
    this.url = project.getUrl() + "/tests/" + id;
  }

  public long getId() {
    return id;
  }

  public Project getProject() {
    return project;
  }

  public String getUrl() {
    return url;
  }

}
