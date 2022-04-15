package us.abstracta.jmeter.javadsl.core.controllers;

import java.util.List;
import org.apache.jmeter.control.ForeachController;
import org.apache.jmeter.control.gui.ForeachControlPanel;
import org.apache.jmeter.testelement.TestElement;
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
public class DslForEachController extends BaseController {

  private final String varsPrefix;
  private final String iterationVarName;

  public DslForEachController(String name, String varsPrefix, String iterationVarName,
      List<ThreadGroupChild> children) {
    super(name != null ? name : "foreach", ForeachControlPanel.class, children);
    this.varsPrefix = varsPrefix;
    this.iterationVarName = iterationVarName;
  }

  @Override
  public TestElement buildTestElement() {
    ForeachController ret = new ForeachController();
    ret.setInputVal(varsPrefix);
    ret.setReturnVal(iterationVarName);
    ret.setUseSeparator(true);
    return ret;
  }

}
