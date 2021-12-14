package us.abstracta.jmeter.javadsl.core.stats;

import java.time.Duration;
import java.time.Instant;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Contains summary statistics of a group of collected sample results.
 *
 * @since 0.1
 */
public interface StatsSummary {

  /**
   * Adds given sample result data to collected statistics.
   *
   * @param result contains new data to include in collected statistics.
   * @since 0.37
   */
  void add(SampleResult result);

  /**
   * Gets the instant when the first sample started.
   * <p>
   * When associated to a test plan or transaction it gets its start time.
   */
  Instant firstTime();

  /**
   * Gets the instant when the last sample ended.
   * <p>
   * When associated to a test plan or transaction it gets its end time.
   * <p>
   * Take into consideration that for transactions this time takes not only into consideration the
   * endTime of last sample, but also the time spent in timers and pre and postprocessors.
   */
  Instant endTime();

  /**
   * Gets metrics for number of samples
   * <p>
   * This counts both failing and passing samples.
   *
   * @since 0.37
   */
  CountMetricSummary samples();

  /**
   * Gets the total number of samples.
   */
  default long samplesCount() {
    return samples().total();
  }

  /**
   * Gets metrics for number of samples that failed.
   *
   * @since 0.37
   */
  CountMetricSummary errors();

  /**
   * Gets the total number of samples that failed.
   */
  default long errorsCount() {
    return errors().total();
  }

  /**
   * Gets metrics for time spent in samples.
   */
  TimeMetricSummary sampleTime();

  /**
   * Gets the 99 percentile of samples times.
   * <p>
   * 99% of samples took less or equal to the returned value.
   *
   * @since 0.15
   */
  default Duration sampleTimePercentile99() {
    return sampleTime().perc99();
  }

  /**
   * Gets metrics for received bytes in sample responses.
   */
  CountMetricSummary receivedBytes();

  /**
   * Gets metrics for sent bytes in samples requests.
   */
  CountMetricSummary sentBytes();

}
