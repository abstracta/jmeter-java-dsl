package us.abstracta.jmeter.javadsl.core.timers;

import org.apache.jmeter.timers.gui.UniformRandomTimerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.UniformRandomTimer;
import us.abstracta.jmeter.javadsl.core.BaseTestElement;
import us.abstracta.jmeter.javadsl.core.MultiLevelTestElement;

/**
 * Allows specifying JMeter Uniform Random Timers which pause the thread with a
 * random time with uniform distribution
 */
public class DslUniformRandomTimer extends BaseTestElement implements MultiLevelTestElement {
  private final long minimumMillis;
  private final long maximumMillis;

  public DslUniformRandomTimer(long minimumMillis, long maximumMillis) {
    super("Uniform Random Timer", UniformRandomTimerGui.class);
    this.minimumMillis = minimumMillis;
    this.maximumMillis = maximumMillis;

  }

  @Override
  protected TestElement buildTestElement() {
    UniformRandomTimer urt = new UniformRandomTimer();
    urt.setRange(maximumMillis - minimumMillis);
    urt.setDelay(String.valueOf(minimumMillis));
    return urt;
  }

}