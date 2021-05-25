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

  public void setStart(Instant start) {
    ((AggregatingStatsSummary)overallStats).setStart(start);
  }

  public void setEnd(Instant end) {
    ((AggregatingStatsSummary)overallStats).setEnd(end);
  }

  public static class AggregatingStatsSummary implements StatsSummary {

    private final StatisticsSummaryData stats = new StatisticsSummaryData(90, 95, 99);

    private void setStart(Instant start) {
      stats.setFirstTime(start.toEpochMilli());
    }

    private void setEnd(Instant end) {
      stats.setEndTime(end.toEpochMilli());
    }

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
    public Duration minSampleTime() {
      return Duration.ofMillis(stats.getMin());
    }

    @Override
    public Duration maxSampleTime() {
      return Duration.ofMillis(stats.getMax());
    }

    @Override
    public Duration meanSampleTime() {
      return Duration.ofMillis((long) stats.getMean().getResult());
    }

    @Override
    public Duration sampleTimePercentile90() {
      return Duration.ofMillis((long) stats.getPercentile1().getResult());
    }

    @Override
    public Duration sampleTimePercentile95() {
      return Duration.ofMillis((long) stats.getPercentile2().getResult());
    }

    @Override
    public Duration sampleTimePercentile99() {
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

  public synchronized void addSampleResult(SampleResult result) {
    ((AggregatingStatsSummary) overallStats).addResult(result);
    AggregatingStatsSummary labelStats = (AggregatingStatsSummary) labeledStats
        .computeIfAbsent(result.getSampleLabel(), label -> new AggregatingStatsSummary());
    labelStats.addResult(result);
  }

}
