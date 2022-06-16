package us.abstracta.jmeter.javadsl.octoperf.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class BenchResult {

  private final State state;
  private final Instant created;
  private final Instant lastModified;

  @JsonCreator
  public BenchResult(@JsonProperty("state") State state, @JsonProperty("created") Instant created,
      @JsonProperty("lastModified") Instant lastModified) {
    this.state = state;
    this.created = created;
    this.lastModified = lastModified;
  }

  public State getState() {
    return state;
  }

  public Instant getCreated() {
    return created;
  }

  public Instant getLastModified() {
    return lastModified;
  }

  public enum State {
    CREATED, PENDING, SCALING, PREPARING, INITIALIZING, RUNNING, FINISHED, ABORTED, ERROR;

    public boolean isFinalState() {
      return this == FINISHED || this == ABORTED || this == ERROR;
    }

  }

}
