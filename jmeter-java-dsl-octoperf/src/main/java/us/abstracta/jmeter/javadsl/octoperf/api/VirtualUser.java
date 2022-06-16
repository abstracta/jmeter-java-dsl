package us.abstracta.jmeter.javadsl.octoperf.api;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonTypeInfo(use = NAME, include = PROPERTY)
public class VirtualUser {

  // we don't need getters since Jackson gets the values from fields
  private final String id;
  private final String userId;
  private final String projectId;
  private final String name;
  private final String description;
  private final List<JsonNode> children;
  private final Instant created;
  private final Instant lastModified;
  private final String type;
  private final Set<String> tags;
  private String url;

  @JsonCreator
  private VirtualUser(@JsonProperty("id") String id, @JsonProperty("userId") String userId,
      @JsonProperty("projectId") String projectId, @JsonProperty("name") String name,
      @JsonProperty("description") String description,
      @JsonProperty("children") List<JsonNode> children,
      @JsonProperty("created") Instant created, @JsonProperty("lastModified") Instant lastModified,
      @JsonProperty("type") String type, @JsonProperty("tags") Set<String> tags) {
    this.id = id;
    this.userId = userId;
    this.projectId = projectId;
    this.name = name;
    this.description = description;
    this.children = children;
    this.created = created;
    this.lastModified = lastModified;
    this.type = type;
    this.tags = tags;
  }

  public String getId() {
    return id;
  }

  @JsonIgnore
  public List<Action> getChildren() {
    return children.stream()
        .map(n -> new Action(n.get("id").textValue(), n.get("name").textValue()))
        .collect(Collectors.toList());
  }

  public Set<String> getTags() {
    return tags;
  }

  public void setProject(Project project) {
    url = project.getUrl() + "/" + id;
  }

  @JsonIgnore
  public String getUrl() {
    return url;
  }

  public static class Action {

    private final String id;
    private final String name;

    public Action(String id, String name) {
      this.id = id;
      this.name = name;
    }

    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }

  }

}
