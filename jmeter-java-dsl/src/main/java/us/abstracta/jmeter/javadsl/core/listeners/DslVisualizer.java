package us.abstracta.jmeter.javadsl.core.listeners;

import java.awt.GraphicsEnvironment;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;

/**
 * Provides general logic for listeners which show some live information in Swing window.
 *
 * @since 0.23
 */
public abstract class DslVisualizer extends BaseListener {

  private static final Logger LOG = LoggerFactory.getLogger(DslVisualizer.class);

  public DslVisualizer(String name, Class<? extends JMeterGUIComponent> guiClass) {
    super(name, guiClass);
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    if (GraphicsEnvironment.isHeadless()) {
      logNonGuiExecutionWarning();
      return parent;
    }
    TestElement testElement = buildConfiguredTestElement();
    HashTree ret = parent.add(testElement);
    context.addVisualizer(this, () -> buildTestElementGui(testElement));
    return ret;
  }

  protected void logNonGuiExecutionWarning() {
    LOG.warn("The test plan contains a {} which is of no use in non GUI executions (like this one)."
        + " Ignoring it for this execution. Remember removing them once your test plan is ready "
        + "for load testing execution.", getClass().getSimpleName());
  }

}
