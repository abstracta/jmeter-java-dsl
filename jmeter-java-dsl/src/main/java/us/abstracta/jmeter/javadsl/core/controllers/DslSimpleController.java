package us.abstracta.jmeter.javadsl.core.controllers;

import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

import java.lang.reflect.Method;
import java.util.List;

/**
 * The Simple Logic Controller lets you organize your Samplers and other Logic Controllers. 
 * This controller provides no additional functionality
 * <p>
 * Used primarily to organize other controllers and samples
 *
 * @since 1.21
 */
public class DslSimpleController extends BaseController<DslSimpleController> {

  public DslSimpleController(String name, List<ThreadGroupChild> children) {
    super(name, LogicControllerGui.class, children);
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
      return buildMethodCall(new StringParam(testElement.getName()), new ChildrenParam<>(ThreadGroupChild[].class));
    }

  }

}
