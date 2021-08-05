package us.abstracta.jmeter.javadsl.core;

import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

/**
 * Provides the basic logic for all {@link DslTestElement}.
 * <p>
 * In particular it currently allows to set the name of the TestElement and abstracts building of
 * the tree only requiring, from subclasses, to implement the logic to build the JMeter TestElement.
 * The test element name is particularly useful for later reporting and statistics collection to
 * differentiate metrics for each test element.
 * <p>
 * Sub classes may overwrite {@link #buildTreeUnder} if they need additional logic (e.g: {@link
 * TestElementContainer}).
 */
public abstract class BaseTestElement implements DslTestElement {

  private final String name;
  private final Class<? extends JMeterGUIComponent> guiClass;

  protected BaseTestElement(String name, Class<? extends JMeterGUIComponent> guiClass) {
    this.name = name;
    this.guiClass = guiClass;
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    TestElement testElement = buildTestElement();
    testElement.setName(name);
    testElement.setProperty(TestElement.GUI_CLASS, guiClass.getName());
    testElement.setProperty(TestElement.TEST_CLASS, testElement.getClass().getName());
    return parent.add(testElement);
  }

  protected abstract TestElement buildTestElement();

}
