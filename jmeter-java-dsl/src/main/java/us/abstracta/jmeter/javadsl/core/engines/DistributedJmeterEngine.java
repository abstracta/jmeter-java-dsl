package us.abstracta.jmeter.javadsl.core.engines;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.apache.jmeter.engine.DistributedRunner;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.documentation.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

/**
 * Allows running a JMeter test plan distributed across multiple machines.
 * <p>
 * This engine uses JMeter built-in feature to run tests from one node into remote machines.
 *
 * @since 0.29
 */
public class DistributedJmeterEngine extends EmbeddedJmeterEngine {

  private final List<String> hosts;
  private final DistributedRunner distributedRunner;
  private int basePort;
  private boolean stopEngines;
  private JmeterEnvironment jmeterEnv;

  public DistributedJmeterEngine(String... hosts) {
    this.hosts = Arrays.asList(hosts);
    distributedRunner = new DistributedRunner();
    distributedRunner.setStdout(System.out);
    distributedRunner.setStdErr(System.err);
  }

  /**
   * Allows setting the initial port number used to calculate rest of ports to establish RMI
   * connections.
   * <p>
   * This method allows to have a predefined range of ports to be used, and in consequence, properly
   * configure firewall rules.
   *
   * @param basePort the port number to start creating connections from. 1 port is required for each
   *                 connection to a remote port, and ports will be assigned incrementally from the
   *                 given value.
   * @return the DistributedJMeterEngine instance for further configuration or usage.
   */
  public DistributedJmeterEngine localBasePort(int basePort) {
    this.basePort = basePort;
    return this;
  }

  /**
   * Specifies to stop remote engines once a test is run and finished.
   *
   * @return the DistributedJMeterEngine instance for further configuration or usage.
   */
  public DistributedJmeterEngine stopEnginesOnTestEnd() {
    return stopEnginesOnTestEnd(true);
  }

  /**
   * Same as {@link #stopEnginesOnTestEnd()} but allowing to enable or disable it.
   *
   * @param enable specifies to enable or disable the setting. By default, it is set to false.
   * @return the DistributedJMeterEngine instance for further configuration or usage.
   * @see #stopEnginesOnTestEnd()
   * @since 1.0
   */
  public DistributedJmeterEngine stopEnginesOnTestEnd(boolean enable) {
    stopEngines = enable;
    return this;
  }

  @VisibleForTesting
  protected DistributedJmeterEngine localJMeterEnv(JmeterEnvironment env) {
    this.jmeterEnv = env;
    return this;
  }

  @Override
  public TestPlanStats run(DslTestPlan testPlan) throws IOException {
    if (jmeterEnv != null) {
      return runInEnv(testPlan, jmeterEnv);
    } else {
      return runInEnv(testPlan, new JmeterEnvironment());
    }
  }

  protected void addStatsCollector(HashTree testPlanTree, TestPlanStats stats) {
    testPlanTree.add(new StatsCollector(stats));
  }

  private static class StatsCollector implements SampleListener,
      Remoteable {

    private final TestPlanStats stats;

    private StatsCollector(TestPlanStats stats) {
      this.stats = stats;
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
      stats.addSampleResult(e.getResult());
    }

    @Override
    public void sampleStarted(SampleEvent e) {
    }

    @Override
    public void sampleStopped(SampleEvent e) {
    }

  }

  @Override
  protected BaseTestStopper buildTestStopper() {
    return new BaseTestStopper() {

      @Override
      protected void stopTestExecution() {
        distributedRunner.stop();
      }

    };
  }

  @Override
  protected TestRunner buildTestRunner(HashTree testPlanTree,
      HashTree rootTree, TestStopper testStopper) {
    JMeterUtils.setProperty("client.rmi.localport", String.valueOf(basePort));
    EnginesEndListener endListener = new EnginesEndListener(stopEngines);
    testPlanTree.add(endListener);
    distributedRunner.init(hosts, rootTree);
    endListener.setStartedRemoteEngines(new ArrayList<>(distributedRunner.getEngines()));
    return new TestRunner() {

      @Override
      public void runTest() {
        distributedRunner.start();
        try {
          endListener.await();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }

      @Override
      public void stop() {
        testStopper.stop(null);
      }

    };
  }

  private static class EnginesEndListener implements TestStateListener, Remoteable {

    private static final Logger LOG = LoggerFactory.getLogger(EnginesEndListener.class);
    private final boolean stopEngines;
    private CountDownLatch runningEngines;
    private List<JMeterEngine> remoteEngines;

    private EnginesEndListener(boolean stopEngines) {
      this.stopEngines = stopEngines;
    }

    public void setStartedRemoteEngines(List<JMeterEngine> engines) {
      remoteEngines = engines;
      runningEngines = new CountDownLatch(engines.size());
    }

    @Override
    // This is called by a daemon RMI thread from the remote host
    public void testEnded(String host) {
      final long now = System.currentTimeMillis();
      LOG.info("Finished remote host: {} ({})", host, now);
      runningEngines.countDown();
      if (stopEngines && runningEngines.getCount() <= 0) {
        for (JMeterEngine engine : remoteEngines) {
          engine.exit();
        }
      }
    }

    @Override
    public void testEnded() {
    }

    @Override
    public void testStarted(String host) {
      LOG.info("Started remote host:  {} ({})", host, System.currentTimeMillis());
    }

    @Override
    public void testStarted() {
    }

    public void await() throws InterruptedException {
      runningEngines.await();
    }

  }

}
