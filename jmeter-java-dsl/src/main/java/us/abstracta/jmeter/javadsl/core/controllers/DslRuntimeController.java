package us.abstracta.jmeter.javadsl.core.controllers;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.control.RunTime;
import org.apache.jmeter.control.gui.RunTimeGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Allows running a part of a test plan repeatedly for a specified number of seconds
 * for each iteration of each thread in a thread group.
 * <p>
 * Internally this uses JMeter Runtime Controller (Runtime class).
 *
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
    protected MethodCall buildMethodCall(RunTime testElement,
        MethodCallContext context) {
      return buildMethodCall(new ChildrenParam<>(ThreadGroupChild[].class));
    }
  }
}
