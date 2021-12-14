package us.abstracta.jmeter.javadsl.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.apache.jmeter.samplers.SampleResult;
import us.abstracta.jmeter.javadsl.core.stats.StatsSummary;

/**
 * Contains all statistics collected during the execution of a test plan.
 * <p>
 * When using different samples, specify different names on them to be able to get each sampler
 * specific statistics after they run.
 *
 * @since 0.1
 */
public class TestPlanStats {

  protected final Supplier<StatsSummary> statsSummaryBuilder;
  protected StatsSummary overallStats;
  protected final Map<String, StatsSummary> labeledStats = new ConcurrentHashMap<>();
  private Instant start;
  private Instant end;

  public TestPlanStats(Supplier<StatsSummary> statsSummaryBuilder) {
    this.statsSummaryBuilder = statsSummaryBuilder;
    overallStats = statsSummaryBuilder.get();
  }

  public synchronized void addSampleResult(SampleResult result) {
    overallStats.add(result);
    StatsSummary labelStats = labeledStats.computeIfAbsent(
        result.getSampleLabel(), label -> statsSummaryBuilder.get());
    labelStats.add(result);
  }

  public void setStart(Instant start) {
    this.start = start;
  }

  public void setEnd(Instant end) {
    this.end = end;
  }

  /**
   * Provides the time taken to run the test plan.
   */
  public Duration duration() {
    return Duration.between(start, end);
  }

  /**
   * Provides statistics for the entire test plan.
   */
  public StatsSummary overall() {
    return overallStats;
  }

  /**
   * Provides statistics for a specific label (usually a sampler label).
   */
  public StatsSummary byLabel(String label) {
    return labeledStats.get(label);
  }

  /**
   * Provides a set of collected labels (usually samplers labels).
   */
  public Set<String> labels() {
    return labeledStats.keySet();
  }

}
