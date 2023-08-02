package us.abstracta.jmeter.javadsl.core.threadgroups;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kg.apc.jmeter.threads.UltimateThreadGroup;
import kg.apc.jmeter.threads.UltimateThreadGroupGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.core.threadgroups.defaultthreadgroup.SimpleThreadGroupHelper;
import us.abstracta.jmeter.javadsl.core.threadgroups.defaultthreadgroup.Stage;
import us.abstracta.jmeter.javadsl.core.threadgroups.defaultthreadgroup.UltimateThreadGroupHelper;
import us.abstracta.jmeter.javadsl.core.util.SingleSeriesTimelinePanel;

/**
 * Represents the standard thread group test element included by JMeter.
 * <p>
 * For complex thread profiles that can't be mapped to JMeter built-in thread group element, the DSL
 * uses <a href="https://jmeter-plugins.org/wiki/UltimateThreadGroup/">Ultimate Thread Group
 * plugin</a>
 *
 * @since 0.1
 */
public class DslDefaultThreadGroup extends BaseThreadGroup<DslDefaultThreadGroup> {

  private static final Integer ZERO = 0;
  protected final List<Stage> stages = new ArrayList<>();

  public DslDefaultThreadGroup(String name, int threads, int iterations,
      List<ThreadGroupChild> children) {
    this(name, children);
    checkThreadCount(threads);
    stages.add(new Stage(threads, Duration.ZERO, null));
    stages.add(new Stage(threads, null, iterations));
  }

  private DslDefaultThreadGroup(String name, List<ThreadGroupChild> children) {
    super(name != null ? name : "Thread Group", ThreadGroupGui.class, children);
  }

  public DslDefaultThreadGroup(String name, int threads, Duration duration,
      List<ThreadGroupChild> children) {
    this(name, children);
    checkThreadCount(threads);
    stages.add(new Stage(threads, Duration.ZERO, null));
    stages.add(new Stage(threads, duration, null));
  }

  public DslDefaultThreadGroup(String name) {
    this(name, Collections.emptyList());
  }

  private void checkThreadCount(int threads) {
    if (threads <= 0) {
      throw new IllegalArgumentException("Threads count must be >=1");
    }
  }

  /**
   * Allows ramping up or down threads with a given duration.
   * <p>
   * It is usually advised to use this method when working with considerable amount of threads to
   * avoid load of creating all the threads at once to affect test results.
   * <p>
   * JMeter will create (or remove) a thread every {@code rampUp.seconds * 1000 / threadCount}
   * milliseconds.
   * <p>
   * If you specify a thread duration time (instead of iterations), take into consideration that
   * ramp up is not considered as part of thread duration time. For example: if you have a thread
   * group duration of 10 seconds, and a ramp-up of 10 seconds, the last threads (and the test plan
   * run) will run at least (duration may vary depending on test plan contents) after 20 seconds of
   * starting the test.
   * <p>
   * You can use this method multiple times in a thread group and in conjunction with
   * {@link #holdFor(Duration)} and {@link #rampToAndHold(int, Duration, Duration)} to elaborate
   * complex test plan profiles.
   * <p>
   * Eg:
   * <pre>{@code
   *  threadGroup()
   *    .rampTo(10, Duration.ofSeconds(10))
   *    .rampTo(5, Duration.ofSeconds(10))
   *    .rampToAndHold(20, Duration.ofSeconds(5), Duration.ofSeconds(10))
   *    .rampTo(0, Duration.ofSeconds(5))
   *    .children(...)
   * }</pre>
   *
   * @param threadCount specifies the final number of threads after the given period.
   * @param duration    duration taken to reach the given threadCount and move to the next stage or
   *                    end the test plan. Since JMeter only supports specifying times in seconds,
   *                    if you specify a smaller granularity (like milliseconds) it will be rounded
   *                    up to seconds.
   * @return the thread group for further configuration or usage.
   * @throws IllegalStateException if used after an iterations stage, since JMeter does not provide
   *                               built-in thread group to support such scenario.
   * @since 0.18
   */
  public DslDefaultThreadGroup rampTo(int threadCount, Duration duration) {
    if (threadCount < 0) {
      throw new IllegalArgumentException("Thread count must be >=0");
    }
    checkRampNotAfterIterations();
    addStage(new Stage(threadCount, duration, null));
    return this;
  }

  /**
   * Same as {@link #rampTo(int, Duration)} but allowing to use JMeter expressions (variables or
   * functions) to solve the actual parameter values.
   * <p>
   * This is usually used in combination with properties to define values that change between
   * environments or different test runs. Eg: <pre>{@code rampTo("${THREADS}", "${RAMP}"}</pre>.
   * <p>
   * This method can only be used for simple thread group configurations. Allowed combinations are:
   * rampTo, rampTo + holdFor, holdFor + rampTo + holdFor, rampTo + holdIterating, holdFor + rampTo
   * + holdIterating.
   *
   * @param threadCount a JMeter expression that returns the number of threads to ramp to.
   * @param duration    a JMeter expression that returns the number of seconds to take for the
   *                    ramp.
   * @return the thread group for further configuration or usage.
   * @see #rampTo(int, Duration)
   * @since 0.57
   */
  public DslDefaultThreadGroup rampTo(String threadCount, String duration) {
    checkRampNotAfterIterations();
    addStage(new Stage(threadCount, duration, null));
    return this;
  }

  private void checkRampNotAfterIterations() {
    if (isLastStageHoldingForIterations()) {
      throw new IllegalStateException(
          "Ramping up/down after holding for iterations is not supported. "
              + "If you used constructor with iterations and some ramp "
              + "(eg: threadGroup(X, Y, ...).rampTo(X, Z)), consider using "
              + "threadGroup().rampTo(X, Z).holdIterating(Y) instead");
    }
  }

  private boolean isLastStageHoldingForIterations() {
    return !stages.isEmpty() && getLastStage().duration() == null;
  }

  private Stage getLastStage() {
    return stages.get(stages.size() - 1);
  }

  private void addStage(Stage stage) {
    stages.add(stage);
    if (!isSimpleThreadGroup() && stages.stream().anyMatch(s -> !s.isFixedStage())) {
      stages.remove(stages.size() - 1);
      throw new UnsupportedOperationException(
          "The DSL does not yet support configuring multiple thread ramps with ramp or hold "
              + "parameters using jmeter expressions. If you need this please create an issue in "
              + "Github repository.");
    }
  }

  private boolean isSimpleThreadGroup() {
    return stages.size() <= 1
        || stages.size() == 2 && (
        ZERO.equals(stages.get(0).threadCount())
            || stages.get(0).threadCount().equals(stages.get(1).threadCount()))
        || stages.size() == 3 && (
        ZERO.equals(stages.get(0).threadCount())
            && stages.get(1).threadCount().equals(stages.get(2).threadCount()));
  }

  /**
   * Specifies to keep current number of threads for a given duration.
   * <p>
   * This method is usually used in combination with {@link #rampTo(int, Duration)} to define the
   * profile of the test plan.
   *
   * @param duration duration to hold the current number of threads until moving to next stage or
   *                 ending the test plan. Since JMeter only supports specifying times in seconds,
   *                 if you specify a smaller granularity (like milliseconds) it will be rounded up
   *                 to seconds.
   * @return the thread group for further configuration or usage.
   * @see #rampTo(int, Duration)
   * @since 0.18
   */
  public DslDefaultThreadGroup holdFor(Duration duration) {
    checkHoldNotAfterIterations();
    addStage(new Stage(getPrevThreadsCount(), duration, null));
    return this;
  }

  /**
   * Same as {@link #holdFor(Duration)} but allowing to use JMeter expressions (variables or
   * functions) to solve the duration.
   * <p>
   * This is usually used in combination with properties to define values that change between
   * environments or different test runs. Eg: <pre>{@code holdFor("${DURATION}"}</pre>.
   * <p>
   * This method can only be used for simple thread group configurations. Allowed combinations are:
   * rampTo, rampTo + holdFor, holdFor + rampTo + holdFor, rampTo + holdIterating, holdFor + rampTo
   * + holdIterating.
   *
   * @param duration a JMeter expression that returns the number of seconds to hold current thread
   *                 groups.
   * @return the thread group for further configuration or usage.
   * @see #holdFor(Duration)
   * @since 0.57
   */
  public DslDefaultThreadGroup holdFor(String duration) {
    Object threadsCount = getPrevThreadsCount();
    checkHoldNotAfterIterations();
    addStage(new Stage(threadsCount, duration, null));
    return this;
  }

  private void checkHoldNotAfterIterations() {
    if (isLastStageHoldingForIterations()) {
      throw new IllegalStateException(
          "Holding for duration after holding for iterations is not supported.");
    }
  }

  private Object getPrevThreadsCount() {
    return stages.isEmpty() ? 0 : getLastStage().threadCount();
  }

  /**
   * Specifies to keep current number of threads until they execute the given number of iterations
   * each.
   * <p>
   * <b>Warning: </b> holding for iterations can be added to a thread group that has an initial
   * stage with 0 threads followed by a stage ramping up, or only a stage ramping up, or no stages
   * at all.
   *
   * @param iterations number of iterations to execute the test plan steps each thread.
   *                   <p>
   *                   If you specify -1, then threads will iterate until test plan execution is
   *                   interrupted (you manually stop the running process, there is an error and
   *                   thread group is configured to stop on error, or some other explicit
   *                   termination condition).
   *                   <p>
   *                   <b>Setting this property to -1 is in general not advised</b>, since you
   *                   might
   *                   inadvertently end up running a test plan without limits consuming unnecessary
   *                   computing power. Prefer specifying a big value as a safe limit for iterations
   *                   or duration instead.
   * @return the thread group for further configuration or usage.
   * @throws IllegalStateException when adding iterations would result in not supported JMeter
   *                               thread group.
   * @since 0.18
   */
  public DslDefaultThreadGroup holdIterating(int iterations) {
    checkIterationsPreConditions();
    addStage(new Stage(getLastStage().threadCount(), null, iterations));
    return this;
  }

  /**
   * Same as {@link #holdIterating(int)} but allowing to use JMeter expressions (variables or
   * functions) to solve the iterations.
   * <p>
   * This is usually used in combination with properties to define values that change between
   * environments or different test runs. Eg: <pre>{@code holdIterating("${ITERATIONS}"}</pre>.
   * <p>
   * This method can only be used for simple thread group configurations. Allowed combinations are:
   * rampTo, rampTo + holdFor, holdFor + rampTo + holdFor, rampTo + holdIterating, holdFor + rampTo
   * + holdIterating.
   *
   * @param iterations a JMeter expression that returns the number of iterations for current threads
   *                   to execute.
   * @return the thread group for further configuration or usage.
   * @see #holdIterating(int)
   * @since 0.57
   */
  public DslDefaultThreadGroup holdIterating(String iterations) {
    checkIterationsPreConditions();
    addStage(new Stage(getLastStage().threadCount(), null, iterations));
    return this;
  }

  private void checkIterationsPreConditions() {
    if (!(stages.size() == 1 && !ZERO.equals(stages.get(0).threadCount())
        || stages.size() == 2 && ZERO.equals(stages.get(0).threadCount())
        && !ZERO.equals(stages.get(1).threadCount()))) {
      throw new IllegalStateException(
          "Holding for iterations is only supported after initial hold and ramp, or ramp.");
    }
    if (ZERO.equals(getLastStage().threadCount())) {
      throw new IllegalStateException("Can't hold for iterations with no threads.");
    }
  }

  /**
   * simply combines {@link #rampTo(int, Duration)} and {@link #holdFor(Duration)} which are usually
   * used in combination.
   *
   * @param threads      number of threads to ramp threads up/down to.
   * @param rampDuration duration taken to reach the given threadCount to start holding that number
   *                     of threads.
   * @param holdDuration duration to hold the given number of threads, after the ramp, until moving
   *                     to next stage or ending the test plan.
   * @return the thread group for further configuration or usage.
   * @see #rampTo(int, Duration)
   * @see #holdFor(Duration)
   * @since 0.18
   */
  public DslDefaultThreadGroup rampToAndHold(int threads, Duration rampDuration,
      Duration holdDuration) {
    return rampTo(threads, rampDuration)
        .holdFor(holdDuration);
  }

  /**
   * Same as {@link #rampToAndHold(int, Duration, Duration)} but allowing to use JMeter expressions
   * (variables or functions) to solve the actual parameter values.
   * <p>
   * This is usually used in combination with properties to define values that change between
   * environments or different test runs. Eg:
   * <pre>{@code rampToAndHold("${THREADS}", "${RAMP}" ,"${DURATION}"}</pre>.
   * <p>
   * This method can only be used for simple thread group configurations. Allowed combinations are:
   * rampTo, rampTo + holdFor, holdFor + rampTo + holdFor, rampTo + holdIterating, holdFor + rampTo
   * + holdIterating.
   *
   * @param threads      a JMeter expression that returns the number of threads to ramp to.
   * @param rampDuration a JMeter expression that returns the number of seconds to take for the
   *                     ramp.
   * @param holdDuration a JMeter expression that returns the number of seconds to hold current
   *                     thread groups.
   * @return the thread group for further configuration or usage.
   * @see #rampToAndHold(int, Duration, Duration)
   * @since 0.57
   */
  public DslDefaultThreadGroup rampToAndHold(String threads, String rampDuration,
      String holdDuration) {
    return rampTo(threads, rampDuration)
        .holdFor(holdDuration);
  }

  /**
   * Allows specifying thread group children elements (samplers, listeners, post processors, etc.).
   * <p>
   * This method is just an alternative to the constructor specification of children, and is handy
   * when you want to keep general thread group settings together and then specify children (instead
   * of specifying threadCount &amp; duration/iterations, then children, and at the end alternative
   * settings like ramp-up period).
   *
   * @param children list of test elements to add as children of the thread group.
   * @return the thread group for further configuration or usage.
   * @since 0.12
   */
  @Override
  public DslDefaultThreadGroup children(ThreadGroupChild... children) {
    return super.children(children);
  }

  @Override
  public AbstractThreadGroup buildThreadGroup() {
    if (isSimpleThreadGroup()) {
      return new SimpleThreadGroupHelper(stages).buildThreadGroup();
    } else {
      guiClass = UltimateThreadGroupGui.class;
      return new UltimateThreadGroupHelper(stages).buildThreadGroup();
    }
  }

  /**
   * Shows a graph with a timeline of planned threads count execution for this test plan.
   * <p>
   * The graph will be displayed in a popup window.
   * <p>
   * This method is provided mainly to ease test plan designing when working with complex thread
   * group profiles (several stages with ramps and holds).
   *
   * @since 0.26
   */
  public void showTimeline() {
    if (stages.stream().anyMatch(s -> !s.isFixedStage())) {
      throw new IllegalStateException(
          "Can't display timeline when some JMeter expression is used in any ramp or hold.");
    }
    SingleSeriesTimelinePanel chart = new SingleSeriesTimelinePanel("Threads");
    chart.add(0, 0);
    stages.forEach(s -> chart.add(((Duration) s.duration()).toMillis(), (int) s.threadCount()));
    showAndWaitFrameWith(chart, name + " threads timeline", 800, 300);
  }

  public static class CodeBuilder extends MethodCallBuilder {

    public CodeBuilder(List<Method> builderMethods) {
      super(builderMethods);
    }

    @Override
    public boolean matches(MethodCallContext context) {
      TestElement testElement = context.getTestElement();
      return testElement.getClass() == ThreadGroup.class
          || testElement.getClass() == UltimateThreadGroup.class;
    }

    @Override
    protected MethodCall buildMethodCall(MethodCallContext context) {
      MethodCall ret;
      TestElement testElement = context.getTestElement();
      if (testElement.getClass() == ThreadGroup.class) {
        ret = new SimpleThreadGroupHelper.CodeBuilder(builderMethods).buildMethodCall(context);
      } else {
        ret = new UltimateThreadGroupHelper.CodeBuilder(builderMethods).buildMethodCall(context);
      }
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      ret.chain("sampleErrorAction",
          paramBuilder.enumParam(AbstractThreadGroup.ON_SAMPLE_ERROR, SampleErrorAction.CONTINUE));
      return ret;
    }

  }

}
