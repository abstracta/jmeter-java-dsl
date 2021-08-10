package us.abstracta.jmeter.javadsl.core;

import java.awt.Component;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.core.EmbeddedJmeterEngine.JMeterEnvironment;

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

  protected final String name;
  protected Class<? extends JMeterGUIComponent> guiClass;

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

  @Override
  public void showInGui() {
    try (JMeterEnvironment env = new JMeterEnvironment()) {
      // this is required for proper visualization of labels and messages from resources bundle
      env.initLocale();
      TestElement testElement = buildTestElement();
      testElement.setName(name);
      JMeterGUIComponent gui =
          guiClass == TestBeanGUI.class ? new TestBeanGUI(testElement.getClass())
              : guiClass.newInstance();
      gui.configure(testElement);
      showFrameWith((Component) gui, 800, 600);
    } catch (InstantiationException | IllegalAccessException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void showFrameWith(Component content, int width, int height) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setLocation(200, 200);
    frame.setSize(width, height);
    frame.add(content);
    frame.setVisible(true);
  }

  protected abstract TestElement buildTestElement();

}
