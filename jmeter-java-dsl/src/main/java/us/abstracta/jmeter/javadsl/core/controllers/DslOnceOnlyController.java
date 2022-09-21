package us.abstracta.jmeter.javadsl.core.controllers;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.control.OnceOnlyController;
import org.apache.jmeter.control.gui.OnceOnlyControllerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Allows running a part of a test plan only once and only on the first iteration of each thread
 * group.
 * <p>
 * Internally this uses JMeter Once Only Controller.
 *
 * @since 0.34
 */
public class DslOnceOnlyController extends BaseController<DslOnceOnlyController> {

  public DslOnceOnlyController(List<BaseThreadGroup.ThreadGroupChild> children) {
    super("Once Only Controller", OnceOnlyControllerGui.class, children);
  }

  @Override
  protected TestElement buildTestElement() {
    return new OnceOnlyController();
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<OnceOnlyController> {

    public CodeBuilder(List<Method> builderMethods) {
      super(OnceOnlyController.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(OnceOnlyController testElement,
        MethodCallContext context) {
      return buildMethodCall(new ChildrenParam<>(ThreadGroupChild[].class));
    }

  }

}
