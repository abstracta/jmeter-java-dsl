package us.abstracta.jmeter.javadsl.core.configs;

import org.apache.jmeter.gui.JMeterGUIComponent;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;

/**
 * Contains common logic for config elements defined by the DSL.
 *
 * @since 0.37
 */
public abstract class BaseConfigElement extends BaseTestElement implements DslConfig {

  protected BaseConfigElement(String name, Class<? extends JMeterGUIComponent> guiClass) {
    super(name, guiClass);
  }

}
