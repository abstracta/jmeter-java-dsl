package us.abstracta.jmeter.javadsl.core.controllers;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.control.ForeachController;
import org.apache.jmeter.control.gui.ForeachControlPanel;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Iterates over variables with a given prefix and runs part of a test plan for each of the
 * variables.
 * <p>
 * This is usually used in combination with extractors that return more than one variable (like
 * regex extractor with -1 index), to iterate over generated variables.
 * <p>
 * JMeter automatically creates a variable named {@code __jm__<controllerName>__idx} which contains
 * the index of the iteration starting with zero.
 *
 * @since 0.44
 */
public class DslForEachController extends BaseController<DslForEachController> {

  protected String varsPrefix;
  protected String iterationVarName;

  public DslForEachController(String name, String varsPrefix, String iterationVarName,
      List<ThreadGroupChild> children) {
    super(name, ForeachControlPanel.class, children);
    this.varsPrefix = varsPrefix;
    this.iterationVarName = iterationVarName;
  }

  @Override
  protected TestElement buildTestElement() {
    ForeachController ret = new ForeachController();
    ret.setInputVal(varsPrefix);
    ret.setReturnVal(iterationVarName);
    ret.setUseSeparator(true);
    return ret;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<ForeachController> {

    public CodeBuilder(List<Method> builderMethods) {
      super(ForeachController.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(ForeachController testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement,
          "ForeachController");
      MethodParam name = paramBuilder.nameParam(null);
      MethodParam varsPrefix = paramBuilder.stringParam("inputVal");
      MethodParam iterationVarName = paramBuilder.stringParam("returnVal");
      MethodParam children = new ChildrenParam<>(ThreadGroupChild[].class);
      return name.getExpression().equals(iterationVarName.getExpression())
          ? buildMethodCall(varsPrefix, iterationVarName, children)
          : buildMethodCall(name, varsPrefix, iterationVarName, children);
    }

  }

}
