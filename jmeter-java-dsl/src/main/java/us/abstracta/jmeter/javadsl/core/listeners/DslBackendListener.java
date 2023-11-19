package us.abstracta.jmeter.javadsl.core.listeners;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.visualizers.backend.BackendListener;
import org.apache.jmeter.visualizers.backend.BackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerGui;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;

/**
 * Contains common logic used by test elements that use the backend listener.
 *
 * @since 0.20
 */
public abstract class DslBackendListener<T extends DslBackendListener<T>> extends BaseListener {

  protected final String url;
  protected final Class<? extends BackendListenerClient> listenerClass;
  protected int queueSize = 5000;

  protected DslBackendListener(Class<? extends BackendListenerClient> listenerClass, String url) {
    super("Backend Listener", BackendListenerGui.class);
    this.url = url;
    this.listenerClass = listenerClass;
  }

  /**
   * Specifies the length of sample results queue used to asynchronously send the information to the
   * backend service.
   * <p>
   * When the queue reaches this limit, then the test plan execution will be affected since sample
   * results will get blocked until there is space in the queue, affecting the general execution of
   * the test plan and in consequence collected metrics.
   * <p>
   * When not specified, this value defaults to 5000.
   *
   * @param queueSize the size of the queue to use.
   * @return the listener for further configuration or usage.
   */
  public T queueSize(int queueSize) {
    this.queueSize = queueSize;
    return (T) this;
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

  protected abstract static class BackendListenerCodeBuilder extends MethodCallBuilder {

    private final Class<? extends BackendListenerClient> backendListenerClass;

    public BackendListenerCodeBuilder(Class<? extends BackendListenerClient> backendListenerClass,
        List<Method> builderMethods) {
      super(builderMethods);
      this.backendListenerClass = backendListenerClass;
    }

    @Override
    public boolean matches(MethodCallContext context) {
      TestElement testElement = context.getTestElement();
      return testElement instanceof BackendListener
          && backendListenerClass.getName().equals(((BackendListener) testElement).getClassname());
    }

    @Override
    protected MethodCall buildMethodCall(MethodCallContext context) {
      try {
        TestElementParamBuilder paramBuilder = new TestElementParamBuilder(
            context.getTestElement());
        Map<String, String> args = ((BackendListener) context.getTestElement()).getArguments()
            .getArgumentsAsMap();
        Map<String, String> defaultValues = backendListenerClass.newInstance()
            .getDefaultParameters()
            .getArgumentsAsMap();
        return buildBackendListenerCall(args, defaultValues)
            .chain("queueSize", paramBuilder.intParam(BackendListener.QUEUE_SIZE));
      } catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    protected abstract MethodCall buildBackendListenerCall(Map<String, String> args,
        Map<String, String> defaultValues);

    protected MethodParam buildArgParam(String argName, Map<String, String> args,
        Map<String, String> defaultArgs) {
      return new StringParam(args.get(argName), defaultArgs.get(argName));
    }

  }

}
