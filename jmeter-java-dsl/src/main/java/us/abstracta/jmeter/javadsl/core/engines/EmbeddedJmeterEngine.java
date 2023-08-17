package us.abstracta.jmeter.javadsl.core.engines;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
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
  private final Map<String, Object> props = new HashMap<>();
  private final StandardJMeterEngine engine = new StandardJMeterEngine();
  private String propsFile;

  /**
   * Allows setting properties to be used during test plan execution.
   * <p>
   * This is an alternate to using {@link System#setProperty(String, String)} but that also works
   * with <pre>props['PROP_NAME']</pre> and <pre>props.get('PROP_NAME')</pre> in groovy code, in
   * addition to allowing to set arbitrary objects (and not just strings). This properties
   * additionally don't alter the JVM properties, which is preferable to improve tests isolation.
   * <p>
   * Setting arbitrary objects might be helpful to pass some object that can be reused by all
   * threads in test plan (eg: a cache).
   *
   * @param name  specifies the name of the property, used by test plan to access the associated
   *              value.
   * @param value specifies a value to store associated to the name.
   * @return the engine instance for further configuration or usage.
   * @since 0.37
   */
  public EmbeddedJmeterEngine prop(String name, Object value) {
    props.put(name, value);
    return this;
  }

  /**
   * Allows specifying a properties file path to get properties from.
   * <p>
   * This is handy when different properties are required for different runs (eg: different
   * environments).
   * <p>
   * This is an alternative to {@link #prop(String, Object)} which is handy for setting a small set
   * of properties, or set them programmatically.
   *
   * @param propsFile is the path to the properties file to load.
   * @return the engine instance for further configuration or usage.
   * @since 0.57
   */
  public EmbeddedJmeterEngine propertiesFile(String propsFile) {
    this.propsFile = propsFile;
    return this;
  }

  @Override
  public TestPlanStats run(DslTestPlan testPlan) throws IOException {
    return runInEnv(testPlan, new JmeterEnvironment());
  }

  protected TestPlanStats runInEnv(DslTestPlan testPlan, JmeterEnvironment env) throws IOException {
    Properties jmeterProps = JMeterUtils.getJMeterProperties();
    if (propsFile != null) {
      try (FileInputStream is = new FileInputStream(propsFile)) {
        jmeterProps.load(is);
      }
    }
    jmeterProps.putAll(props);
    BaseTestStopper testStopper = buildTestStopper();
    BuildTreeContext buildContext = new BuildTreeContext();
    buildContext.setTestStopper(testStopper);
    HashTree rootTree = new ListedHashTree();
    HashTree testPlanTree = buildContext.buildTreeFor(testPlan, rootTree);
    env.updateSearchPath(testPlanTree);

    TestPlanStats stats = new TestPlanStats(EmbeddedStatsSummary::new);
    addStatsCollector(testPlanTree, stats);
    testPlanTree.add(new ResultCollector(new Summariser()));

    List<Future<Void>> closedVisualizers = Collections.emptyList();
    TestRunner testRunner = buildTestRunner(testPlanTree, rootTree, testStopper);
    Map<DslVisualizer, Supplier<Component>> visualizers = buildContext.getVisualizers();
    if (!visualizers.isEmpty()) {
      // this is required for proper visualization of labels and messages from resources bundle
      env.initLocale();
      closedVisualizers = showVisualizers(visualizers, testRunner);
    }
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
    String stopMessage = testStopper.getStopMessage();
    if (stopMessage != null) {
      throw new AutoStoppedTestException(stopMessage);
    }
    return stats;
  }

  public static class EmbeddedJMeterEngineStopper extends BaseTestStopper {

    @Override
    protected void stopTestExecution() {
      StandardJMeterEngine.stopEngine();
    }

  }

  protected BaseTestStopper buildTestStopper() {
    return new EmbeddedJMeterEngineStopper();
  }

  protected void addStatsCollector(HashTree testPlanTree, TestPlanStats stats) {
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

  protected TestRunner buildTestRunner(HashTree testPlanTree, HashTree rootTree,
      TestStopper testStopper) {
    engine.configure(rootTree);
    return new TestRunner() {

      @Override
      public void runTest() {
        engine.run();
      }

      @Override
      public void stop() {
        testStopper.stop(null);
      }

    };
  }

  public abstract static class TestRunner {

    private final List<Runnable> endListeners = new ArrayList<>();

    public void addEndListener(Runnable listener) {
      endListeners.add(listener);
    }

    public void run() {
      try {
        runTest();
      } finally {
        endListeners.forEach(Runnable::run);
      }
    }

    protected abstract void runTest();

    public abstract void stop();

  }

  private List<Future<Void>> showVisualizers(Map<DslVisualizer, Supplier<Component>> visualizers,
      TestRunner testRunner) {
    return visualizers.entrySet().stream()
        .map(e -> {
          CompletableFuture<Void> closedVisualizer = new CompletableFuture<>();
          Component guiComponent = e.getValue().get();
          JPanel panel = new JPanel();
          panel.setLayout(new BorderLayout());
          panel.add(buildToolbar(testRunner), BorderLayout.NORTH);
          panel.add(guiComponent, BorderLayout.CENTER);
          e.getKey().showTestElementGui(panel, () -> closedVisualizer.complete(null));
          return closedVisualizer;
        })
        .collect(Collectors.toList());
  }

  private Component buildToolbar(TestRunner testRunner) {
    JToolBar toolbar = new JToolBar();
    toolbar.add(buildStopButton(testRunner));
    return toolbar;
  }

  private JButton buildStopButton(TestRunner testRunner) {
    URL iconResource = JMeterUtils.class.getClassLoader()
        .getResource("org/apache/jmeter/images/toolbar/22x22/road-sign-us-stop.png");
    JButton button = new JButton(new ImageIcon(iconResource));
    button.setToolTipText("stop test plan");
    button.addActionListener(e -> {
      button.setEnabled(false);
      testRunner.stop();
    });
    testRunner.addEndListener(() -> button.setEnabled(false));
    return button;
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
      Thread.currentThread().interrupt();
    }
  }

}
