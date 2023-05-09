package us.abstracta.jmeter.javadsl.azure.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Token {

  private final String accessToken;

  @JsonCreator
  public Token(@JsonProperty("access_token") String accessToken) {
    this.accessToken = accessToken;
  }

  public String getAccessToken() {
    return accessToken;
  }

}
