package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Workspace {

  public final long accountId;

  @JsonCreator
  private Workspace(@JsonProperty("accountId") long accountId) {
    this.accountId = accountId;
  }

}
