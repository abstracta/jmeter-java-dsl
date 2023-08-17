package us.abstracta.jmeter.javadsl.core.engines;

import java.util.concurrent.atomic.AtomicReference;
import org.apache.jmeter.testelement.AbstractTestElement;

/**
 * Provides common logic for {@link TestStopper} implementations.
 */
public abstract class BaseTestStopper extends AbstractTestElement implements TestStopper {

  protected final AtomicReference<String> stopMessage = new AtomicReference<>();

  @Override
  public void stop(String message) {
    if (stopMessage.compareAndSet(null, message)) {
      stopTestExecution();
    }
  }

  protected abstract void stopTestExecution();

  public String getStopMessage() {
    return stopMessage.get();
  }

}
