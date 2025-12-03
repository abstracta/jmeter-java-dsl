package us.abstracta.jmeter.javadsl.octoperf.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Scenario {

  // we don't need getters since Jackson gets the values from fields
  private final String id;
  private final String userId;
  private final String projectId;
  private final String name;
  private final String description = "";
  private final List<UserProfile> userProfiles;
  private final String mode = "STANDARD";
  private final Instant created = Instant.now();
  private final Instant lastModified = Instant.now();
  private final Set<String> tags;
  private Project project;

  @JsonCreator
  public Scenario(@JsonProperty("id") String id, @JsonProperty("tags") Set<String> tags) {
    this.id = id;
    this.userId = null;
    this.projectId = null;
    this.name = null;
    this.userProfiles = null;
    this.tags = tags;
  }

  public Scenario(User user, Project project, String name, List<UserProfile> userProfiles,
      Set<String> tags) {
    this.id = "";
    this.userId = user.getId();
    this.project = project;
    this.projectId = project.getId();
    this.name = name;
    this.userProfiles = userProfiles;
    this.tags = tags;
  }

  public String getId() {
    return id;
  }

  public Set<String> getTags() {
    return tags;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  @JsonIgnore
  public Project getProject() {
    return project;
  }

  @JsonIgnore
  public String getUrl() {
    return project.getBaseUrl() + "/runtime/scenario/" + id;
  }

}
