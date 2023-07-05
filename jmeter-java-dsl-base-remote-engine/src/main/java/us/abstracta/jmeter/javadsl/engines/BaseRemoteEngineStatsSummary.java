package us.abstracta.jmeter.javadsl.engines;

import java.time.Instant;
import org.apache.jmeter.samplers.SampleResult;
import us.abstracta.jmeter.javadsl.core.stats.CountMetricSummary;
import us.abstracta.jmeter.javadsl.core.stats.StatsSummary;

/**
 * Contains common logic for statistics summary collected from a remote engine service.
 * @since 1.10
 */
public abstract class BaseRemoteEngineStatsSummary implements StatsSummary {

  protected final Instant firstTime;
  protected final Instant endTime;
  protected final CountMetricSummary samples = new CountMetricSummary();
  protected final CountMetricSummary errors = new CountMetricSummary();
  protected final CountMetricSummary receivedBytes = new CountMetricSummary();
  protected final CountMetricSummary sentBytes = new CountMetricSummary();
  protected final RemoteEngineTimeMetricSummary sampleTime;

  protected BaseRemoteEngineStatsSummary(Instant firstTime, Instant endTime, long elapsedTimeMillis,
      long sampleCount, long errorCount, long minResponseTime, long maxResponseTime,
      double meanResponseTime, double medianResponseTime, double responseTimePerc90,
      double responseTimePerc95, double responseTimePerc99, double receivedBytesPerSec,
      double sentBytesPerSec) {
    this.firstTime = firstTime;
    this.endTime = endTime;
    this.samples.increment(sampleCount, elapsedTimeMillis);
    this.errors.increment(errorCount, elapsedTimeMillis);
    this.sampleTime = new RemoteEngineTimeMetricSummary(minResponseTime, maxResponseTime,
        meanResponseTime, medianResponseTime, responseTimePerc90, responseTimePerc95,
        responseTimePerc99);
    this.receivedBytes.increment(perSecond2Total(receivedBytesPerSec, elapsedTimeMillis),
        elapsedTimeMillis);
    this.sentBytes.increment(perSecond2Total(sentBytesPerSec, elapsedTimeMillis),
        elapsedTimeMillis);
  }

  private static long perSecond2Total(double avgBytes, long elapsedTimeMillis) {
    return Math.round(avgBytes / 1000 * elapsedTimeMillis);
  }

  @Override
  public void add(SampleResult result) {
  }

  @Override
  public Instant firstTime() {
    return firstTime;
  }

  @Override
  public Instant endTime() {
    return endTime;
  }

  @Override
  public CountMetricSummary samples() {
    return samples;
  }

  @Override
  public CountMetricSummary errors() {
    return errors;
  }

  public RemoteEngineTimeMetricSummary sampleTime() {
    return sampleTime;
  }

  @Override
  public CountMetricSummary receivedBytes() {
    return receivedBytes;
  }

  @Override
  public CountMetricSummary sentBytes() {
    return sentBytes;
  }

}
