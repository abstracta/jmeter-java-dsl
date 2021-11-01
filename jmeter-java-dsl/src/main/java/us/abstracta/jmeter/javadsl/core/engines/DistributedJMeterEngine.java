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
 *
 * This engine uses JMeter built-in feature to run tests from one node into remote machines.
 *
 * @since 0.29
 */
public class DistributedJMeterEngine extends EmbeddedJmeterEngine {

  private final List<String> hosts;
  private int basePort;
  private boolean stopEngines;
  private JMeterEnvironment jmeterEnv;

  public DistributedJMeterEngine(String... hosts) {
    this.hosts = Arrays.asList(hosts);
  }

  /**
   * Allows setting the initial port number used to calculate rest of ports to establish RMI
   * connections.
   *
   * This method allows to have a predefined range of ports to be used, and in consequence, properly
   * configure firewall rules.
   *
   * @param basePort the port number to start creating connections from. 1 port is required for each
   * connection to a remote port, and ports will be assigned incrementally from the given value.
   * @return the DistributedJMeterEngine instance for further configuration or usage.
   */
  public DistributedJMeterEngine localBasePort(int basePort) {
    this.basePort = basePort;
    return this;
  }

  /**
   * Specifies to stop remote engines once a test is run and finished.
   *
   * @return the DistributedJMeterEngine instance for further configuration or usage.
   */
  public DistributedJMeterEngine stopEnginesOnTestEnd() {
    stopEngines = true;
    return this;
  }

  @VisibleForTesting
  protected DistributedJMeterEngine localJMeterEnv(JMeterEnvironment env) {
    this.jmeterEnv = env;
    return this;
  }

  @Override
  public TestPlanStats run(DslTestPlan testPlan) throws IOException {
    if (jmeterEnv != null) {
      return runInEnv(testPlan, jmeterEnv);
    } else {
      try (JMeterEnvironment env = new JMeterEnvironment()) {
        return runInEnv(testPlan, env);
      }
    }
  }

  protected void addStatsCollector(HashTree testPlanTree, AggregatingTestPlanStats stats) {
    testPlanTree.add(new StatsCollector(stats));
  }

  private static class StatsCollector implements SampleListener, Remoteable {

    private final AggregatingTestPlanStats stats;

    private StatsCollector(AggregatingTestPlanStats stats) {
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
  protected Runnable buildTestRunner(HashTree testPlanTree,
      HashTree rootTree) {
    JMeterUtils.setProperty("client.rmi.localport", String.valueOf(basePort));
    EnginesEndListener endListener = new EnginesEndListener(stopEngines);
    testPlanTree.add(endListener);
    DistributedRunner distributedRunner = new DistributedRunner();
    distributedRunner.setStdout(System.out);
    distributedRunner.setStdErr(System.err);
    distributedRunner.init(hosts, rootTree);
    endListener.setStartedRemoteEngines(new ArrayList<>(distributedRunner.getEngines()));
    return () -> {
      distributedRunner.start();
      try {
        endListener.await();
      } catch (InterruptedException e) {
        Thread.interrupted();
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
