package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class TestRunStatus {

  public static final TestRunStatus CREATED = new TestRunStatus("CREATED", false);
  public static final TestRunStatus ENDED = new TestRunStatus("ENDED", false);

  private final String status;
  private final boolean isDataAvailable;

  @JsonCreator
  private TestRunStatus(@JsonProperty("status") String status,
      @JsonProperty("isDataAvailable") boolean isDataAvailable) {
    this.status = status;
    this.isDataAvailable = isDataAvailable;
  }

  public boolean isDataAvailable() {
    return isDataAvailable;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TestRunStatus that = (TestRunStatus) o;
    return Objects.equals(status, that.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status);
  }

  @Override
  public String toString() {
    return status;
  }

}
