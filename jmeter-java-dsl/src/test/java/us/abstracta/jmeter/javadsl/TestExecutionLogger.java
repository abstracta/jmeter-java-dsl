package us.abstracta.jmeter.javadsl;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestExecutionLogger implements TestExecutionListener {

  private static final Logger LOG = LoggerFactory.getLogger(TestExecutionLogger.class);

  @Override
  public void executionStarted(TestIdentifier testIdentifier) {
    LOG.debug("Started test {}", testIdentifier.getDisplayName());
  }

  @Override
  public void executionFinished(TestIdentifier testIdentifier,
      TestExecutionResult testExecutionResult) {
    LOG.debug("Ended test {}: {}", testIdentifier.getDisplayName(), testExecutionResult.getStatus());
  }
}
