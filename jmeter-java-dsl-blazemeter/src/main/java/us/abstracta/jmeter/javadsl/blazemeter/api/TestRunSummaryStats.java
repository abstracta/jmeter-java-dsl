package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public class TestRunSummaryStats {

  public final List<TestRunLabeledSummary> summary;

  @JsonCreator
  private TestRunSummaryStats(@JsonProperty("summary") List<TestRunLabeledSummary> summary) {
    this.summary = summary;
  }

  public static class TestRunLabeledSummary {

    public final Instant first;
    public final Instant last;

    @JsonCreator
    private TestRunLabeledSummary(@JsonProperty("first") Instant first,
        @JsonProperty("last") Instant last) {
      this.first = first;
      this.last = last;
    }

  }

}
