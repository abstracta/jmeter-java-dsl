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

    Instant firstTime();

    Instant endTime();

    Duration elapsedTime();

    long samplesCount();

    double samplesPerSecond();

    long errorsCount();

    Duration minElapsedTime();

    Duration maxElapsedTime();

    Duration meanElapsedTime();

    Duration elapsedTimePercentile90();

    Duration elapsedTimePercentile95();

    Duration elapsedTimePercentile99();

    long receivedBytes();

    double receivedBytesPerSecond();

    long sentBytes();

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
