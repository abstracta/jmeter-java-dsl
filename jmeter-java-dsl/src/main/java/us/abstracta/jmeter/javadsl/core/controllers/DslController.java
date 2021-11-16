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
public abstract class DslController extends TestElementContainer<ThreadGroupChild> implements
    ThreadGroupChild {

  protected DslController(String name, Class<? extends JMeterGUIComponent> guiClass,
      List<ThreadGroupChild> children) {
    super(name, guiClass, children);
  }

}
