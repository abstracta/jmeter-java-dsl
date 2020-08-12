package us.abstracta.jmeter.javadsl.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.jmeter.report.processor.StatisticsSummaryData;
import org.apache.jmeter.samplers.SampleResult;

/**
 * This class contains all statistics collected during the execution of a test plan.
 *
 * When using different samples, specify different names on them to be able to get each sampler
 * specific statistics after they run.
 */
public class TestPlanStats {

  private final StatsSummary overallStats = new StatsSummary();
  private final Map<String, StatsSummary> labeledStats = new ConcurrentHashMap<>();

  public static class StatsSummary {

    private final StatisticsSummaryData stats = new StatisticsSummaryData(90, 95, 99);

    private void addResult(SampleResult result) {
      stats.incTotal();
      stats.incBytes(result.getBytesAsLong());
      stats.incSentBytes(result.getSentBytes());
      if (!result.isSuccessful()) {
        stats.incErrors();
      }
      updateElapsedTime(result.getTime());
      stats.setFirstTime(result.getStartTime());
      stats.setEndTime(result.getEndTime());
    }

    private void updateElapsedTime(long elapsedTime) {
      stats.getPercentile1().addValue(elapsedTime);
      stats.getPercentile2().addValue(elapsedTime);
      stats.getPercentile3().addValue(elapsedTime);
      stats.getMean().addValue(elapsedTime);
      stats.setMin(elapsedTime);
      stats.setMax(elapsedTime);
    }

    public Instant firstTime() {
      return Instant.ofEpochMilli(stats.getFirstTime());
    }

    public Instant endTime() {
      return Instant.ofEpochMilli(stats.getEndTime());
    }

    public Duration elapsedTime() {
      return Duration.ofMillis(stats.getElapsedTime());
    }

    public long samplesCount() {
      return stats.getTotal();
    }

    public double samplesPerSecond() {
      return stats.getThroughput();
    }

    public long errorsCount() {
      return stats.getErrors();
    }

    public Duration minElapsedTime() {
      return Duration.ofMillis(stats.getMin());
    }

    public Duration maxElapsedTime() {
      return Duration.ofMillis(stats.getMax());
    }

    public Duration meanElapsedTime() {
      return Duration.ofMillis((long) stats.getMean().getResult());
    }

    public Duration elapsedTimePercentile90() {
      return Duration.ofMillis((long) stats.getPercentile1().getResult());
    }

    public Duration elapsedTimePercentile95() {
      return Duration.ofMillis((long) stats.getPercentile2().getResult());
    }

    public Duration elapsedTimePercentile99() {
      return Duration.ofMillis((long) stats.getPercentile3().getResult());
    }

    public long receivedBytes() {
      return stats.getBytes();
    }

    public double receivedBytesPerSecond() {
      return stats.getBytesPerSecond();
    }

    public long sentBytes() {
      return stats.getSentBytes();
    }

    public double sentBytesPerSecond() {
      return stats.getSentBytesPerSecond();
    }

  }

  public void addSampleResult(SampleResult result) {
    overallStats.addResult(result);
    labeledStats
        .computeIfAbsent(result.getSampleLabel(), label -> new StatsSummary())
        .addResult(result);
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
