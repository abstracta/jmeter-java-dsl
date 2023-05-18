package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;

public class TestRun {

  private final long id;
  private final String reportStatus;
  private final List<ReportMessage> aggregatedMessages;
  private String url;

  @JsonCreator
  private TestRun(@JsonProperty("id") long id, @JsonProperty("reportStatus") String reportStatus,
      @JsonProperty("aggregatedMessages") List<ReportMessage> aggregatedMessages) {
    this.id = id;
    this.reportStatus = reportStatus;
    this.aggregatedMessages = aggregatedMessages;
  }

  public void setTest(Test test) {
    this.url = test.getProject().getUrl() + "/masters/" + id;
  }

  public long getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }

  public boolean isErrorStatus() {
    return "error".equals(reportStatus);
  }

  public List<String> getErrorMessages() {
    return aggregatedMessages.stream()
        .filter(m -> "ERROR".equals(m.level))
        .map(m -> m.message)
        .collect(Collectors.toList());
  }

  private static class ReportMessage {

    private final String message;
    private final String level;

    @JsonCreator
    private ReportMessage(@JsonProperty("message") String message,
        @JsonProperty("level") String level) {
      this.message = message;
      this.level = level;
    }

  }

}
