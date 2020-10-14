package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Test {

  public final long id;
  public final String name;
  public final long projectId;
  public Project project;
  public String url;

  @JsonCreator
  private Test(@JsonProperty("id") long id, @JsonProperty("name") String name,
      @JsonProperty("projectId") long projectId) {
    this.id = id;
    this.name = name;
    this.projectId = projectId;
  }

  public void setProject(Project project) {
    this.project = project;
    this.url = project.url + "/tests/" + id;
  }

}
