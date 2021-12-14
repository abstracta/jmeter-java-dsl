package us.abstracta.jmeter.javadsl.wrapper.wrappers;

import java.util.HashMap;
import java.util.Map;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.AbstractProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;

/**
 * Includes common logic to be used by test elements wrappers.
 * <p>
 * This logic can't be inherited by instances of {@link TestElementWrapper} since they need to
 * inherit from proper class in each case (BaseDslThreadGroup, DslSampler, etc) to properly
 * accommodate in the test plan and to inherit associated logic (eg: show in gui, children elements
 * handling, etc).
 *
 * @param <T> is the type of the JMeter test element to be wrapped.
 * @since 0.41
 */
public class TestElementWrapperHelper<T extends TestElement> {

  private final T testElement;
  private final JMeterGUIComponent guiComponent;
  private final Map<String, Object> props = new HashMap<>();

  public TestElementWrapperHelper(T testElement,
      JMeterGUIComponent guiComponent) {
    this.testElement = testElement;
    this.guiComponent = guiComponent;
  }

  /**
   * Is used by wrappers constructors to solve the final name to be used.
   *
   * @param name         is a potential name to use. If null then, test element's and gui
   *                     component's name are used.
   * @param testElement  specifies a test element from where the name could be extracted when name
   *                     is null. If null then guiComponent name is used.
   * @param guiComponent specifies a gui component from where name can be extracted when other
   *                     options are null.
   * @return the name to be used by the wrapper.
   */
  public static String solveName(String name, TestElement testElement,
      JMeterGUIComponent guiComponent) {
    if (name != null) {
      return name;
    } else if (testElement != null) {
      return testElement.getName();
    } else {
      return guiComponent.getName();
    }
  }

  /**
   * Is used by wrappers constructors to solve the final gui class to be used.
   *
   * @param testElement  specifies a test element, if is a TestBean class then we know TestBeanGUI
   *                     should be used. Otherwise, check gui component or return null.
   * @param guiComponent specifies a gui component to be used when class can't be extracted from
   *                     test element (when is null).
   * @return the gui class to be associated to the wrapper.
   */
  public static Class<? extends JMeterGUIComponent> solveGuiClass(TestElement testElement,
      JMeterGUIComponent guiComponent) {
    if (testElement instanceof TestBean) {
      return TestBeanGUI.class;
    } else if (guiComponent != null) {
      return guiComponent.getClass();
    } else {
      return null;
    }
  }

  /**
   * Is used by {@link TestElementWrapper#prop(String, Object)} to add properties to a wrapper.
   *
   * @param name  specifies the name of the property.
   * @param value specifies the value associated to the property.
   */
  public void prop(String name, Object value) {
    props.put(name, value);
  }

  /**
   * Is used by {@link TestElementWrapper#buildTreeUnder(HashTree, BuildTreeContext)} to build the
   * JMeter test plan tree for the wrapped element.
   *
   * @return the test element with all properties set.
   */
  public T buildTestElement() {
    T ret = testElement != null ? testElement : (T) guiComponent.createTestElement();
    props.forEach((k, v) -> {
      JMeterProperty prop = AbstractProperty.createProperty(v);
      prop.setName(k);
      ret.setProperty(prop);
    });
    return ret;
  }

}
