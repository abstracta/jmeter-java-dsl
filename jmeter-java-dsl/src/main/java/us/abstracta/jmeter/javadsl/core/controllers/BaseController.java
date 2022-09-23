package us.abstracta.jmeter.javadsl.core.controllers;

import java.util.List;
import org.apache.jmeter.gui.JMeterGUIComponent;
import us.abstracta.jmeter.javadsl.core.testelements.TestElementContainer;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Contains common logic for logic controllers defined by the DSL.
 *
 * @since 0.25
 */
public abstract class BaseController<T extends BaseController<T>> extends
    TestElementContainer<T, ThreadGroupChild> implements DslController {

  protected BaseController(String name, Class<? extends JMeterGUIComponent> guiClass,
      List<ThreadGroupChild> children) {
    super(name, guiClass, children);
  }

  /**
   * Allows specifying children elements that are affected by this controller.
   * <p>
   * This method is helpful to keep general controller settings at the beginning and specify
   * children at last.
   *
   * @param children set of elements to be included in the controller. This list is appended to any
   *                 children defined in controller builder method.
   * @return a new controller instance for further configuration or usage.
   * @since 1.0
   */
  @Override
  public T children(ThreadGroupChild... children) {
    return super.children(children);
  }

}
