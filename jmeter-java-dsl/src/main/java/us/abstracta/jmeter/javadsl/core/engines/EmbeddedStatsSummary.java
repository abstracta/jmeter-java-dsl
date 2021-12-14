package us.abstracta.jmeter.javadsl.core.engines;

import java.time.Duration;
import java.time.Instant;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.PSquarePercentile;
import org.apache.jmeter.samplers.SampleResult;
import us.abstracta.jmeter.javadsl.core.stats.CountMetricSummary;
import us.abstracta.jmeter.javadsl.core.stats.StatsSummary;
import us.abstracta.jmeter.javadsl.core.stats.TimeMetricSummary;

/**
 * Contains statistics collected by {@link EmbeddedJmeterEngine}.
 * <p>
 * You can use this class to collect additional statistics by extending it and using it with {@link
 * us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor}.
 *
 * @since 0.37
 */
public class EmbeddedStatsSummary implements StatsSummary {

  private long firstTime = Long.MAX_VALUE;
  private long endTime = Long.MIN_VALUE;
  private final CountMetricSummary samples = new CountMetricSummary();
  private final CountMetricSummary errors = new CountMetricSummary();
  private final CountMetricSummary receivedBytes = new CountMetricSummary();
  private final CountMetricSummary sentBytes = new CountMetricSummary();
  private final EmbeddedTimeMetricSummary sampleTime = new EmbeddedTimeMetricSummary();

  public void add(SampleResult result) {
    firstTime = Math.min(firstTime, result.getStartTime());
    endTime = Math.max(endTime, result.getEndTime());
    long elapsedTimeMillis = endTime - firstTime;
    samples.increment(1, elapsedTimeMillis);
    if (!result.isSuccessful()) {
      errors.increment(1, elapsedTimeMillis);
    }
    receivedBytes.increment(result.getBytesAsLong(), elapsedTimeMillis);
    sentBytes.increment(result.getSentBytes(), elapsedTimeMillis);
    sampleTime.add(result.getTime());
  }

  @Override
  public Instant firstTime() {
    return Instant.ofEpochMilli(firstTime);
  }

  @Override
  public Instant endTime() {
    return Instant.ofEpochMilli(endTime);
  }

  @Override
  public CountMetricSummary samples() {
    return samples;
  }

  @Override
  public CountMetricSummary errors() {
    return errors;
  }

  @Override
  public TimeMetricSummary sampleTime() {
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

  public static class EmbeddedTimeMetricSummary implements TimeMetricSummary {

    private final PSquarePercentile median = new PSquarePercentile(50);
    private final PSquarePercentile percentile90 = new PSquarePercentile(90);
    private final PSquarePercentile percentile95 = new PSquarePercentile(95);
    private final PSquarePercentile percentile99 = new PSquarePercentile(99);
    private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;
    private final Mean mean = new Mean();

    public void add(long val) {
      min = Math.min(min, val);
      max = Math.max(max, val);
      median.increment(val);
      percentile90.increment(val);
      percentile95.increment(val);
      percentile99.increment(val);
      mean.increment(val);
    }

    @Override
    public Duration min() {
      return Duration.ofMillis(min);
    }

    @Override
    public Duration max() {
      return Duration.ofMillis(max);
    }

    @Override
    public Duration mean() {
      return double2Duration(mean.getResult());
    }

    private Duration double2Duration(double millis) {
      return Duration.ofMillis(Math.round(millis));
    }

    @Override
    public Duration median() {
      return double2Duration(median.getResult());
    }

    @Override
    public Duration perc90() {
      return double2Duration(percentile90.getResult());
    }

    @Override
    public Duration perc95() {
      return double2Duration(percentile95.getResult());
    }

    @Override
    public Duration perc99() {
      return double2Duration(percentile99.getResult());
    }

  }

}
