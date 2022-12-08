package us.abstracta.jmeter.javadsl.core.controllers;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.control.RunTime;
import org.apache.jmeter.control.gui.RunTimeGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Is a controller that stops executing child elements when a period of time expires.
 * <p>
 * Internally this uses JMeter Runtime Controller.
 *
 * @since 1.1
 */
public class DslRuntimeController extends BaseController<DslRuntimeController> {

  protected String seconds;

  public DslRuntimeController(String seconds,
      List<BaseThreadGroup.ThreadGroupChild> children) {
    super("Runtime Controller", RunTimeGui.class, children);
    this.seconds = seconds;
  }

  @Override
  protected TestElement buildTestElement() {
    RunTime ret = new RunTime();
    ret.setRuntime(seconds);
    return ret;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<RunTime> {

    public CodeBuilder(List<Method> builderMethods) {
      super(RunTime.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(RunTime testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement, "RunTime");
      return buildMethodCall(paramBuilder.durationParam("seconds"),
          new ChildrenParam<>(ThreadGroupChild[].class));
    }

  }

}
