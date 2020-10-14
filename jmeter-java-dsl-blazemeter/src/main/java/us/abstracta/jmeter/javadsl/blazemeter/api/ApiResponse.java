package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiResponse<T> {

  public final T result;

  @JsonCreator
  private ApiResponse(@JsonProperty("result") T result) {
    this.result = result;
  }

}
