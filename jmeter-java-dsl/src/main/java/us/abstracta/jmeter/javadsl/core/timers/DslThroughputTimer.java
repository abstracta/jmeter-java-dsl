package us.abstracta.jmeter.javadsl.core.timers;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.ConstantThroughputTimer;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;

/**
 * Allows using JMeter Constant Throughput Timers which pauses samplers under it's control to
 * achieve a target number of samples per minute.
 * <p>
 * The number of threads must be sufficient to enable the target samples per minute to be
 * achieved based on the sampler response times
 * <p>
 *  There are two different ways of pacing the requests: - delay each thread according to when it
 *  last ran - delay each thread according to when any thread last ran.
 *  The positioning of the timer also determines it's scope i.e. at test plan level to control the
 *  pacing of multiple samples across multiple threads or under a Thread Group to just influence
 *  samplers in that THread Group or as a child of a sampler to only control that sampler (other
 *  samplers in the same  Thread group maybe influenced but are not directly being paused.
 *
 * @since 1.4
 */
public class DslThroughputTimer extends BaseTimer {

  protected  Double throughput;
  protected String calcMode;

  private enum CalcModes {
    ThisThreadOnly,
    AllActiveThreads,
    AllActiveThreadsInCurrentThreadGroup,
    AllActiveThreads_Shared,
    AllActiveThreadsInCurrentThreadGroup_Shared
  }

  public DslThroughputTimer(Double samplesPerMinute) {
    super("Constant Throughput Timer", TestBeanGUI.class);
    this.throughput = samplesPerMinute;
    this.calcMode = String.valueOf(CalcModes.AllActiveThreadsInCurrentThreadGroup_Shared);

  }

  @Override
    protected TestElement buildTestElement() {
    ConstantThroughputTimer ret = new ConstantThroughputTimer();

    ret.setCalcMode(CalcModes.valueOf(calcMode).ordinal());

    ret.setThroughput(throughput);

    return ret;
  }

  public DslThroughputTimer modeThisThreadOnly() {

    this.calcMode = String.valueOf(CalcModes.ThisThreadOnly);

    return this;

  }

  public DslThroughputTimer modeAllActiveThreadsShared() {

    this.calcMode = String.valueOf(CalcModes.AllActiveThreads_Shared);

    return this;

  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<ConstantThroughputTimer> {

    public CodeBuilder(List<Method> builderMethods) {
      super(ConstantThroughputTimer.class, builderMethods);
    }

    @Override
        protected MethodCall buildMethodCall(ConstantThroughputTimer testElement,
        MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement,
                    "ConstantThroughputTimer");

      return buildMethodCall(paramBuilder.stringParam("CALC_MODE"),
              paramBuilder.stringParam("THROUGHPUT"));
    }

  }

}
