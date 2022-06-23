package us.abstracta.jmeter.javadsl.wrapper.wrappers;

import static us.abstracta.jmeter.javadsl.wrapper.wrappers.TestElementWrapperHelper.solveGuiClass;
import static us.abstracta.jmeter.javadsl.wrapper.wrappers.TestElementWrapperHelper.solveName;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.controllers.BaseController;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Is a {@link TestElementWrapper} for JMeter controllers.
 *
 * @since 0.41
 */
public class DslControllerWrapper extends BaseController implements
    TestElementWrapper<DslControllerWrapper> {

  private final TestElementWrapperHelper<Controller> helper;

  public DslControllerWrapper(String name, Controller testElement,
      AbstractControllerGui guiComponent) {
    super(solveName(name, testElement, guiComponent), solveGuiClass(testElement, guiComponent),
        Collections.emptyList());
    this.helper = new TestElementWrapperHelper<>(testElement, guiComponent);
  }

  public DslControllerWrapper prop(String name, Object value) {
    helper.prop(name, value);
    return this;
  }

  /**
   * Allows specifying controller children elements (samplers, listeners, post processors, etc.).
   *
   * @param children list of test elements to add as children of the controller.
   * @return the altered controller for further configuration or usage.
   */
  public DslControllerWrapper children(ThreadGroupChild... children) {
    return (DslControllerWrapper) addChildren(children);
  }

  @Override
  protected TestElement buildTestElement() {
    return helper.buildTestElement();
  }

  public static class CodeBuilder extends TestElementWrapperCallBuilder<GenericController> {

    public CodeBuilder(List<Method> builderMethods) {
      super(GenericController.class, AbstractControllerGui.class, builderMethods);
    }

  }

}
