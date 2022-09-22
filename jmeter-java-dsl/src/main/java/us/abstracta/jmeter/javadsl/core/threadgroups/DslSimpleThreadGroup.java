package us.abstracta.jmeter.javadsl.core.threadgroups;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.ThreadGroup;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;

/**
 * Contains common logic for thread groups that only require children in constructor and provide
 * simple settings (like iterations, threads, etc).
 *
 * @param <T> is the type of the thread group. Used for proper contract definition of fluent builder
 *            methods.
 * @since 0.33
 */
public abstract class DslSimpleThreadGroup<T extends DslSimpleThreadGroup<T>> extends
    BaseThreadGroup<T> {

  protected int threadCount = 1;
  protected int iterations = 1;

  protected DslSimpleThreadGroup(String name, Class<? extends JMeterGUIComponent> guiClass,
      List<ThreadGroupChild> children) {
    super(name, guiClass, children);
  }

  /**
   * Allows specifying the number of iterations (times) to run the child elements of the thread
   * group for each configured thread.
   *
   * @param iterations contains number of iterations to run in each thread of the thread group. When
   *                   -1 is specified, then the thread group will have no limit of iterations and
   *                   will run until some other condition (like on error and stopping on error is
   *                   configured) stops the thread group. By default, this value is initialized to
   *                   1.
   * @return the thread group for further configuration or usage.
   */
  public T iterations(int iterations) {
    this.iterations = iterations;
    return (T) this;
  }

  /**
   * Allows specifying the number of threads to run in parallel iterations of provided tasks.
   *
   * @param threadCount is the number of threads to use. By default is 1.
   * @return the thread group for further configuration or usage.
   */
  public T threadCount(int threadCount) {
    this.threadCount = threadCount;
    return (T) this;
  }

  /**
   * Allows specifying thread group children elements (samplers, listeners, post processors, etc.).
   * <p>
   * This method is just an alternative to the constructor specification of children, and is handy
   * when you want to keep general thread group settings together and then specify children (instead
   * of specifying children and at the end alternative settings like iterations).
   *
   * @param children list of test elements to add as children of the thread group.
   * @return the altered thread group to allow for fluent API usage.
   */
  @Override
  public T children(ThreadGroupChild... children) {
    return super.children(children);
  }

  @Override
  protected AbstractThreadGroup buildThreadGroup() {
    AbstractThreadGroup ret = buildSimpleThreadGroup();
    LoopController loopController = new LoopController();
    ret.setNumThreads(threadCount);
    ret.setSamplerController(loopController);
    loopController.setLoops(iterations);
    return ret;
  }

  protected abstract AbstractThreadGroup buildSimpleThreadGroup();

  protected abstract static class SimpleThreadGroupCodeBuilder<T extends TestElement> extends
      SingleTestElementCallBuilder<T> {

    private final String defaultName;

    protected SimpleThreadGroupCodeBuilder(Class<T> testElementClass, String defaultName,
        List<Method> builderMethods) {
      super(testElementClass, builderMethods);
      this.defaultName = defaultName;
    }

    @Override
    protected MethodCall buildMethodCall(T testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      return buildMethodCall(paramBuilder.nameParam(defaultName),
          new ChildrenParam<>(ThreadGroupChild[].class))
          .chain("threadCount", paramBuilder.intParam(ThreadGroup.NUM_THREADS, 1))
          .chain("iterations",
              paramBuilder.intParam(ThreadGroup.MAIN_CONTROLLER + "/" + LoopController.LOOPS, 1))
          .chain("sampleErrorAction", paramBuilder.enumParam(AbstractThreadGroup.ON_SAMPLE_ERROR,
              SampleErrorAction.CONTINUE));
    }

  }

}
