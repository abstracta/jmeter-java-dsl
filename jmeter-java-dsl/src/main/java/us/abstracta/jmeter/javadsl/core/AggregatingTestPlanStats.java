package us.abstracta.jmeter.javadsl.core;

import java.time.Duration;
import java.time.Instant;
import org.apache.jmeter.report.processor.StatisticsSummaryData;
import org.apache.jmeter.samplers.SampleResult;

/**
 * A {@link TestPlanStats} which automatically calculate aggregates from sample results.
 */
public class AggregatingTestPlanStats extends TestPlanStats {

  public AggregatingTestPlanStats() {
    setOverallStats(new AggregatingStatsSummary());
  }

  public static class AggregatingStatsSummary implements StatsSummary {

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

    @Override
    public Instant firstTime() {
      return Instant.ofEpochMilli(stats.getFirstTime());
    }

    @Override
    public Instant endTime() {
      return Instant.ofEpochMilli(stats.getEndTime());
    }

    @Override
    public Duration elapsedTime() {
      return Duration.ofMillis(stats.getElapsedTime());
    }

    @Override
    public long samplesCount() {
      return stats.getTotal();
    }

    @Override
    public double samplesPerSecond() {
      return stats.getThroughput();
    }

    @Override
    public long errorsCount() {
      return stats.getErrors();
    }

    @Override
    public Duration minElapsedTime() {
      return Duration.ofMillis(stats.getMin());
    }

    @Override
    public Duration maxElapsedTime() {
      return Duration.ofMillis(stats.getMax());
    }

    @Override
    public Duration meanElapsedTime() {
      return Duration.ofMillis((long) stats.getMean().getResult());
    }

    @Override
    public Duration elapsedTimePercentile90() {
      return Duration.ofMillis((long) stats.getPercentile1().getResult());
    }

    @Override
    public Duration elapsedTimePercentile95() {
      return Duration.ofMillis((long) stats.getPercentile2().getResult());
    }

    @Override
    public Duration elapsedTimePercentile99() {
      return Duration.ofMillis((long) stats.getPercentile3().getResult());
    }

    @Override
    public long receivedBytes() {
      return stats.getBytes();
    }

    @Override
    public double receivedBytesPerSecond() {
      return stats.getBytesPerSecond();
    }

    @Override
    public long sentBytes() {
      return stats.getSentBytes();
    }

    @Override
    public double sentBytesPerSecond() {
      return stats.getSentBytesPerSecond();
    }

  }

  public void addSampleResult(SampleResult result) {
    ((AggregatingStatsSummary) overallStats).addResult(result);
    AggregatingStatsSummary labelStats = (AggregatingStatsSummary) labeledStats
        .computeIfAbsent(result.getSampleLabel(), label -> new AggregatingStatsSummary());
    labelStats.addResult(result);
  }

}
