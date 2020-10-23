package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

  private final Project defaultProject;

  @JsonCreator
  private User(@JsonProperty("defaultProject") Project defaultProject) {
    this.defaultProject = defaultProject;
  }

  public Project getDefaultProject() {
    return defaultProject;
  }

}
