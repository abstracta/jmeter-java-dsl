package us.abstracta.jmeter.javadsl.blazemeter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.apache.jmeter.samplers.SampleResult;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunRequestStats;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunSummaryStats.TestRunLabeledSummary;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.stats.CountMetricSummary;
import us.abstracta.jmeter.javadsl.core.stats.StatsSummary;
import us.abstracta.jmeter.javadsl.core.stats.TimeMetricSummary;

public class BlazeMeterTestPlanStats extends TestPlanStats {

  public BlazeMeterTestPlanStats(TestRunLabeledSummary summary,
      List<TestRunRequestStats> labeledStats) {
    super(() -> null);
    for (TestRunRequestStats labeledStat : labeledStats) {
      BlazemeterStatsSummary labelStatsSummary = new BlazemeterStatsSummary(labeledStat, summary);
      if ("ALL".equals(labeledStat.getLabelName())) {
        overallStats = labelStatsSummary;
        setStart(labelStatsSummary.firstTime);
        setEnd(labelStatsSummary.endTime);
      } else {
        this.labeledStats.put(labeledStat.getLabelName(), labelStatsSummary);
      }
    }
  }

  private static class BlazemeterStatsSummary implements StatsSummary {

    private final Instant firstTime;
    private final Instant endTime;
    private final CountMetricSummary samples = new CountMetricSummary();
    private final CountMetricSummary errors = new CountMetricSummary();
    private final CountMetricSummary receivedBytes = new CountMetricSummary();
    private final BlazeMeterTimeMetricSummary sampleTime;

    private BlazemeterStatsSummary(TestRunRequestStats labeledStat, TestRunLabeledSummary summary) {
      /*
       These two values are approximations, since BZ api does not provide such information per label
       and calculating it from result logs would incur in significant additional time and resources
       usage.
       */
      firstTime = summary.getFirst();
      endTime = summary.getLast();
      long elapsedTimeMillis = labeledStat.getDuration();
      samples.increment(labeledStat.getSamples(), elapsedTimeMillis);
      errors.increment(labeledStat.getErrorsCount(), elapsedTimeMillis);
      sampleTime = new BlazeMeterTimeMetricSummary(labeledStat.getMinResponseTime(),
          labeledStat.getMaxResponseTime(), labeledStat.getAvgResponseTime(),
          labeledStat.getMedianResponseTime(), labeledStat.getPerc90(), labeledStat.getPerc95(),
          labeledStat.getPerc99());
      // Similar comment as with firstTime and endTime: this is just an approximation.
      receivedBytes.increment(Math.round(labeledStat.getAvgBytes() / 1000 * elapsedTimeMillis),
          elapsedTimeMillis);
    }

    @Override
    public void add(SampleResult result) {
    }

    @Override
    public Instant firstTime() {
      return firstTime;
    }

    @Override
    public Instant endTime() {
      return endTime;
    }

    @Override
    public CountMetricSummary samples() {
      return samples;
    }

    @Override
    public CountMetricSummary errors() {
      return errors;
    }

    public BlazeMeterTimeMetricSummary sampleTime() {
      return sampleTime;
    }

    @Override
    public CountMetricSummary receivedBytes() {
      return receivedBytes;
    }

    @Override
    public CountMetricSummary sentBytes() {
      throw new UnsupportedOperationException(
          "BlazeMeter API does not provide an efficient way to get this value.");
    }

  }

  private static class BlazeMeterTimeMetricSummary implements TimeMetricSummary {

    private final Duration min;
    private final Duration max;
    private final Duration mean;
    private final Duration median;
    private final Duration percentile90;
    private final Duration percentile95;
    private final Duration percentile99;

    private BlazeMeterTimeMetricSummary(long min, long max, double mean, double median,
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

}
