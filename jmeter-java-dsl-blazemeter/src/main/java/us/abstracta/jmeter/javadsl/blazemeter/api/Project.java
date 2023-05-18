package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Project {

  private final long id;
  private final long workspaceId;
  private long accountId;
  private String url;

  @JsonCreator
  public Project(@JsonProperty("id") long id, @JsonProperty("workspaceId") long workspaceId,
      @JsonProperty("accountId") Long accountId) {
    this.id = id;
    this.workspaceId = workspaceId;
    if (accountId != null) {
      this.accountId = accountId;
    }
  }

  public void setBaseUrl(String baseUrl) {
    url = baseUrl + String
        .format("/accounts/%d/workspaces/%d/projects/%d", accountId, workspaceId, id);
  }

  public long getId() {
    return id;
  }

  public long getAccountId() {
    return accountId;
  }

  public void setAccountId(long accountId) {
    this.accountId = accountId;
  }

  public long getWorkspaceId() {
    return workspaceId;
  }

  public String getUrl() {
    return url;
  }

}
