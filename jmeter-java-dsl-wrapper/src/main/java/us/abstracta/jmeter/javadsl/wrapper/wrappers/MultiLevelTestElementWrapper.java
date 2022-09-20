package us.abstracta.jmeter.javadsl.wrapper.wrappers;

import static us.abstracta.jmeter.javadsl.wrapper.wrappers.TestElementWrapperHelper.solveGuiClass;
import static us.abstracta.jmeter.javadsl.wrapper.wrappers.TestElementWrapperHelper.solveName;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;
import us.abstracta.jmeter.javadsl.core.testelements.MultiLevelTestElement;

/**
 * Is a {@link TestElementWrapper} for JMeter assertions, pre-/post- processors, timers, listeners
 * and config elements.
 *
 * @since 0.41
 */
public class MultiLevelTestElementWrapper extends BaseTestElement implements MultiLevelTestElement,
    TestElementWrapper<MultiLevelTestElement> {

  protected final TestElementWrapperHelper<TestElement> helper;

  public MultiLevelTestElementWrapper(String name, TestElement testElement,
      JMeterGUIComponent guiComponent) {
    super(solveName(name, testElement, guiComponent), solveGuiClass(testElement, guiComponent));
    this.helper = new TestElementWrapperHelper<>(testElement, guiComponent);
  }

  @Override
  public MultiLevelTestElementWrapper prop(String name, Object value) {
    helper.prop(name, value);
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    return helper.buildTestElement();
  }

  public static class CodeBuilder extends TestElementWrapperCallBuilder<TestElement> {

    public CodeBuilder(List<Method> builderMethods) {
      super(TestElement.class, JMeterGUIComponent.class, builderMethods);
    }

    @Override
    public int order() {
      return 10;
    }

  }

}
