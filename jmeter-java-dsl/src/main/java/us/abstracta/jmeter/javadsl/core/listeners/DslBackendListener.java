package us.abstracta.jmeter.javadsl.core.listeners;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.visualizers.backend.BackendListener;
import org.apache.jmeter.visualizers.backend.BackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerGui;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;
import us.abstracta.jmeter.javadsl.core.testelements.MultiLevelTestElement;

/**
 * Contains common logic used by test elements that use the backend listener.
 *
 * @since 0.20
 */
public abstract class DslBackendListener extends BaseTestElement implements MultiLevelTestElement {

  protected final String url;
  private final Class<? extends BackendListenerClient> listenerClass;
  private int queueSize = 5000;

  protected DslBackendListener(Class<? extends BackendListenerClient> listenerClass, String url) {
    super("Backend Listener", BackendListenerGui.class);
    this.url = url;
    this.listenerClass = listenerClass;
  }

  /**
   * Specifies the length of sample results queue used to asynchronously send the information to
   * backend.
   * <p>
   * When the queue reaches this limit, then the test plan execution will be affected since sample
   * results will get blocked until there is space in the queue, affecting the general execution of
   * the test plan and in consequence collected metrics.
   * <p>
   * When not specified, this value defaults to 5000.
   *
   * @param queueSize the size of the queue to use
   */
  protected void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }

  @Override
  protected TestElement buildTestElement() {
    BackendListener ret = new BackendListener();
    ret.setClassname(listenerClass.getName());
    ret.setQueueSize(String.valueOf(queueSize));
    ret.setArguments(buildArguments());
    return ret;
  }

  private Arguments buildArguments() {
    try {
      Arguments ret = listenerClass.newInstance().getDefaultParameters();
      addAllArguments(buildListenerArguments(), ret);
      return ret;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract Arguments buildListenerArguments();

  private void addAllArguments(Arguments args, Arguments ret) {
    for (JMeterProperty prop : args) {
      Argument arg = (Argument) prop.getObjectValue();
      ret.removeArgument(arg.getName());
      ret.addArgument(arg.getName(), arg.getValue());
    }
  }

}
