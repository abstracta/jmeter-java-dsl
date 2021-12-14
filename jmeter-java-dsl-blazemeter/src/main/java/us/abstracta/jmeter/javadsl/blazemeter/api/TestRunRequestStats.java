package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestRunRequestStats {

  private final double avgBytes;
  private final double avgResponseTime;
  private final double avgThroughput;
  private final long duration;
  private final long errorsCount;
  private final String labelName;
  private final long medianResponseTime;
  private final long perc90;
  private final long perc95;
  private final long perc99;
  private final long maxResponseTime;
  private final long minResponseTime;
  private final long samples;

  @JsonCreator
  public TestRunRequestStats(@JsonProperty("avgBytes") double avgBytes,
      @JsonProperty("avgResponseTime") double avgResponseTime,
      @JsonProperty("avgThroughput") double avgThroughput,
      @JsonProperty("duration") long duration,
      @JsonProperty("errorsCount") long errorsCount,
      @JsonProperty("labelName") String labelName,
      @JsonProperty("medianResponseTime") long medianResponseTime,
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
    this.medianResponseTime = medianResponseTime;
    this.perc90 = perc90;
    this.perc95 = perc95;
    this.perc99 = perc99;
    this.maxResponseTime = maxResponseTime;
    this.minResponseTime = minResponseTime;
    this.samples = samples;
  }

  public double getAvgBytes() {
    return avgBytes;
  }

  public double getAvgResponseTime() {
    return avgResponseTime;
  }

  public double getAvgThroughput() {
    return avgThroughput;
  }

  public long getDuration() {
    return duration;
  }

  public long getErrorsCount() {
    return errorsCount;
  }

  public String getLabelName() {
    return labelName;
  }

  public long getMedianResponseTime() {
    return medianResponseTime;
  }

  public long getPerc90() {
    return perc90;
  }

  public long getPerc95() {
    return perc95;
  }

  public long getPerc99() {
    return perc99;
  }

  public long getMaxResponseTime() {
    return maxResponseTime;
  }

  public long getMinResponseTime() {
    return minResponseTime;
  }

  public long getSamples() {
    return samples;
  }

}
