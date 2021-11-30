package us.abstracta.jmeter.javadsl.core.engines;

import java.time.Duration;
import java.time.Instant;
import org.apache.jmeter.samplers.SampleResult;
import us.abstracta.jmeter.javadsl.core.DslStatisticsSummaryData;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

/**
 * A {@link TestPlanStats} which automatically calculate aggregates from sample results.
 */
public class AggregatingTestPlanStats extends TestPlanStats {

  public AggregatingTestPlanStats() {
    setOverallStats(new AggregatingStatsSummary());
  }

  public void setStart(Instant start) {
    ((AggregatingStatsSummary) overallStats).setStart(start);
  }

  public void setEnd(Instant end) {
    ((AggregatingStatsSummary) overallStats).setEnd(end);
  }

  public static class AggregatingStatsSummary implements StatsSummary {

    private final DslStatisticsSummaryData stats = new DslStatisticsSummaryData(90, 95, 99);

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
      updateLatencyTime(result.getLatency());
      updateProcessingTime(result.getTime() - result.getLatency());
      stats.setLatency(result.getLatency());
      stats.setProcessing(result.getTime() - result.getLatency());
      stats.setFirstTime(result.getStartTime());
      stats.setEndTime(result.getEndTime());
    }

    private void updateElapsedTime(long elapsedTime) {
      stats.getPercentile1().addValue(elapsedTime);
      stats.getPercentile2().addValue(elapsedTime);
      stats.getPercentile3().addValue(elapsedTime);
      stats.getMean().addValue(elapsedTime);
      stats.getMedian().addValue(elapsedTime);
      stats.setMin(elapsedTime);
      stats.setMax(elapsedTime);
    }

    private void updateLatencyTime(long latencyTime) {
      stats.getLatencyPercentile1().addValue(latencyTime);
      stats.getLatencyPercentile2().addValue(latencyTime);
      stats.getLatencyPercentile3().addValue(latencyTime);
      stats.getLatencyMean().addValue(latencyTime);
      stats.getLatencyMedian().addValue(latencyTime);
      stats.setLatencyMin(latencyTime);
      stats.setLatencyMax(latencyTime);
      stats.setLatencyMax(latencyTime);
    }

    private void updateProcessingTime(long processingTime) {
      stats.getProcessingPercentile1().addValue(processingTime);
      stats.getProcessingPercentile2().addValue(processingTime);
      stats.getProcessingPercentile3().addValue(processingTime);
      stats.getProcessingMean().addValue(processingTime);
      stats.getProcessingMedian().addValue(processingTime);
      stats.setProcessingMin(processingTime);
      stats.setProcessingMax(processingTime);
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

    @Override
    public Duration minSampleLatencyTime() {
      return Duration.ofMillis(stats.getLatencyMin());
    }
    
    @Override
    public Duration maxSampleLatencyTime() {
      return Duration.ofMillis(stats.getLatencyMax());
    }

    @Override
    public Duration meanSampleLatencyTime() {
      return Duration.ofMillis((long) stats.getLatencyMean().getResult());
    }

    @Override
    public Duration sampleLatencyTimePercentile90() {
      return Duration.ofMillis((long) stats.getLatencyPercentile1().getResult());
    }

    @Override
    public Duration sampleLatencyTimePercentile95() {
      return Duration.ofMillis((long) stats.getLatencyPercentile2().getResult());
    }

    @Override
    public Duration sampleLatencyTimePercentile99() {
      return Duration.ofMillis((long) stats.getLatencyPercentile3().getResult());
    }

    @Override
    public Duration minSampleProcessingTime() {
      return Duration.ofMillis(stats.getProcessingMin());
    }

    @Override
    public Duration maxSampleProcessingTime() {
      return Duration.ofMillis(stats.getProcessingMax());
    }

    @Override
    public Duration meanSampleProcessingTime() {
      return Duration.ofMillis((long) stats.getProcessingMean().getResult());
    }

    @Override
    public Duration sampleProcessingTimePercentile90() {
      return Duration.ofMillis((long) stats.getProcessingPercentile1().getResult());
    }

    @Override
    public Duration sampleProcessingTimePercentile95() {
      return Duration.ofMillis((long) stats.getProcessingPercentile2().getResult());
    }

    @Override
    public Duration sampleProcessingTimePercentile99() {
      return Duration.ofMillis((long) stats.getProcessingPercentile3().getResult());
    }

    @Override
    public Duration processingTime() {
      return Duration.ofMillis(stats.getProcessingTime());
    }

    @Override
    public Duration latencyTime() {
      return Duration.ofMillis(stats.getLatencyTime());
    }

  }

  public synchronized void addSampleResult(SampleResult result) {
    ((AggregatingStatsSummary) overallStats).addResult(result);
    AggregatingStatsSummary labelStats = (AggregatingStatsSummary) labeledStats
        .computeIfAbsent(result.getSampleLabel(), label -> new AggregatingStatsSummary());
    labelStats.addResult(result);
  }

}
