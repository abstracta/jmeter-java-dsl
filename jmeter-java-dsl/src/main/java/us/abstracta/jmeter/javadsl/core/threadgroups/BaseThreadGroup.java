package us.abstracta.jmeter.javadsl.core.threadgroups;

import java.util.List;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.AbstractThreadGroup;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam.EnumPropertyValue;
import us.abstracta.jmeter.javadsl.core.DslTestElement;
import us.abstracta.jmeter.javadsl.core.testelements.TestElementContainer;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Contains common logic for all Thread Groups.
 *
 * @param <T> is the type of the thread group. Used for proper contract definition of fluent builder
 *            methods.
 * @since 0.33
 */
public abstract class BaseThreadGroup<T extends BaseThreadGroup<T>> extends
    TestElementContainer<T, ThreadGroupChild> implements DslThreadGroup {

  protected SampleErrorAction sampleErrorAction = SampleErrorAction.CONTINUE;

  protected BaseThreadGroup(String name, Class<? extends JMeterGUIComponent> guiClass,
      List<ThreadGroupChild> children) {
    super(name, guiClass, children);
  }

  /**
   * Specifies what action to be taken when a sample error is detected.
   *
   * @param sampleErrorAction specifies the action to be taken on sample error. By default, thread
   *                          groups just ignores the error and continue with following sample in
   *                          children elements.
   * @return the thread group for further configuration or usage.
   * @see SampleErrorAction
   */
  public T sampleErrorAction(SampleErrorAction sampleErrorAction) {
    this.sampleErrorAction = sampleErrorAction;
    return (T) this;
  }

  /**
   * Allows specifying thread group children elements (samplers, listeners, post processors, etc.).
   *
   * @param children list of test elements to add as children of the thread group.
   * @return the thread group for further configuration or usage.
   */
  public T children(ThreadGroupChild... children) {
    return super.children(children);
  }

  @Override
  protected TestElement buildTestElement() {
    TestElement ret = buildThreadGroup();
    ret.setProperty(
        new StringProperty(AbstractThreadGroup.ON_SAMPLE_ERROR, sampleErrorAction.propertyValue));
    return ret;
  }

  protected abstract AbstractThreadGroup buildThreadGroup();

  /**
   * Test elements that can be added as direct children of a thread group in jmeter should implement
   * this interface.
   */
  public interface ThreadGroupChild extends DslTestElement {

  }

  /**
   * Specifies an action to be taken by thread group when a sample error is detected.
   */
  public enum SampleErrorAction implements EnumPropertyValue {
    /**
     * Ignores the error and continues execution with the next element in children elements, or
     * starts a new iteration.
     */
    CONTINUE("continue"),
    /**
     * Does not execute following elements in current iteration and jumps to a new iteration.
     */
    START_NEXT_ITERATION("startnextloop"),
    /**
     * Stops the thread, not executing any further children elements or iterations.
     */
    STOP_THREAD("stopthread"),
    /**
     * Stops the test plan, with all associated threads, when all current samples end.
     */
    STOP_TEST("stoptest"),
    /**
     * Stops the test plan abruptly, with all associated threads, interrupting current samples.
     */
    STOP_TEST_NOW("stoptestnow");

    private final String propertyValue;

    SampleErrorAction(String propertyValue) {
      this.propertyValue = propertyValue;
    }

    @Override
    public String propertyValue() {
      return propertyValue;
    }

  }

}
