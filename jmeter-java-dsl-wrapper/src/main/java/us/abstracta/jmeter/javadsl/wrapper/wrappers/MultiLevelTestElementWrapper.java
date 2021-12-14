package us.abstracta.jmeter.javadsl.wrapper.wrappers;

import static us.abstracta.jmeter.javadsl.wrapper.wrappers.TestElementWrapperHelper.solveGuiClass;
import static us.abstracta.jmeter.javadsl.wrapper.wrappers.TestElementWrapperHelper.solveName;

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

  private final TestElementWrapperHelper<TestElement> helper;

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

}
