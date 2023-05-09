package us.abstracta.jmeter.javadsl.engines;

import java.time.Duration;
import us.abstracta.jmeter.javadsl.core.stats.TimeMetricSummary;

/**
 * Allows to easily store information associated to a time metric collected by remote engine
 * service.
 *
 * @since 1.10
 */
public class RemoteEngineTimeMetricSummary implements TimeMetricSummary {

  protected final Duration min;
  protected final Duration max;
  protected final Duration mean;
  protected final Duration median;
  protected final Duration percentile90;
  protected final Duration percentile95;
  protected final Duration percentile99;

  public RemoteEngineTimeMetricSummary(long min, long max, double mean, double median,
      double percentile90, double percentile95, double percentile99) {
    this.min = Duration.ofMillis(min);
    this.max = Duration.ofMillis(max);
    this.mean = double2Duration(mean);
    this.median = double2Duration(median);
    this.percentile90 = double2Duration(percentile90);
    this.percentile95 = double2Duration(percentile95);
    this.percentile99 = double2Duration(percentile99);
  }

  private Duration double2Duration(double millis) {
    return Duration.ofMillis(Math.round(millis));
  }

  @Override
  public Duration min() {
    return min;
  }

  @Override
  public Duration max() {
    return max;
  }

  @Override
  public Duration mean() {
    return mean;
  }

  @Override
  public Duration median() {
    return median;
  }

  @Override
  public Duration perc90() {
    return percentile90;
  }

  @Override
  public Duration perc95() {
    return percentile95;
  }

  @Override
  public Duration perc99() {
    return percentile99;
  }

}
