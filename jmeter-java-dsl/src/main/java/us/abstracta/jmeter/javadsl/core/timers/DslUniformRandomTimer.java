package us.abstracta.jmeter.javadsl.core.timers;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.UniformRandomTimer;
import org.apache.jmeter.timers.gui.UniformRandomTimerGui;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;

/**
 * Allows specifying JMeter Uniform Random Timers which pause the thread with a random time with
 * uniform distribution.
 *
 * The pause calculated by the timer will be applied after samplers pre-processors execution and
 * before actual sampling.
 *
 * Take into consideration that timers applies to all samplers in their scope: if added at test plan
 * level, it will apply to all samplers in test plan; if added at thread group level, it will apply
 * only to samples in such thread group; if added as child of a sampler, it will only apply to that
 * sampler.
 *
 * @since 0.16
 */
public class DslUniformRandomTimer extends BaseTestElement implements DslTimer {

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
