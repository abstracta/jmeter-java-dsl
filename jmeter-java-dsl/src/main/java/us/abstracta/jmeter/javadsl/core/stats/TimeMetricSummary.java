package us.abstracta.jmeter.javadsl.core.stats;

import java.time.Duration;

/**
 * Provides summary data for a set of timing values.
 *
 * @since 0.37
 */
public interface TimeMetricSummary {

  /**
   * Gets the minimum collected value.
   */
  Duration min();

  /**
   * Gets the maximum collected value.
   */
  Duration max();

  /**
   * Gets the mean/average of collected values.
   */
  Duration mean();

  /**
   * Gets the median of collected values.
   * <p>
   * The median is the same as percentile 50, and is the value for which 50% of the collected values
   * is smaller/greater.
   * <p>
   * This value might differ from {@link #mean()} when distribution of values is not symmetric.
   */
  Duration median();

  /**
   * Gets the 90 percentile of samples times.
   * <p>
   * 90% of samples took less or equal to the returned value.
   */
  Duration perc90();

  /**
   * Gets the 95 percentile of samples times.
   * <p>
   * 95% of samples took less or equal to the returned value.
   */
  Duration perc95();

  /**
   * Gets the 99 percentile of samples times.
   * <p>
   * 99% of samples took less or equal to the returned value.
   */
  Duration perc99();

}
