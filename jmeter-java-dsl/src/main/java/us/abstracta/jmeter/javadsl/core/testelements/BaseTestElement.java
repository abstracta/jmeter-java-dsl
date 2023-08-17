package us.abstracta.jmeter.javadsl.core.testelements;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.AbstractProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.DslTestElement;
import us.abstracta.jmeter.javadsl.core.engines.JmeterEnvironment;

/**
 * Provides the basic logic for all {@link DslTestElement}.
 * <p>
 * In particular, it currently allows to set the name of the TestElement and abstracts building of
 * the tree only requiring, from subclasses, to implement the logic to build the JMeter TestElement.
 * The test element name is particularly useful for later reporting and statistics collection to
 * differentiate metrics for each test element.
 * <p>
 * Subclasses may overwrite {@link #buildTreeUnder} if they need additional logic (e.g: {@link
 * TestElementContainer}).
 *
 * @since 0.1
 */
public abstract class BaseTestElement implements DslTestElement {

  private static final Logger LOG = LoggerFactory.getLogger(BaseTestElement.class);

  protected String name;
  protected Class<? extends JMeterGUIComponent> guiClass;

  protected BaseTestElement(String name, Class<? extends JMeterGUIComponent> guiClass) {
    this.name = name;
    this.guiClass = guiClass;
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    return parent.add(buildConfiguredTestElement());
  }

  protected TestElement buildConfiguredTestElement() {
    TestElement ret = buildTestElement();
    return configureTestElement(ret, name, guiClass);
  }

  protected static TestElement configureTestElement(TestElement ret, String name,
      Class<? extends JMeterGUIComponent> guiClass) {
    ret.setName(name);
    /*
     guiClass might be null when using wrappers, and we don't want to restrict running test plan
     if none is set.
     */
    if (guiClass != null) {
      ret.setProperty(TestElement.GUI_CLASS, guiClass.getName());
    }
    ret.setProperty(TestElement.TEST_CLASS, ret.getClass().getName());
    if (guiClass == TestBeanGUI.class) {
      loadBeanProperties(ret);
    }
    return ret;
  }

  protected abstract TestElement buildTestElement();

  public static void loadBeanProperties(TestElement bean) {
    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
      for (PropertyDescriptor prop : beanInfo.getPropertyDescriptors()) {
        if (TestBeanHelper.isDescriptorIgnored(prop)) {
          continue;
        }
        try {
          JMeterProperty jprop = AbstractProperty.createProperty(prop.getReadMethod().invoke(bean));
          jprop.setName(prop.getName());
          bean.setProperty(jprop);
        } catch (IllegalAccessException | InvocationTargetException e) {
          LOG.error("Could not set property {} for bean {}", prop.getName(), bean, e);
        }
      }
    } catch (IntrospectionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void showInGui() {
    try {
      // this is required for proper visualization of labels and messages from resources bundle
      new JmeterEnvironment().initLocale();
      CountDownLatch countDownLatch = new CountDownLatch(1);
      showTestElementGui(buildTestElementGui(buildConfiguredTestElement()),
          countDownLatch::countDown);
      countDownLatch.await();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  protected Component buildTestElementGui(TestElement testElement) {
    try {
      Class<? extends JMeterGUIComponent> guiClass = (Class<? extends JMeterGUIComponent>)
          Class.forName(testElement.getPropertyAsString(TestElement.GUI_CLASS));
      JMeterGUIComponent gui =
          guiClass == TestBeanGUI.class ? new TestBeanGUI(testElement.getClass())
              : guiClass.newInstance();
      gui.clearGui();
      gui.configure(testElement);
      gui.modifyTestElement(testElement);
      return (Component) gui;
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public void showTestElementGui(Component guiComponent, Runnable closeListener) {
    showFrameWith(guiComponent, name, 800, 600, closeListener);
  }

  protected void showFrameWith(Component content, String title, int width, int height,
      Runnable closeListener) {
    JFrame frame = new JFrame(title);
    frame.setDefaultCloseOperation(
        closeListener != null ? WindowConstants.DISPOSE_ON_CLOSE : WindowConstants.EXIT_ON_CLOSE);
    if (closeListener != null) {
      frame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
          super.windowClosed(e);
          closeListener.run();
        }
      });
    }
    frame.setLocation(200, 200);
    frame.setSize(width, height);
    frame.add(content);
    frame.setVisible(true);
  }

  protected void showAndWaitFrameWith(Component content, String title, int width, int height) {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    showFrameWith(content, title, width, height, countDownLatch::countDown);
    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  protected static long durationToSeconds(Duration duration) {
    return Math.round(Math.ceil((double) duration.toMillis() / 1000));
  }

}
