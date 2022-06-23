package us.abstracta.jmeter.javadsl.wrapper;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;
import us.abstracta.jmeter.javadsl.wrapper.wrappers.DslControllerWrapper;
import us.abstracta.jmeter.javadsl.wrapper.wrappers.DslSamplerWrapper;
import us.abstracta.jmeter.javadsl.wrapper.wrappers.DslThreadGroupWrapper;
import us.abstracta.jmeter.javadsl.wrapper.wrappers.MultiLevelTestElementWrapper;

/**
 * Includes builder methods which allow to easily use custom or still not supported JMeter test
 * elements with the DSL.
 * <p>
 * In general, prefer implementing some custom builder method (and class) for a given test element,
 * and even contribute it to JMeter DSL if the element is public, instead of using this class. This
 * class allows you to quickly use a component, or avoid having to create a class and methods for
 * supporting some private and very specific component.
 * <p>
 * This class provides a builder method for each type of test element to avoid using the element in
 * incorrect test plan locations (e.g.: defining a sampler without a thread group, or nest
 * samplers).
 *
 * @since 0.41
 */
public class WrapperJmeterDsl {

  /**
   * Builds a test element wrapper from a thread group gui component.
   * <p>
   * For {@link TestBean} JMeter elements prefer using {@link #testElement(AbstractThreadGroup)}
   * instead.
   *
   * @param threadGroupGui is a thread group gui component used to create the associated test
   *                       element.
   *                       <p>
   *                       You can provide a gui component with pre initialized properties, or you
   *                       can set properties in returned instance through
   *                       {@link DslThreadGroupWrapper#prop(String, Object)} method.
   *                       <p>
   *                       Consider invoking {@link JMeterGUIComponent#clearGui()} before passing it
   *                       to the method in case the method sets some default values not set by
   *                       {@link JMeterGUIComponent#createTestElement()}.
   * @return the wrapped test element for further configuration and usage.
   * @see DslThreadGroupWrapper
   */
  public static DslThreadGroupWrapper testElement(AbstractThreadGroupGui threadGroupGui) {
    return testElement(null, threadGroupGui);
  }

  /**
   * Same as {@link #testElement(AbstractThreadGroupGui)} but allowing to set a name for easy
   * identification in reports.
   *
   * @see #testElement(AbstractThreadGroupGui)
   */
  public static DslThreadGroupWrapper testElement(String name,
      AbstractThreadGroupGui threadGroupGui) {
    return new DslThreadGroupWrapper(name, null, threadGroupGui);
  }

  /**
   * Builds a test element wrapper from a thread group JMeter test element.
   * <p>
   * This method should be used, instead of {@link #testElement(AbstractThreadGroupGui)}, when
   * wrapping {@link TestBean} JMeter test elements (which always use {@link TestBeanGUI}).
   * <p>
   * This method can also be used with non {@link TestBean} instances, but, if you want to use
   * {@link BaseTestElement#showInGui()} or {@link DslTestPlan#saveAsJmx(String)}, then you need to
   * set {@link TestElement#GUI_CLASS} property to the proper GUI component class path, e.g.:
   * <pre>{@code elem.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName()}</pre>.
   * <b>Prefer using {@link #testElement(AbstractThreadGroupGui)} for non {@link TestBean} JMeter
   * test elements.</b>
   *
   * @param threadGroup is a JMeter thread group test element to be used by the DSL in the test
   *                    plan.
   *                    <p>
   *                    You can provide a pre initialized test element, or you can set properties in
   *                    returned instance through {@link DslThreadGroupWrapper#prop(String, Object)}
   *                    method.
   * @return the wrapped test element for further configuration and usage.
   * @see DslThreadGroupWrapper
   */
  public static DslThreadGroupWrapper testElement(AbstractThreadGroup threadGroup) {
    return testElement(null, threadGroup);
  }

  /**
   * Same as {@link #testElement(AbstractThreadGroup)} but allowing to set a name for easy
   * identification in reports.
   *
   * @see #testElement(AbstractThreadGroup)
   */
  public static DslThreadGroupWrapper testElement(String name, AbstractThreadGroup threadGroup) {
    return new DslThreadGroupWrapper(name, threadGroup, null);
  }

  /**
   * Builds a test element wrapper from a sampler gui component.
   * <p>
   * For {@link TestBean} JMeter elements prefer using {@link #testElement(Sampler)} instead.
   *
   * @param samplerGui is a sampler gui component used to create the associated test element.
   *                   <p>
   *                   You can provide a gui component with pre initialized properties, or you can
   *                   set properties in returned instance through
   *                   {@link DslSamplerWrapper#prop(String, Object)} method.
   *                   <p>
   *                   Consider invoking {@link JMeterGUIComponent#clearGui()} before passing it to
   *                   the method in case the method sets some default values not set by
   *                   {@link JMeterGUIComponent#createTestElement()}.
   * @return the wrapped test element for further configuration and usage.
   * @see DslSamplerWrapper
   */
  public static DslSamplerWrapper testElement(AbstractSamplerGui samplerGui) {
    return testElement(null, samplerGui);
  }

  /**
   * Same as {@link #testElement(AbstractSamplerGui)} but allowing to set a name for easy
   * identification in reports.
   *
   * @see #testElement(AbstractSamplerGui)
   */
  public static DslSamplerWrapper testElement(String name, AbstractSamplerGui samplerGui) {
    return new DslSamplerWrapper(name, null, samplerGui);
  }

  /**
   * Builds a test element wrapper from a sampler JMeter test element.
   * <p>
   * This method should be used, instead of {@link #testElement(AbstractSamplerGui)}, when wrapping
   * {@link TestBean} JMeter test elements (which always use {@link TestBeanGUI}).
   * <p>
   * This method can also be used with non {@link TestBean} instances, but, if you want to use
   * {@link BaseTestElement#showInGui()} or {@link DslTestPlan#saveAsJmx(String)}, then you need to
   * set {@link TestElement#GUI_CLASS} property to the proper GUI component class path e.g.:
   * <pre>{@code elem.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName()}</pre>.
   * <b>Prefer using {@link #testElement(AbstractSamplerGui)} for non {@link TestBean} JMeter
   * test elements.</b>
   *
   * @param sampler is a JMeter sampler test element to be used by the DSL in the test plan.
   *                <p>
   *                You can provide a pre initialized test element, or you can set properties in
   *                returned instance through {@link DslSamplerWrapper#prop(String, Object)}
   *                method.
   * @return the wrapped test element for further configuration and usage.
   * @see DslSamplerWrapper
   */
  public static DslSamplerWrapper testElement(Sampler sampler) {
    return new DslSamplerWrapper(null, sampler, null);
  }

  /**
   * Same as {@link #testElement(Sampler)} but allowing to set a name for easy identification in
   * reports.
   *
   * @see #testElement(Sampler)
   */
  public static DslSamplerWrapper testElement(String name, Sampler sampler) {
    return new DslSamplerWrapper(name, sampler, null);
  }

  /**
   * Builds a test element wrapper from a controller gui component.
   * <p>
   * For {@link TestBean} JMeter elements prefer using {@link #testElement(Controller)} instead.
   *
   * @param controllerGui is a controller gui component used to create the associated test element.
   *                      <p>
   *                      You can provide a gui component with pre initialized properties, or you
   *                      can set properties in returned instance through
   *                      {@link DslControllerWrapper#prop(String, Object)} method.
   *                      <p>
   *                      Consider invoking {@link JMeterGUIComponent#clearGui()} before passing it
   *                      to the method in case the method sets some default values not set by
   *                      {@link JMeterGUIComponent#createTestElement()}.
   * @return the wrapped test element for further configuration and usage.
   * @see DslControllerWrapper
   */
  public static DslControllerWrapper testElement(AbstractControllerGui controllerGui) {
    return testElement(null, controllerGui);
  }

  /**
   * Same as {@link #testElement(AbstractControllerGui)} but allowing to set a name for easy
   * identification in reports.
   *
   * @see #testElement(AbstractControllerGui)
   */
  public static DslControllerWrapper testElement(String name, AbstractControllerGui controllerGui) {
    return new DslControllerWrapper(name, null, controllerGui);
  }

  /**
   * Builds a test element wrapper from a controller JMeter test element.
   * <p>
   * This method should be used, instead of {@link #testElement(AbstractControllerGui)}, when
   * wrapping {@link TestBean} JMeter test elements (which always use {@link TestBeanGUI}).
   * <p>
   * This method can also be used with non {@link TestBean} instances, but, if you want to use
   * {@link BaseTestElement#showInGui()} or {@link DslTestPlan#saveAsJmx(String)}, then you need to
   * set {@link TestElement#GUI_CLASS} property to the proper GUI component class path, e.g.:
   * <pre>{@code elem.setProperty(TestElement.GUI_CLASS, IfControllerPanel.class.getName()}</pre>.
   * <b>Prefer using {@link #testElement(AbstractControllerGui)} for non {@link TestBean} JMeter
   * test elements.</b>
   *
   * @param controller is a JMeter controller test element to be used by the DSL in the test plan.
   *                   <p>
   *                   You can provide a pre initialized test element, or you can set properties in
   *                   returned instance through {@link DslControllerWrapper#prop(String, Object)}
   *                   method.
   * @return the wrapped test element for further configuration and usage.
   * @see DslControllerWrapper
   */
  public static DslControllerWrapper testElement(Controller controller) {
    return testElement(null, controller);
  }

  /**
   * Same as {@link #testElement(Controller)} but allowing to set a name for easy identification in
   * reports.
   *
   * @see #testElement(Controller)
   */
  public static DslControllerWrapper testElement(String name, Controller controller) {
    return new DslControllerWrapper(name, controller, null);
  }

  /**
   * Builds a test element wrapper from a JMeter multi level test element (listener, assertion,
   * pre-/post- processors, timers or config element) gui component.
   * <p>
   * For {@link TestBean} JMeter elements prefer using {@link #testElement(TestElement)} instead.
   *
   * @param componentGui is a multi level test element (listener, assertion, pre-/post- processors,
   *                     timers or config element) gui component used to create the associated test
   *                     element.
   *                     <p>
   *                     You can provide a gui component with pre initialized properties, or you can
   *                     set properties in returned instance through
   *                     {@link MultiLevelTestElementWrapper#prop(String, Object)} method.
   *                     <p>
   *                     Consider invoking {@link JMeterGUIComponent#clearGui()} before passing it
   *                     to the method in case the method sets some default values not set by
   *                     {@link JMeterGUIComponent#createTestElement()}.
   * @return the wrapped test element for further configuration and usage.
   * @see MultiLevelTestElementWrapper
   */
  public static MultiLevelTestElementWrapper testElement(JMeterGUIComponent componentGui) {
    return testElement(null, componentGui);
  }

  /**
   * Same as {@link #testElement(JMeterGUIComponent)} but allowing to set a name for easy
   * identification in reports.
   *
   * @see #testElement(JMeterGUIComponent)
   */
  public static MultiLevelTestElementWrapper testElement(String name,
      JMeterGUIComponent guiComponent) {
    return new MultiLevelTestElementWrapper(name, null, guiComponent);
  }

  /**
   * Builds a test element wrapper from a JMeter multi level test element (listener, assertion,
   * pre-/post- processors, timers or config element).
   * <p>
   * This method should be used, instead of {@link #testElement(JMeterGUIComponent)}, when wrapping
   * {@link TestBean} JMeter test elements (which always use {@link TestBeanGUI}).
   * <p>
   * This method can also be used with non {@link TestBean} instances, but, if you want to use
   * {@link BaseTestElement#showInGui()} or {@link DslTestPlan#saveAsJmx(String)}, then you need to
   * set {@link TestElement#GUI_CLASS} property to the proper GUI component class path, e.g.:
   * <pre>{@code elem.setProperty(TestElement.GUI_CLASS, RegexExtractorGui.class.getName()}</pre>.
   * <b>Prefer using {@link #testElement(JMeterGUIComponent)} for non {@link TestBean} JMeter
   * test elements.</b>
   *
   * @param testElement is a JMeter multi level test element (listener, assertion, pre-/post-
   *                    processors, timers or config element) to be used by the DSL in the test
   *                    plan.
   *                    <p>
   *                    You can provide a pre initialized test element, or you can set properties in
   *                    returned instance through
   *                    {@link MultiLevelTestElementWrapper#prop(String, Object)} method.
   * @return the wrapped test element for further configuration and usage.
   * @see MultiLevelTestElementWrapper
   */
  public static MultiLevelTestElementWrapper testElement(TestElement testElement) {
    return testElement(null, testElement);
  }

  /**
   * Same as {@link #testElement(TestElement)} but allowing to set a name for easy identification in
   * reports.
   *
   * @see #testElement(TestElement)
   */
  public static MultiLevelTestElementWrapper testElement(String name, TestElement testElement) {
    return new MultiLevelTestElementWrapper(name, testElement, null);
  }

}
