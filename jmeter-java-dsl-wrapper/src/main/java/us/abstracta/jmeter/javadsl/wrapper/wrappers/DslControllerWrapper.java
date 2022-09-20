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

/**
 * Is a {@link TestElementWrapper} for JMeter controllers.
 *
 * @since 0.41
 */
public class DslControllerWrapper extends BaseController<DslControllerWrapper> implements
    TestElementWrapper<DslControllerWrapper> {

  protected final TestElementWrapperHelper<Controller> helper;

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
