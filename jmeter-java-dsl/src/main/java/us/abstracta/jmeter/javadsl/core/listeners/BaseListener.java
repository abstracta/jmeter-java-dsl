package us.abstracta.jmeter.javadsl.core.listeners;

import org.apache.jmeter.gui.JMeterGUIComponent;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;

public abstract class BaseListener extends BaseTestElement implements DslListener {

  protected BaseListener(String name, Class<? extends JMeterGUIComponent> guiClass) {
    super(name, guiClass);
  }

}
