package us.abstracta.jmeter.javadsl.blazemeter;

import java.util.List;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunRequestStats;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunSummaryStats.TestRunLabeledSummary;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.engines.BaseRemoteEngineStatsSummary;
import us.abstracta.jmeter.javadsl.engines.RemoteEngineTimeMetricSummary;

public class BlazeMeterTestPlanStats extends TestPlanStats {

  public BlazeMeterTestPlanStats(TestRunLabeledSummary summary,
      List<TestRunRequestStats> labeledStats) {
    super(() -> null);
    for (TestRunRequestStats labeledStat : labeledStats) {
      BlazemeterStatsSummary labelStatsSummary = new BlazemeterStatsSummary(labeledStat, summary);
      if ("ALL".equals(labeledStat.getLabelName())) {
        overallStats = labelStatsSummary;
        setStart(labelStatsSummary.firstTime());
        setEnd(labelStatsSummary.endTime());
      } else {
        this.labeledStats.put(labeledStat.getLabelName(), labelStatsSummary);
      }
    }
  }

  private static class BlazemeterStatsSummary extends BaseRemoteEngineStatsSummary {

    private BlazemeterStatsSummary(TestRunRequestStats labeledStat, TestRunLabeledSummary summary) {
      /*
       These two values are approximations, since BZ api does not provide such information per label
       and calculating it from result logs would incur in significant additional time and resources
       usage.
       */
      super(summary.getFirst(),
          summary.getLast(),
          labeledStat.getDuration(),
          labeledStat.getSamples(),
          labeledStat.getErrorsCount(),
          labeledStat.getMinResponseTime(),
          labeledStat.getMaxResponseTime(),
          labeledStat.getAvgResponseTime(),
          labeledStat.getMedianResponseTime(),
          labeledStat.getPerc90(),
          labeledStat.getPerc95(),
          labeledStat.getPerc99(),
          labeledStat.getAvgBytes(),
          0.0);
    }

  }

  // This class has been left to avoid breaking api compatibility
  private static class BlazeMeterTimeMetricSummary extends RemoteEngineTimeMetricSummary {

    private BlazeMeterTimeMetricSummary(long min, long max, double mean, double median,
        double percentile90, double percentile95, double percentile99) {
      super(min, max, mean, median, percentile90, percentile95, percentile99);
    }

  }

}
