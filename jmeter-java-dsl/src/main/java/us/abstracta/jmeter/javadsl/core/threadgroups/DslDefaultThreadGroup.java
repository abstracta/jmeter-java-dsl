package us.abstracta.jmeter.javadsl.core.threadgroups;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.threads.UltimateThreadGroup;
import kg.apc.jmeter.threads.UltimateThreadGroupGui;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.ChildrenParam;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.DurationParam;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.IntParam;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.StringParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
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

  private final List<Stage> stages = new ArrayList<>();

  // represents a stage in thread profiling (ramp up or down, hold duration or iterations).
  private static class Stage {

    private final int threadCount;
    private final Duration duration;
    private final int iterations;

    private Stage(int threadCount, Duration duration) {
      this.threadCount = threadCount;
      this.duration = duration;
      this.iterations = 0;
    }

    private Stage(int threadCount, int iterations) {
      this.threadCount = threadCount;
      this.iterations = iterations;
      this.duration = null;
    }

  }

  public DslDefaultThreadGroup(String name, int threads, int iterations,
      List<ThreadGroupChild> children) {
    this(name, children);
    checkThreadCount(threads);
    if (iterations <= 0) {
      throw new IllegalArgumentException("Iterations must be >=1");
    }
    stages.add(new Stage(threads, Duration.ZERO));
    stages.add(new Stage(threads, iterations));
  }

  private DslDefaultThreadGroup(String name, List<ThreadGroupChild> children) {
    super(name != null ? name : "Thread Group", ThreadGroupGui.class, children);
  }

  public DslDefaultThreadGroup(String name, int threads, Duration duration,
      List<ThreadGroupChild> children) {
    this(name, children);
    checkThreadCount(threads);
    stages.add(new Stage(threads, Duration.ZERO));
    stages.add(new Stage(threads, duration));
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
   * You can use this method multiple times in a thread group and in conjunction with {@link
   * #holdFor(Duration)} and {@link #rampToAndHold(int, Duration, Duration)} to elaborate complex
   * test plan profiles.
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
   * @return the DslThreadGroup instance to use fluent API to set additional options.
   * @throws IllegalStateException if used after an iterations stage, since JMeter does not provide
   *                               built-in thread group to support such scenario.
   * @since 0.18
   */
  public DslDefaultThreadGroup rampTo(int threadCount, Duration duration) {
    if (threadCount < 0) {
      throw new IllegalArgumentException("Thread count must be >=0");
    }
    if (!stages.isEmpty() && getLastStage().duration == null) {
      throw new IllegalStateException(
          "Ramping up/down after holding for iterations is not supported");
    }
    stages.add(new Stage(threadCount, duration));
    return this;
  }

  private Stage getLastStage() {
    return stages.get(stages.size() - 1);
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
   * @return the DslThreadGroup instance to use fluent API to set additional options.
   * @see #rampTo(int, Duration)
   * @since 0.18
   */
  public DslDefaultThreadGroup holdFor(Duration duration) {
    int threadsCount = stages.isEmpty() ? 0 : getLastStage().threadCount;
    stages.add(new Stage(threadsCount, duration));
    return this;
  }

  /**
   * Specifies to keep current number of threads until they execute the given number of iterations
   * each.
   *
   * <b>Warning: </b> holding for iterations can be added to a thread group that has an initial
   * stage with 0 threads followed by a stage ramping up, or only a stage ramping up, or no stages
   * at all.
   *
   * @param iterations number of iterations to execute the test plan steps each thread.
   * @return the DslThreadGroup instance to use fluent API to set additional options.
   * @throws IllegalStateException when adding iterations would result in not supported JMeter
   *                               thread group.
   * @since 0.18
   */
  public DslDefaultThreadGroup holdIterating(int iterations) {
    if (iterations < 0) {
      throw new IllegalArgumentException("Iterations must be >=0");
    }
    if (!(stages.size() == 1 && stages.get(0).threadCount != 0
        || stages.size() == 2 && stages.get(0).threadCount == 0
        && stages.get(1).threadCount != 0)) {
      throw new IllegalStateException(
          "Holding for iterations is only supported after initial hold and ramp, or ramp.");
    }
    stages.add(new Stage(getLastStage().threadCount, iterations));
    return this;
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
   * @return the DslThreadGroup instance to use fluent API to set additional options.
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
   * Allows specifying thread group children elements (samplers, listeners, post processors, etc.).
   * <p>
   * This method is just an alternative to the constructor specification of children, and is handy
   * when you want to keep general thread group settings together and then specify children (instead
   * of specifying threadCount &amp; duration/iterations, then children, and at the end alternative
   * settings like ramp-up period).
   *
   * @param children list of test elements to add as children of the thread group.
   * @return the altered thread group to allow for fluent API usage.
   * @since 0.12
   */
  @Override
  public DslDefaultThreadGroup children(ThreadGroupChild... children) {
    return super.children(children);
  }

  @Override
  public AbstractThreadGroup buildThreadGroup() {
    return isSimpleThreadGroup() ? buildSimpleThreadGroup() : buildUltimateThreadGroup();
  }

  private boolean isSimpleThreadGroup() {
    return stages.size() <= 1
        || stages.size() == 2 && (stages.get(0).threadCount == 0
        || stages.get(0).threadCount == stages.get(1).threadCount)
        || stages.size() == 3 && (stages.get(0).threadCount == 0
        && stages.get(1).threadCount == stages.get(2).threadCount);
  }

  private AbstractThreadGroup buildSimpleThreadGroup() {
    int threads = 1;
    int iterations = 1;
    Duration rampUpPeriod = null;
    Duration duration = null;
    Duration delay = null;
    if (!stages.isEmpty()) {
      Stage firstStage = stages.get(0);
      if (firstStage.threadCount == 0) {
        delay = firstStage.duration;
      } else {
        rampUpPeriod = firstStage.duration;
        threads = firstStage.threadCount;
      }
      iterations = firstStage.iterations;
      if (stages.size() > 1) {
        Stage secondStage = stages.get(1);
        threads = secondStage.threadCount;
        iterations = secondStage.iterations;
        if (firstStage.threadCount == 0) {
          rampUpPeriod = secondStage.duration;
          if (stages.size() > 2) {
            Stage lastStage = stages.get(2);
            duration = lastStage.duration;
            iterations = lastStage.iterations;
          }
        } else {
          duration = secondStage.duration;
        }
      }
    }
    if (rampUpPeriod != null && !Duration.ZERO.equals(rampUpPeriod) &&
        (iterations == 0 || duration != null)) {
      duration = duration != null ? duration.plus(rampUpPeriod) : rampUpPeriod;
    }
    return buildSimpleThreadGroup(threads, iterations, rampUpPeriod, duration, delay);
  }

  private ThreadGroup buildSimpleThreadGroup(int threads, int iterations, Duration rampUpPeriod,
      Duration duration, Duration delay) {
    ThreadGroup ret = new ThreadGroup();
    ret.setNumThreads(Math.max(threads, 1));
    ret.setRampUp(
        (int) durationToSeconds(rampUpPeriod == null ? Duration.ZERO : rampUpPeriod));
    LoopController loopController = new LoopController();
    ret.setSamplerController(loopController);
    if (duration != null) {
      loopController.setLoops(-1);
      ret.setDuration(durationToSeconds(duration));
    } else {
      loopController.setLoops(iterations);
    }
    if (delay != null) {
      ret.setDelay(durationToSeconds(delay));
    }
    if (duration != null || delay != null) {
      ret.setScheduler(true);
    }
    return ret;
  }

  private AbstractThreadGroup buildUltimateThreadGroup() {
    guiClass = UltimateThreadGroupGui.class;
    UltimateThreadGroup ret = new UltimateThreadGroup();
    PowerTableModel table = buildUltimateThreadGroupTableModel();
    buildUltimateThreadGroupSchedules().forEach(s -> table.addRow(s.buildTableRow()));
    ret.setData(JMeterPluginsUtils.tableModelRowsToCollectionProperty(table,
        UltimateThreadGroup.DATA_PROPERTY));
    LoopController loopController = new LoopController();
    loopController.setLoops(-1);
    loopController.setContinueForever(true);
    ret.setSamplerController(loopController);
    return ret;
  }

  private static PowerTableModel buildUltimateThreadGroupTableModel() {
    return new PowerTableModel(UltimateThreadGroupGui.columnIdentifiers,
        UltimateThreadGroupGui.columnClasses);
  }

  private List<UltimateThreadSchedule> buildUltimateThreadGroupSchedules() {
    List<UltimateThreadSchedule> ret = new ArrayList<>();
    Duration delay = Duration.ZERO;
    int threads = 0;
    Stack<UltimateThreadSchedule> stack = new Stack<>();
    UltimateThreadSchedule curr = new UltimateThreadSchedule(0, Duration.ZERO, Duration.ZERO,
        Duration.ZERO, Duration.ZERO);
    for (Stage s : stages) {
      if (s.threadCount == threads) {
        curr.hold = curr.hold.plus(s.duration);
      } else if (s.threadCount > threads) {
        stack.add(curr);
        curr = new UltimateThreadSchedule(s.threadCount - threads, delay, s.duration, Duration.ZERO,
            Duration.ZERO);
      } else {
        int diff = threads - s.threadCount;
        Duration shutdown = s.duration;
        while (diff > curr.threadCount) {
          curr.shutdown = interpolateDurationForThreadCountWithRamp(curr.threadCount, diff,
              shutdown);
          diff -= curr.threadCount;
          shutdown = shutdown.minus(curr.shutdown);
          curr = completeCurrentSchedule(curr, ret, stack);
        }
        if (diff == curr.threadCount) {
          curr.shutdown = shutdown;
        } else {
          Duration start = interpolateDurationForThreadCountWithRamp(diff, curr.threadCount,
              curr.startup);
          UltimateThreadSchedule last = curr;
          curr = new UltimateThreadSchedule(diff, curr.delay.plus(curr.startup).minus(start), start,
              curr.hold, shutdown);
          last.threadCount -= diff;
          last.startup = last.startup.minus(start);
          last.hold = Duration.ZERO;
          stack.push(last);
        }
        curr = completeCurrentSchedule(curr, ret, stack);
      }
      threads = s.threadCount;
      delay = delay.plus(s.duration);
    }
    while (!stack.isEmpty()) {
      curr = completeCurrentSchedule(curr, ret, stack);
    }
    ret.sort(Comparator.comparing(r -> r.delay.toMillis()));
    return ret;
  }

  protected static class UltimateThreadSchedule {

    private int threadCount;
    private final Duration delay;
    private Duration startup;
    private Duration hold;
    private Duration shutdown;

    public UltimateThreadSchedule(int threadCount, Duration delay, Duration startup,
        Duration hold, Duration shutdown) {
      this.threadCount = threadCount;
      this.delay = delay;
      this.startup = startup;
      this.hold = hold;
      this.shutdown = shutdown;
    }

    public static UltimateThreadSchedule fromTableRow(Object[] row) {
      int i = 0;
      return new UltimateThreadSchedule(Integer.parseInt(stringProp(row[i++])), duration(row[i++]),
          duration(row[i++]), duration(row[i++]), duration(row[i]));
    }

    private static Duration duration(Object val) {
      return Duration.ofSeconds(Long.parseLong(stringProp(val)));
    }

    private static String stringProp(Object val) {
      return ((JMeterProperty) val).getStringValue();
    }

    public Object[] buildTableRow() {
      return new Object[]{String.valueOf(threadCount),
          String.valueOf(durationToSeconds(delay)),
          String.valueOf(durationToSeconds(startup)),
          String.valueOf(durationToSeconds(hold)),
          String.valueOf(durationToSeconds(shutdown))};
    }

  }

  private static Duration interpolateDurationForThreadCountWithRamp(int threadCount,
      int rampThreads, Duration rampDuration) {
    return Duration.ofMillis(
        (long) (rampDuration.toMillis() * ((double) threadCount / rampThreads)));
  }

  private UltimateThreadSchedule completeCurrentSchedule(UltimateThreadSchedule curr,
      List<UltimateThreadSchedule> ret, Stack<UltimateThreadSchedule> stack) {
    ret.add(curr);
    UltimateThreadSchedule last = curr;
    curr = stack.pop();
    curr.hold = curr.hold.plus(last.startup).plus(last.hold).plus(last.shutdown);
    return curr;
  }

  /**
   * Shows a graph with a timeline of planned threads count execution for this test plan.
   *
   * @since 0.18
   * @deprecated as of 0.26, use {@link #showTimeline()} instead.
   */
  @Deprecated
  public void showThreadsTimeline() {
    showTimeline();
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
    SingleSeriesTimelinePanel chart = new SingleSeriesTimelinePanel("Threads");
    chart.add(0, 0);
    stages.forEach(s -> chart.add(s.duration.toMillis(), s.threadCount));
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
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      if (testElement.getClass() == ThreadGroup.class) {
        ret = buildSimpleThreadGroupMethodCall(paramBuilder);
      } else {
        ret = buildUltimateThreadGroupMethodCall(paramBuilder);
      }
      ret.chain("sampleErrorAction", new SampleErrorActionMethodParam(paramBuilder));
      return ret;
    }

    private MethodCall buildSimpleThreadGroupMethodCall(TestElementParamBuilder testElement) {
      StringParam name = testElement.nameParam("Thread Group");
      IntParam threads = testElement.intParam(ThreadGroup.NUM_THREADS);
      DurationParam rampTime = testElement.durationParam(ThreadGroup.RAMP_TIME,
          Duration.ofSeconds(1));
      DurationParam duration = testElement.durationParam(ThreadGroup.DURATION);
      DurationParam delay = testElement.durationParam(ThreadGroup.DELAY);
      IntParam iterations = testElement.intParam(
          ThreadGroup.MAIN_CONTROLLER + "/" + LoopController.LOOPS);
      if ((rampTime.isDefault() || rampTime.getValue().isZero()) && delay.isDefault()) {
        return buildMethodCall(name, threads, duration.isDefault() ? iterations : duration,
            new ChildrenParam<>(ThreadGroupChild[].class));
      } else {
        MethodCall ret = buildMethodCall(name);
        if (!delay.isDefault()) {
          ret.chain("holdFor", delay);
        }
        if (!duration.isDefault()) {
          ret.chain("rampToAndHold", threads, rampTime,
              new DurationParam(duration.getValue().minus(rampTime.getValue())));
        } else {
          ret.chain("rampTo", threads, rampTime)
              .chain("holdIterating", iterations);
        }
        return ret;
      }
    }

    private MethodCall buildUltimateThreadGroupMethodCall(TestElementParamBuilder testElement) {
      StringParam name = testElement.nameParam("jp@gc - Ultimate Thread Group");
      MethodCall ret = buildMethodCall(name);
      return ThreadsTimeline.fromSchedules(schedulesProp(testElement))
          .addMethodCallsTo(ret);
    }

    private List<UltimateThreadSchedule> schedulesProp(TestElementParamBuilder testElement) {
      JMeterProperty schedulesProp = testElement.prop(UltimateThreadGroup.DATA_PROPERTY);
      PowerTableModel tableModel = buildUltimateThreadGroupTableModel();
      JMeterPluginsUtils.collectionPropertyToTableModelRows((CollectionProperty) schedulesProp,
          tableModel);
      List<UltimateThreadSchedule> ret = new ArrayList<>();
      for (int i = 0; i < tableModel.getRowCount(); i++) {
        ret.add(UltimateThreadSchedule.fromTableRow(tableModel.getRowData(i)));
      }
      return ret;
    }

  }

  private static class ThreadsTimeline {

    private final Map<Duration, Integer> points = new LinkedHashMap<>();
    private Duration lastDuration = Duration.ZERO;
    private int lastThreads;
    private double lastSlope;

    private ThreadsTimeline() {
    }

    private ThreadsTimeline(UltimateThreadSchedule s) {
      Duration duration = Duration.ZERO;
      if (!s.delay.isZero()) {
        duration = duration.plus(s.delay);
        points.put(duration, 0);
      }
      duration = duration.plus(s.startup);
      points.put(duration, s.threadCount);
      if (!s.hold.isZero()) {
        duration = duration.plus(s.hold);
        points.put(duration, s.threadCount);
      }
      duration = duration.plus(s.shutdown);
      points.put(duration, 0);
    }

    public static ThreadsTimeline fromSchedules(List<UltimateThreadSchedule> scheds) {
      ThreadsTimeline ret = new ThreadsTimeline();
      for (UltimateThreadSchedule s : scheds) {
        ret = ret.plus(new ThreadsTimeline(s));
      }
      return ret;
    }

    public ThreadsTimeline plus(ThreadsTimeline other) {
      if (points.isEmpty()) {
        return other;
      }
      ThreadsTimeline ret = new ThreadsTimeline();
      Iterator<Map.Entry<Duration, Integer>> thisIter = this.points.entrySet()
          .iterator();
      Iterator<Map.Entry<Duration, Integer>> otherIter = other.points.entrySet()
          .iterator();
      Entry<Duration, Integer> thisPoint = thisIter.next();
      Entry<Duration, Integer> otherPoint = otherIter.next();
      int prevThisThread = 0;
      int prevOtherThread = 0;
      while (thisPoint != null && otherPoint != null) {
        int durationOrder = thisPoint.getKey().compareTo(otherPoint.getKey());
        if (durationOrder == 0) {
          prevThisThread = thisPoint.getValue();
          prevOtherThread = otherPoint.getValue();
          ret.add(thisPoint.getKey(), prevThisThread + prevOtherThread);
          thisPoint = nextPoint(thisIter);
          otherPoint = nextPoint(otherIter);
        } else if (durationOrder < 0) {
          prevThisThread = thisPoint.getValue();
          ret.add(thisPoint.getKey(), prevThisThread + prevOtherThread);
          thisPoint = nextPoint(thisIter);
        } else {
          prevOtherThread = otherPoint.getValue();
          ret.add(otherPoint.getKey(), prevThisThread + prevOtherThread);
          otherPoint = nextPoint(otherIter);
        }
      }
      Entry<Duration, Integer> pendingPoint = thisPoint != null ? thisPoint : otherPoint;
      Iterator<Map.Entry<Duration, Integer>> pendingIter = thisPoint != null ? thisIter : otherIter;
      while (pendingPoint != null) {
        ret.add(pendingPoint.getKey(), pendingPoint.getValue());
        pendingPoint = nextPoint(pendingIter);
      }
      return ret;
    }

    private void add(Duration duration, int threads) {
      if (duration.equals(lastDuration) && threads == lastThreads) {
        return;
      }
      // we calculate slope to identify if we can compact the new point with previous one
      double slope;
      if (duration.equals(lastDuration)) {
        slope = threads > lastThreads ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
      } else {
        slope = (double) (threads - lastThreads) / duration.minus(lastDuration).getSeconds();
      }
      // using double comparison with threshold to avoid precision issues while comparing slopes
      if (Math.abs(slope - lastSlope) < 0.01) {
        points.remove(lastDuration);
      }
      points.put(duration, threads);
      lastDuration = duration;
      lastThreads = threads;
      lastSlope = slope;
    }

    private Entry<Duration, Integer> nextPoint(Iterator<Entry<Duration, Integer>> iter) {
      return iter.hasNext() ? iter.next() : null;
    }

    private MethodCall addMethodCallsTo(MethodCall methodCall) {
      int prevThreads = 0;
      Duration prevDuration = Duration.ZERO;
      Duration rampDuration = Duration.ZERO;
      Duration holdDuration = Duration.ZERO;
      for (Map.Entry<Duration, Integer> point : points.entrySet()) {
        if (point.getValue() == prevThreads) {
          holdDuration = holdDuration.plus(point.getKey().minus(prevDuration));
        } else {
          if (!rampDuration.isZero()) {
            if (!holdDuration.isZero()) {
              methodCall.chain("rampToAndHold", new IntParam(prevThreads),
                  new DurationParam(rampDuration), new DurationParam(holdDuration));
              holdDuration = Duration.ZERO;
            } else {
              chainRampTo(methodCall, prevThreads, rampDuration);
            }
          } else {
            if (!holdDuration.isZero()) {
              methodCall.chain("holdFor", new DurationParam(holdDuration));
              holdDuration = Duration.ZERO;
            }
          }
          rampDuration = point.getKey().minus(prevDuration);
        }
        prevThreads = point.getValue();
        prevDuration = point.getKey();
      }
      if (!rampDuration.isZero()) {
        chainRampTo(methodCall, prevThreads, rampDuration);

      }
      return methodCall;
    }

    private void chainRampTo(MethodCall methodCall, int threads, Duration rampDuration) {
      methodCall.chain("rampTo", new IntParam(threads), new DurationParam(rampDuration));
    }

  }

  private static class SampleErrorActionMethodParam extends MethodParam<SampleErrorAction> {

    protected SampleErrorActionMethodParam(TestElementParamBuilder testElement) {
      super(SampleErrorAction.class, SampleErrorAction.fromPropertyValue(
              testElement.prop(AbstractThreadGroup.ON_SAMPLE_ERROR).getStringValue()),
          SampleErrorAction.CONTINUE);
    }

    @Override
    public String buildCode(String indent) {
      return SampleErrorAction.class.getSimpleName() + "." + value.name();
    }

  }

}
