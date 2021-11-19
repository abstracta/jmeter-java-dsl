package us.abstracta.jmeter.javadsl.core.engines;

import java.awt.Component;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.DslJmeterEngine;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.listeners.DslVisualizer;

/**
 * Allows running test plans in an embedded JMeter instance.
 *
 * @since 0.1
 */
public class EmbeddedJmeterEngine implements DslJmeterEngine {

  private static final Logger LOG = LoggerFactory.getLogger(EmbeddedJmeterEngine.class);

  @Override
  public TestPlanStats run(DslTestPlan testPlan) throws IOException {
    return runInEnv(testPlan, new JmeterEnvironment());
  }

  protected TestPlanStats runInEnv(DslTestPlan testPlan, JmeterEnvironment env) {
    HashTree rootTree = new ListedHashTree();
    BuildTreeContext buildContext = new BuildTreeContext(rootTree);
    HashTree testPlanTree = testPlan.buildTreeUnder(rootTree, buildContext);

    AggregatingTestPlanStats stats = new AggregatingTestPlanStats();
    addStatsCollector(testPlanTree, stats);
    testPlanTree.add(new ResultCollector(new Summariser()));

    List<Future<Void>> closedVisualizers = Collections.emptyList();
    if (!buildContext.getVisualizers().isEmpty()) {
      // this is required for proper visualization of labels and messages from resources bundle
      env.initLocale();
      closedVisualizers = showVisualizers(buildContext.getVisualizers());
    }
    Runnable testRunner = buildTestRunner(testPlanTree, rootTree);
    /*
     we register the start and end of test since calculating it from sample results may be
     inaccurate when timers or post processors are used outside of transactions, since such time
     is not included in sample results. Additionally, we want to provide a consistent meaning for
     start, end and elapsed time for samplers, transactions and test plan (which would not be if
     we only use sample results times).
     */
    stats.setStart(Instant.now());
    testRunner.run();
    stats.setEnd(Instant.now());
    awaitAllClosedVisualizers(closedVisualizers);
    return stats;
  }

  protected void addStatsCollector(HashTree testPlanTree, AggregatingTestPlanStats stats) {
    ResultCollector collector = new ResultCollector();
    Visualizer statsVisualizer = new Visualizer() {

      @Override
      public void add(SampleResult r) {
        stats.addSampleResult(r);
      }

      @Override
      public boolean isStats() {
        return true;
      }

    };
    collector.setListener(statsVisualizer);
    testPlanTree.add(collector);
    testPlanTree.add(statsVisualizer);
  }

  protected Runnable buildTestRunner(HashTree testPlanTree, HashTree rootTree) {
    StandardJMeterEngine engine = new StandardJMeterEngine();
    engine.configure(rootTree);
    return engine;
  }

  private List<Future<Void>> showVisualizers(Map<DslVisualizer, Supplier<Component>> visualizers) {
    return visualizers.entrySet().stream()
        .map(e -> {
          CompletableFuture<Void> closedVisualizer = new CompletableFuture<>();
          e.getKey().showTestElementGui(e.getValue(), () -> closedVisualizer.complete(null));
          return closedVisualizer;
        })
        .collect(Collectors.toList());
  }

  public void awaitAllClosedVisualizers(List<Future<Void>> closedVisualizers) {
    try {
      for (Future<Void> closedVisualizer : closedVisualizers) {
        try {
          closedVisualizer.get();
        } catch (ExecutionException e) {
          LOG.warn("Problem waiting for a visualizer to close", e);
        }
      }
    } catch (InterruptedException e) {
      //just stop waiting for visualizers and reset interrupted flag
      Thread.interrupted();
    }
  }

}
