package us.abstracta.jmeter.javadsl.core.engines;

import org.apache.jmeter.testelement.TestElement;

/**
 * Allows to stop a test plan execution with a given message.
 * <p>
 * This is used to stop test plans when auto stop condition is triggered.
 * <p>
 * Each engine should provide a proper implementation of this interface.
 */
public interface TestStopper extends TestElement {

  void stop(String message);

}
