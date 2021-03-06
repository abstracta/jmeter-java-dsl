package us.abstracta.jmeter.javadsl.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains all statistics collected during the execution of a test plan.
 * <p>
 * When using different samples, specify different names on them to be able to get each sampler
 * specific statistics after they run.
 */
public class TestPlanStats {

  protected StatsSummary overallStats;
  protected final Map<String, StatsSummary> labeledStats = new ConcurrentHashMap<>();

  public interface StatsSummary {

    /**
     * Gets the instant when the first sample started.
     *
     * When associated to a test plan or transaction it gets it's start time.
     */
    Instant firstTime();

    /**
     * Gets the instant when the last sample ended.
     *
     * When associated to a test plan or transaction it gets it's end time.
     *
     * Take into consideration that for transactions this time takes not only into consideration the
     * endTime of last sample, but also the time spent in timers and pre and post processors.
     */
    Instant endTime();

    /**
     * Gets the duration between {@link #firstTime()} and {@link #endTime()}.
     *
     * It is a simple way to get the duration of a sample, transaction or test plan.
     */
    Duration elapsedTime();

    /**
     * Gets the total number of samples.
     *
     * This counts both failing and passing samples.
     */
    long samplesCount();

    /**
     * Gets the average count of samples per second.
     *
     * This is just {@link #samplesCount()}/{@link #elapsedTime()}.
     */
    double samplesPerSecond();

    /**
     * Gets the total number of samples that failed.
     */
    long errorsCount();

    /**
     * Gets the minimum time spent in a sample.
     */
    Duration minSampleTime();

    /**
     * Gets the maximum time spent in a sample.
     */
    Duration maxSampleTime();

    /**
     * Gets the mean value of samples times.
     */
    Duration meanSampleTime();

    /**
     * Gets the 90 percentile of samples times.
     *
     * 90% of samples took less or equal to the returned value.
     */
    Duration sampleTimePercentile90();

    /**
     * Gets the 95 percentile of samples times.
     *
     * 95% of samples took less or equal to the returned value.
     */
    Duration sampleTimePercentile95();

    /**
     * Gets the 99 percentile of samples times.
     *
     * 99% of samples took less or equal to the returned value.
     */
    Duration sampleTimePercentile99();

    /**
     * Gets the total sum of received bytes in samples responses.
     */
    long receivedBytes();

    /**
     * Gets the average amount of received bytes per second in samples responses.
     *
     * This is just {@link #receivedBytes()}/{@link #elapsedTime()}.
     */
    double receivedBytesPerSecond();

    /**
     * Gets the total sum of sent bytes in samples requests.
     */
    long sentBytes();

    /**
     * Gets the average amount of sent bytes per second in samples requests.
     *
     * This is just {@link #sentBytes()}/{@link #elapsedTime()}.
     */
    double sentBytesPerSecond();

  }

  public void setLabeledStats(String label, StatsSummary stats) {
    labeledStats.put(label, stats);
  }

  public void setOverallStats(StatsSummary stats) {
    overallStats = stats;
  }

  public StatsSummary overall() {
    return overallStats;
  }

  public StatsSummary byLabel(String label) {
    return labeledStats.get(label);
  }

  public Set<String> labels() {
    return labeledStats.keySet();
  }

}
