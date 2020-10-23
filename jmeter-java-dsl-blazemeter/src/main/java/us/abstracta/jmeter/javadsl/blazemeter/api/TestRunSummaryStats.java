package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public class TestRunSummaryStats {

  private final List<TestRunLabeledSummary> summary;

  @JsonCreator
  private TestRunSummaryStats(@JsonProperty("summary") List<TestRunLabeledSummary> summary) {
    this.summary = summary;
  }

  public static class TestRunLabeledSummary {

    private final Instant first;
    private final Instant last;

    @JsonCreator
    private TestRunLabeledSummary(@JsonProperty("first") Instant first,
        @JsonProperty("last") Instant last) {
      this.first = first;
      this.last = last;
    }

    public Instant getFirst() {
      return first;
    }

    public Instant getLast() {
      return last;
    }

  }

  public List<TestRunLabeledSummary> getSummary() {
    return summary;
  }

}
