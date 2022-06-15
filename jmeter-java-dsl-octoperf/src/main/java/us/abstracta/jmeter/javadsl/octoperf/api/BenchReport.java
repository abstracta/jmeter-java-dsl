package us.abstracta.jmeter.javadsl.octoperf.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class BenchReport {

  private final String id;
  private final List<String> benchResultIds;
  private String url;

  @JsonCreator
  public BenchReport(@JsonProperty("id") String id,
      @JsonProperty("benchResultIds") List<String> benchResultIds) {
    this.id = id;
    this.benchResultIds = benchResultIds;
  }

  public List<String> getBenchResultIds() {
    return benchResultIds;
  }

  public void setProject(Project project) {
    this.url = project.getBaseUrl() + "/analysis/" + id;
  }

  public String getUrl() {
    return url;
  }

}
