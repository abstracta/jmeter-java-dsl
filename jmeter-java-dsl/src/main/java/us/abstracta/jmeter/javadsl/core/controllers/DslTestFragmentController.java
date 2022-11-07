package us.abstracta.jmeter.javadsl.core.controllers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jmeter.control.TestFragmentController;
import org.apache.jmeter.control.gui.TestFragmentControllerGui;
import org.apache.jmeter.testelement.TestElement;
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
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(context.getTestElement());
      return new FragmentMethodCall(solveMethodName(context),
          buildMethodCall(paramBuilder.nameParam(DEFAULT_NAME),
              new ChildrenParam<>(ThreadGroupChild[].class)));
    }

    private static String solveMethodName(MethodCallContext context) {
      // removing any character that may not be allowed in method name
      String ret = context.getTestElement().getName().replaceAll("\\W", "");
      // avoid method names starting with digits which are not supported by java
      ret = (Character.isDigit(ret.charAt(0)) ? "fragment" : "") + ret;
      // lower first char to follow java method naming convention
      ret = Character.toLowerCase(ret.charAt(0)) + ret.substring(1);
      Set<String> definedMethods = getDefinedMethods(context.getRoot());
      if (definedMethods.contains(ret)) {
        int index = 1;
        do {
          index++;
        } while (definedMethods.contains(ret + index));
        ret = ret + index;
      }
      definedMethods.add(ret);
      return ret;
    }

    private static Set<String> getDefinedMethods(MethodCallContext context) {
      Object entryKey = DslTestFragmentController.class;
      Set<String> definedMethods = (Set<String>) context.getEntry(entryKey);
      if (definedMethods == null) {
        definedMethods = new HashSet<>();
        context.setEntry(entryKey, definedMethods);
      }
      return definedMethods;
    }

    private static class FragmentMethodCall extends MethodCall {

      private final MethodCall delegate;

      protected FragmentMethodCall(String methodName, MethodCall delegate) {
        super(methodName, DslTestFragmentController.class);
        this.delegate = delegate;
      }

      @Override
      public MethodCall child(MethodCall child) {
        return delegate.child(child);
      }

      @Override
      public Map<String, MethodCall> getMethodDefinitions() {
        return Collections.singletonMap(methodName, delegate);
      }

      @Override
      public String buildCode(String indent) {
        return methodName + "()";
      }

    }

  }

}
