package us.abstracta.jmeter.javadsl.azure;

import java.time.Duration;
import us.abstracta.jmeter.javadsl.azure.api.TestRun;
import us.abstracta.jmeter.javadsl.azure.api.TransactionStats;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.engines.BaseRemoteEngineStatsSummary;

public class AzureTestPlanStats extends TestPlanStats {

  public AzureTestPlanStats(TestRun testRun) {
    super(() -> null);
    setStart(testRun.getStartTime());
    setEnd(testRun.getEndTime());
    testRun.getTestRunStatistics().forEach((label, stat) -> {
      AzureStatsSummary statsSummary = new AzureStatsSummary(stat, testRun,
          Duration.between(testRun.getStartTime(), testRun.getEndTime()).toMillis());
      if ("Total".equals(label)) {
        overallStats = statsSummary;
      } else {
        this.labeledStats.put(label, statsSummary);
      }
    });
  }

  private static class AzureStatsSummary extends BaseRemoteEngineStatsSummary {

    private AzureStatsSummary(TransactionStats stats, TestRun testRun, long elapsedTimeMillis) {
      super(
          /*
         These two values are approximations, since Azure api does not provide such information per
         label and calculating it from result logs would incur in significant additional time and
         resources usage.
         */
          testRun.getStartTime(),
          testRun.getEndTime(),
          elapsedTimeMillis,
          stats.getSamples(),
          stats.getErrorsCount(),
          stats.getMinResponseTime(),
          stats.getMaxResponseTime(),
          stats.getAvgResponseTime(),
          stats.getMedianResponseTime(),
          stats.getPerc90(),
          stats.getPerc95(),
          stats.getPerc99(),
          stats.getReceivedAvgBytes(),
          stats.getSentAvgBytes());
    }

  }

}
