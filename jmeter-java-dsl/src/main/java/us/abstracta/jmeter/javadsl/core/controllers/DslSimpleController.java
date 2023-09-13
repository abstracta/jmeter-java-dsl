package us.abstracta.jmeter.javadsl.core.controllers;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Builds a Simple Controller that allows defining new JMeter scope for other elements to apply.
 * <p>
 * This is handy for example to apply timers, configs, listeners, assertions, pre- and
 * post-processors to only some samplers in the test plan.
 * <p>
 * It has a similar functionality as the transaction controller, but it doesn't add any additional
 * sample results (statistics) to the test plan.
 *
 * @since 1.21
 */
public class DslSimpleController extends BaseController<DslSimpleController> {

  private static final String DEFAULT_NAME = "Simple Controller";

  public DslSimpleController(String name, List<ThreadGroupChild> children) {
    super(name == null ? DEFAULT_NAME : name, LogicControllerGui.class, children);
  }

  @Override
  protected TestElement buildTestElement() {
    return new GenericController();
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<GenericController> {

    public CodeBuilder(List<Method> builderMethods) {
      super(GenericController.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(GenericController testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      return buildMethodCall(paramBuilder.nameParam(DEFAULT_NAME),
          new ChildrenParam<>(ThreadGroupChild[].class));
    }

  }

}
