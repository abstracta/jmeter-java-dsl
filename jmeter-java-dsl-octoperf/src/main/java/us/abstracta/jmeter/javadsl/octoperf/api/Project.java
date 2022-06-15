package us.abstracta.jmeter.javadsl.octoperf.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;

public class Project {

  // we don't need getters since Jackson gets the values from fields
  private final String id;
  private final String userId;
  private final String workspaceId;
  private final String name;
  private final String description = "";
  private final String type = "DESIGN";
  private final Instant created = Instant.now();
  private final Instant lastModified = Instant.now();
  private final Set<String> tags;
  private Workspace workspace;

  @JsonCreator
  public Project(@JsonProperty("id") String id, @JsonProperty("userId") String userId,
      @JsonProperty("workspaceId") String workspaceId, @JsonProperty("name") String name) {
    this.id = id;
    this.userId = userId;
    this.workspaceId = workspaceId;
    this.name = name;
    this.tags = Collections.emptySet();
  }

  public Project(User user, Workspace workspace, String name, Set<String> tags) {
    this.id = "";
    this.userId = user.getId();
    this.workspace = workspace;
    this.workspaceId = workspace.getId();
    this.name = name;
    this.tags = tags;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }

  @JsonIgnore
  public Workspace getWorkspace() {
    return workspace;
  }

  @JsonIgnore
  public String getBaseUrl() {
    return workspace.getBaseUrl() + "/project/" + id;
  }

  @JsonIgnore
  public String getUrl() {
    return getBaseUrl() + "/design";
  }

}
