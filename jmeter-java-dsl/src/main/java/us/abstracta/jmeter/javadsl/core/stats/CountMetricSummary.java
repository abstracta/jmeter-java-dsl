package us.abstracta.jmeter.javadsl.core.stats;

/**
 * Provides summary data for a set of count values.
 *
 * @since 0.37
 */
public class CountMetricSummary {

  private long val = 0;
  private long elapsedTimeMillis;

  /**
   * Updates collected summary data with given info.
   * @param inc contains the last value counted.
   * @param elapsedTimeMillis specifies the number of milliseconds since the count started.
   */
  public void increment(long inc, long elapsedTimeMillis) {
    val += inc;
    this.elapsedTimeMillis = elapsedTimeMillis;
  }

  /**
   * Provides the average count per second for the given metric.
   */
  public double perSecond() {
    return val / ((double) elapsedTimeMillis / 1000);
  }

  /**
   * Provides the total count (the sum).
   */
  public long total() {
    return val;
  }

}
