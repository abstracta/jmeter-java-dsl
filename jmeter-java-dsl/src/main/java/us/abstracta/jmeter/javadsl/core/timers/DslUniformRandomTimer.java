package us.abstracta.jmeter.javadsl.core.timers;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.RandomTimer;
import org.apache.jmeter.timers.UniformRandomTimer;
import org.apache.jmeter.timers.gui.UniformRandomTimerGui;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.DoubleParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.DurationParam;

/**
 * Allows specifying JMeter Uniform Random Timers which pause the thread with a random time with
 * uniform distribution.
 * <p>
 * The pause calculated by the timer will be applied after samplers pre-processors execution and
 * before actual sampling.
 * <p>
 * Take into consideration that timers applies to all samplers in their scope: if added at test plan
 * level, it will apply to all samplers in test plan; if added at thread group level, it will apply
 * only to samples in such thread group; if added as child of a sampler, it will only apply to that
 * sampler.
 *
 * @since 0.16
 */
public class DslUniformRandomTimer extends BaseTimer {

  protected Duration minimum;
  protected Duration maximum;

  public DslUniformRandomTimer(Duration minimum, Duration maximum) {
    super("Uniform Random Timer", UniformRandomTimerGui.class);
    this.minimum = minimum;
    this.maximum = maximum;

  }

  @Override
  protected TestElement buildTestElement() {
    UniformRandomTimer urt = new UniformRandomTimer();
    urt.setRange(maximum.minus(minimum).toMillis());
    urt.setDelay(String.valueOf(minimum.toMillis()));
    return urt;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<UniformRandomTimer> {

    public CodeBuilder(List<Method> builderMethods) {
      super(UniformRandomTimer.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(UniformRandomTimer testElement,
        MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      MethodParam delay = paramBuilder.durationParamMillis(RandomTimer.DELAY, null);
      MethodParam range = paramBuilder.doubleParam(RandomTimer.RANGE);
      if (!(delay instanceof DurationParam) || !(range instanceof DoubleParam)) {
        throw new UnsupportedOperationException("Using JMeter expressions in timer properties is "
            + "still not supported. Request it in the GitHub repository as an issue and we will "
            + "add support for it.");
      }
      Duration rangeDuration = Duration.ofMillis(Math.round(((DoubleParam) range).getValue()));
      return buildMethodCall(delay,
          new DurationParam(((DurationParam) delay).getValue().plus(rangeDuration)));
    }

  }

}
