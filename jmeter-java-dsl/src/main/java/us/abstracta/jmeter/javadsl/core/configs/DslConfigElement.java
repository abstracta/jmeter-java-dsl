package us.abstracta.jmeter.javadsl.core.configs;

import org.apache.jmeter.gui.JMeterGUIComponent;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;
import us.abstracta.jmeter.javadsl.core.testelements.MultiLevelTestElement;

/**
 * Contains common logic for config elements defined by the DSL.
 *
 * @since 0.37
 */
public abstract class DslConfigElement extends BaseTestElement implements MultiLevelTestElement {

  protected DslConfigElement(String name, Class<? extends JMeterGUIComponent> guiClass) {
    super(name, guiClass);
  }

}
