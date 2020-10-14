package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestRunRequestStats {

  public final double avgBytes;
  public final double avgResponseTime;
  public final double avgThroughput;
  public final long duration;
  public final long errorsCount;
  public final String labelName;
  public final long perc90;
  public final long perc95;
  public final long perc99;
  public final long maxResponseTime;
  public final long minResponseTime;
  public final long samples;

  @JsonCreator
  public TestRunRequestStats(@JsonProperty("avgBytes") double avgBytes,
      @JsonProperty("avgResponseTime") double avgResponseTime,
      @JsonProperty("avgThroughput") double avgThroughput,
      @JsonProperty("duration") long duration,
      @JsonProperty("errorsCount") long errorsCount,
      @JsonProperty("labelName") String labelName,
      @JsonProperty("90line") long perc90,
      @JsonProperty("95line") long perc95,
      @JsonProperty("99line") long perc99,
      @JsonProperty("maxResponseTime") long maxResponseTime,
      @JsonProperty("minResponseTime") long minResponseTime,
      @JsonProperty("samples") long samples) {
    this.avgBytes = avgBytes;
    this.avgResponseTime = avgResponseTime;
    this.avgThroughput = avgThroughput;
    this.duration = duration;
    this.errorsCount = errorsCount;
    this.labelName = labelName;
    this.perc90 = perc90;
    this.perc95 = perc95;
    this.perc99 = perc99;
    this.maxResponseTime = maxResponseTime;
    this.minResponseTime = minResponseTime;
    this.samples = samples;
  }

}
