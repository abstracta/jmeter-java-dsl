package us.abstracta.jmeter.javadsl.core.controllers;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.control.IfController;
import org.apache.jmeter.control.gui.IfControllerPanel;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.core.util.PropertyScriptBuilder;
import us.abstracta.jmeter.javadsl.core.util.PropertyScriptBuilder.PropertyScript;

/**
 * Allows to conditionally run part of a test plan according to certain condition.
 *
 * @since 0.27
 */
public class DslIfController extends BaseController<DslIfController> {

  protected PropertyScriptBuilder<Boolean> conditionBuilder;

  public DslIfController(String condition, List<ThreadGroupChild> children) {
    this(new PropertyScriptBuilder<>(condition), children);
  }

  private DslIfController(PropertyScriptBuilder<Boolean> conditionBuilder,
      List<ThreadGroupChild> children) {
    super("If Controller", IfControllerPanel.class, children);
    this.conditionBuilder = conditionBuilder;
  }

  public DslIfController(PropertyScript<Boolean> script, List<ThreadGroupChild> children) {
    this(new PropertyScriptBuilder<>(script), children);
  }

  public DslIfController(Class<? extends PropertyScript<Boolean>> conditionClass,
      List<ThreadGroupChild> children) {
    this(new PropertyScriptBuilder<>(conditionClass), children);
  }

  @Override
  protected TestElement buildTestElement() {
    IfController ret = new IfController();
    ret.setUseExpression(true);
    String condition = conditionBuilder.build();
    ret.setCondition(condition);
    return ret;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<IfController> {

    public CodeBuilder(List<Method> builderMethods) {
      super(IfController.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(IfController testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement,
          "IfController");
      return buildMethodCall(paramBuilder.stringParam("condition"),
          new ChildrenParam<>(ThreadGroupChild[].class));
    }

  }

}
