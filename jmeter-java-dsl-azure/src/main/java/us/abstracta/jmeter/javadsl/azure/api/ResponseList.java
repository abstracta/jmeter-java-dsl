package us.abstracta.jmeter.javadsl.azure.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ResponseList<T> {

  private final List<T> value;

  @JsonCreator
  public ResponseList(@JsonProperty("value") List<T> value) {
    this.value = value;
  }

  @JsonIgnore
  public Optional<T> getFirstElement() {
    return value.isEmpty() ? Optional.empty() : Optional.of(value.get(0));
  }

  public Stream<T> stream() {
    return value.stream();
  }

}
