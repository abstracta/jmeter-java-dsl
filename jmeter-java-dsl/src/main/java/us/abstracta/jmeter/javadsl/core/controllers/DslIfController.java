package us.abstracta.jmeter.javadsl.core.controllers;

import java.util.List;
import org.apache.jmeter.control.IfController;
import org.apache.jmeter.control.gui.IfControllerPanel;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.core.util.DslScriptBuilder;
import us.abstracta.jmeter.javadsl.core.util.PropertyScriptBuilder;
import us.abstracta.jmeter.javadsl.core.util.PropertyScriptBuilder.PropertyScript;

/**
 * Allows to conditionally run part of a test plan according to certain condition.
 *
 * @since 0.27
 */
public class DslIfController extends BaseController {

  private final DslScriptBuilder conditionBuilder;

  public DslIfController(String condition, List<ThreadGroupChild> children) {
    this(new PropertyScriptBuilder(condition), children);
  }

  private DslIfController(DslScriptBuilder conditionBuilder, List<ThreadGroupChild> children) {
    super("If Controller", IfControllerPanel.class, children);
    this.conditionBuilder = conditionBuilder;
  }

  public DslIfController(PropertyScript script, List<ThreadGroupChild> children) {
    this(new PropertyScriptBuilder(script), children);
  }

  @Override
  public TestElement buildTestElement() {
    IfController ret = new IfController();
    ret.setUseExpression(true);
    String condition = conditionBuilder.build();
    ret.setCondition(condition);
    return ret;
  }

}
