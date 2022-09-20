package us.abstracta.jmeter.javadsl.core.controllers;

import java.util.List;
import org.apache.jmeter.control.WhileController;
import org.apache.jmeter.control.gui.WhileControllerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.core.util.DslScriptBuilder;
import us.abstracta.jmeter.javadsl.core.util.PropertyScriptBuilder;
import us.abstracta.jmeter.javadsl.core.util.PropertyScriptBuilder.PropertyScript;

/**
 * Allows running part of a test plan until a condition is met.
 * <p>
 * The condition is evaluated in each iteration before and after all children elements are executed.
 * Keep this in mind in case you use conditions with side effects (like incrementing counters).
 * <p>
 * JMeter automatically creates a variable named {@code __jm__<controllerName>__idx} which contains
 * the index of the iteration starting with zero.
 *
 * @since 0.27
 */
public class DslWhileController extends BaseController<DslWhileController> {

  protected DslScriptBuilder conditionBuilder;

  public DslWhileController(String name, String condition, List<ThreadGroupChild> children) {
    this(name, new PropertyScriptBuilder(condition), children);
  }

  private DslWhileController(String name, DslScriptBuilder conditionBuilder,
      List<ThreadGroupChild> children) {
    super(name != null ? name : "while", WhileControllerGui.class, children);
    this.conditionBuilder = conditionBuilder;
  }

  public DslWhileController(String name, PropertyScript script, List<ThreadGroupChild> children) {
    this(name, new PropertyScriptBuilder(script), children);
  }

  @Override
  protected TestElement buildTestElement() {
    WhileController ret = new WhileController();
    String condition = conditionBuilder.build();
    ret.setCondition(condition);
    return ret;
  }

}
