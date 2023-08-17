package us.abstracta.jmeter.javadsl.azure.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Secret {

  private final SecretType type;
  private final String value;

  @JsonCreator
  public Secret(@JsonProperty("type") SecretType type, @JsonProperty("value") String value) {
    this.type = type;
    this.value = value;
  }

  public enum SecretType {
    AKV_SECRET_URI, SECRET_VALUE
  }

}
