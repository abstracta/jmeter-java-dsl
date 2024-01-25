package us.abstracta.jmeter.javadsl.core.samplers;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.sampler.TestAction;
import org.apache.jmeter.sampler.gui.TestActionGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.IntParam;

/**
 * Uses JMeter Flow Control Action to allow taking different actions (stop, pause, interrupt).
 *
 * @since 1.25
 */
public class DslFlowControlAction extends BaseSampler<DslFlowControlAction> {

  private final String duration;

  protected DslFlowControlAction(String duration) {
    super("Flow Control Action", TestActionGui.class);
    this.duration = duration;
  }

  public static DslFlowControlAction pauseThread(String duration) {
    return new DslFlowControlAction(duration);
  }

  @Override
  protected TestElement buildTestElement() {
    TestAction ret = new TestAction();
    ret.setAction(TestAction.PAUSE);
    ret.setTarget(TestAction.THREAD);
    ret.setDuration(duration);
    return ret;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<TestAction> {

    public CodeBuilder(List<Method> builderMethods) {
      super(TestAction.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(TestAction testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement,
          "ActionProcessor");
      MethodParam action = paramBuilder.intParam("action");
      MethodParam duration = paramBuilder.durationParamMillis("duration", null);
      if (((IntParam) action).getValue() != TestAction.PAUSE) {
        throw new UnsupportedOperationException(
            "Only pause action is currently supported for conversion. "
                + "If you need support for converting other types of actions, please create an "
                + "issue in JMeter DSL GitHub repository.");
      }
      return buildMethodCall(duration);
    }
  }

}
