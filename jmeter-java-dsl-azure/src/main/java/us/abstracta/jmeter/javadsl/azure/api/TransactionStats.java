package us.abstracta.jmeter.javadsl.azure.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionStats {

  private final long sampleCount;
  private final long errorCount;
  private final double meanResTime;
  private final long medianResTime;
  private final long maxResTime;
  private final long minResTime;
  private final long pct1ResTime;
  private final long pct2ResTime;
  private final long pct3ResTime;
  private final double receivedKBytesPerSec;
  private final double sentKBytesPerSec;

  @JsonCreator
  public TransactionStats(@JsonProperty("sampleCount") long sampleCount,
      @JsonProperty("errorCount") long errorCount, @JsonProperty("meanResTime") double meanResTime,
      @JsonProperty("medianResTime") long medianResTime,
      @JsonProperty("maxResTime") long maxResTime, @JsonProperty("minResTime") long minResTime,
      @JsonProperty("pct1ResTime") long pct1ResTime, @JsonProperty("pct2ResTime") long pct2ResTime,
      @JsonProperty("pct3ResTime") long pct3ResTime,
      @JsonProperty("receivedKBytesPerSec") double receivedKBytesPerSec,
      @JsonProperty("sentKBytesPerSec") double sentKBytesPerSec) {
    this.sampleCount = sampleCount;
    this.errorCount = errorCount;
    this.meanResTime = meanResTime;
    this.medianResTime = medianResTime;
    this.maxResTime = maxResTime;
    this.minResTime = minResTime;
    this.pct1ResTime = pct1ResTime;
    this.pct2ResTime = pct2ResTime;
    this.pct3ResTime = pct3ResTime;
    this.receivedKBytesPerSec = receivedKBytesPerSec;
    this.sentKBytesPerSec = sentKBytesPerSec;
  }

  public long getSamples() {
    return sampleCount;
  }

  public long getErrorsCount() {
    return errorCount;
  }

  public long getMinResponseTime() {
    return minResTime;
  }

  public long getMaxResponseTime() {
    return maxResTime;
  }

  public double getAvgResponseTime() {
    return meanResTime;
  }

  public double getMedianResponseTime() {
    return medianResTime;
  }

  public double getPerc90() {
    return pct1ResTime;
  }

  public double getPerc95() {
    return pct2ResTime;
  }

  public double getPerc99() {
    return pct3ResTime;
  }

  public double getReceivedAvgBytes() {
    return receivedKBytesPerSec * 1024;
  }

  public double getSentAvgBytes() {
    return sentKBytesPerSec * 1024;
  }

}
