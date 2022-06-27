package us.abstracta.jmeter.javadsl.core.timers;

import org.apache.jmeter.gui.JMeterGUIComponent;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;

/**
 * Contains common logic for all timers.
 *
 * @since 0.62
 */
public abstract class BaseTimer extends BaseTestElement implements DslTimer {

  protected BaseTimer(String name, Class<? extends JMeterGUIComponent> guiClass) {
    super(name, guiClass);
  }

}
