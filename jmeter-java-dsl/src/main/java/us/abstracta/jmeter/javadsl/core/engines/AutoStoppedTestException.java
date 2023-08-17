package us.abstracta.jmeter.javadsl.core.engines;

/**
 * Thrown when a test plan execution has been stopped due to an autoStop condition.
 */
public class AutoStoppedTestException extends IllegalStateException {

  public AutoStoppedTestException(String message) {
    super(message);
  }

}
