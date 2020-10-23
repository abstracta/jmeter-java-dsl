package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiResponse<T> {

  private final T result;

  @JsonCreator
  private ApiResponse(@JsonProperty("result") T result) {
    this.result = result;
  }

  public T getResult() {
    return result;
  }

}
