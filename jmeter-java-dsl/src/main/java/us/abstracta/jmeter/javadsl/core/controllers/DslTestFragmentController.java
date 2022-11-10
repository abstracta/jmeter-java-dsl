package us.abstracta.jmeter.javadsl.core.controllers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.apache.jmeter.control.TestFragmentController;
import org.apache.jmeter.control.gui.TestFragmentControllerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.FragmentMethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

public class DslTestFragmentController extends BaseController<DslTestFragmentController> implements
    TestPlanChild {

  private static final String DEFAULT_NAME = "Test Fragment";

  protected DslTestFragmentController(String name, List<ThreadGroupChild> children) {
    super(name == null ? DEFAULT_NAME : name, TestFragmentControllerGui.class, children);
  }

  /*
   Used to provide a holder just for conversion and be able to easily inject a list of children
   elements in a test plan
   */
  public static DslTestFragmentController fragment(ThreadGroupChild... children) {
    return fragment(null, children);
  }

  public static DslTestFragmentController fragment(String name, ThreadGroupChild... children) {
    return new DslTestFragmentController(name, Arrays.asList(children));
  }

  @Override
  protected TestElement buildTestElement() {
    throw new UnsupportedOperationException();
  }

  public static class CodeBuilder extends MethodCallBuilder {

    public CodeBuilder(List<Method> builderMethods) {
      super(builderMethods);
    }

    @Override
    public boolean matches(MethodCallContext context) {
      return context.getTestElement() instanceof TestFragmentController;
    }

    @Override
    protected MethodCall buildMethodCall(MethodCallContext context) {
      TestElement element = context.getTestElement();
      MethodCall methodDefinitionBody = buildMethodCall(
          new TestElementParamBuilder(element).nameParam(DEFAULT_NAME),
          new ChildrenParam<>(ThreadGroupChild[].class));
      return new FragmentMethodCall(context.solveMethodName(element), methodDefinitionBody);
    }

  }

}
