package us.abstracta.jmeter.javadsl.core;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Interface to be implemented by classes allowing to run a DslTestPlan in different engines.
 *
 * @since 0.2
 */
public interface DslJmeterEngine {

  /**
   * Runs the given test plan obtaining the execution metrics.
   * <p>
   * This method blocks execution until the test plan execution ends.
   *
   * @param testPlan to run in the JMeter engine.
   * @return the metrics associated to the run.
   * @throws IOException          when there is a problem with an IO operation.
   * @throws InterruptedException when the execution thread is interrupted.
   * @throws TimeoutException     when some configured time out is reached. Each engine might define
   *                              a different set of timeouts.
   */
  TestPlanStats run(DslTestPlan testPlan)
      throws IOException, InterruptedException, TimeoutException;

}
