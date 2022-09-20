package us.abstracta.jmeter.javadsl.core.threadgroups;

import com.blazemeter.jmeter.threads.AbstractDynamicThreadGroup;
import com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroup;
import com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroupGui;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.timers.VariableThroughputTimer;
import kg.apc.jmeter.timers.VariableThroughputTimerGui;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.sampler.TestAction;
import org.apache.jmeter.sampler.gui.TestActionGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.util.JmeterFunction;
import us.abstracta.jmeter.javadsl.core.util.SingleSeriesTimelinePanel;

/**
 * Configures a thread group which dynamically adapts the number of threads and pauses to match a
 * given rps.
 * <p>
 * <b>Warning:</b> by default the thread group uses unbounded maximum number of threads, but this
 * is not a good practice since it might impose unexpected load on load generator (CPU or memory may
 * run out). It is advisable to always set a maximum number of threads. Check
 * {@link #maxThreads(int)}.
 * <p>
 * Internally this element uses
 * <a href="https://jmeter-plugins.org/wiki/ConcurrencyThreadGroup/">Concurrency Thread Group</a>
 * in combination with <a href="https://jmeter-plugins.org/wiki/ThroughputShapingTimer/">Throughput
 * Shaping Timer</a>.
 * <p>
 * By default, the thread group will control the number of requests per second, but this can be
 * changed to iterations per second with {@link #counting(EventType)}.
 *
 * @since 0.26
 */
public class RpsThreadGroup extends BaseThreadGroup<RpsThreadGroup> {

  private static int timerId = 1;
  protected final List<TimerSchedule> schedules = new ArrayList<>();
  protected double lastRps = 1;
  protected EventType counting = EventType.REQUESTS;
  protected int initThreads = 1;
  protected int maxThreads = Integer.MAX_VALUE;
  protected double spareThreads = 0.1;

  public static class TimerSchedule {

    public final double fromRps;
    public final double toRps;
    public final long durationSecs;

    private TimerSchedule(double fromRps, double toRps, Duration durationSecs) {
      this.fromRps = fromRps;
      this.toRps = toRps;
      this.durationSecs = durationToSeconds(durationSecs);
    }

    public Object[] buildTableRow() {
      return new Object[]{String.valueOf(fromRps), String.valueOf(toRps),
          String.valueOf(durationSecs)};
    }

  }

  public enum EventType {
    REQUESTS("Requests"),
    ITERATIONS("Iterations");

    private final String label;

    EventType(String label) {
      this.label = label;
    }
  }

  public RpsThreadGroup(String name) {
    super(name != null ? name : "RPS Thread Group", ConcurrencyThreadGroupGui.class,
        Collections.emptyList());
  }

  /**
   * Allows ramping up or down RPS with a given duration.
   * <p>
   * JMeter will automatically create or remove threads from thread group and add time pauses to
   * match provided RPS.
   * <p>
   * You can use this method multiple times in a thread group and in conjunction with
   * {@link #holdFor(Duration)} and {@link #rampToAndHold(double, Duration, Duration)} to elaborate
   * complex test plan profiles.
   * <p>
   * Eg:
   * <pre>{@code
   *  rpsThreadGroup()
   *    .maxThreads(10)
   *    .rampTo(10, Duration.ofSeconds(10))
   *    .rampTo(5, Duration.ofSeconds(10))
   *    .rampToAndHold(20, Duration.ofSeconds(5), Duration.ofSeconds(10))
   *    .rampTo(0, Duration.ofSeconds(5))
   *    .children(...)
   * }</pre>
   *
   * @param rps      specifies the final RPS (requests/iterations per second) after the given
   *                 period. This value directly affects how often threads and pauses are adjusted.
   *                 For example, if you configure a ramp from 0.01 to 10 RPS with 10 seconds
   *                 duration, after 1 request it will wait 100 seconds and then reevaluate, not
   *                 honoring configured ramp. A value greater than 1 should at least re adjust
   *                 every second.
   * @param duration duration taken to reach the given RPS and move to the next stage or end the
   *                 test plan. Since JMeter only supports specifying times in seconds, if you
   *                 specify a smaller granularity (like milliseconds) it will be rounded up to
   *                 seconds.
   * @return the thread group for further configuration and usage.
   */
  public RpsThreadGroup rampTo(double rps, Duration duration) {
    if (rps < 0) {
      throw new IllegalArgumentException("RPS must be >=0");
    }
    if (!Duration.ZERO.equals(duration)) {
      schedules.add(new TimerSchedule(lastRps, rps, duration));
    }
    lastRps = rps;
    return this;
  }

  /**
   * Specifies to keep current RPS for a given duration.
   * <p>
   * This method is usually used in combination with {@link #rampTo(double, Duration)} to define the
   * profile of the test plan.
   *
   * @param duration duration to hold the current RPS until moving to next stage or ending the test
   *                 plan. Since JMeter only supports specifying times in seconds, if you specify a
   *                 smaller granularity (like milliseconds) it will be rounded up to seconds.
   * @return the thread group for further configuration and usage.
   * @see #rampTo(double, Duration)
   */
  public RpsThreadGroup holdFor(Duration duration) {
    if (!Duration.ZERO.equals(duration)) {
      schedules.add(new TimerSchedule(lastRps, lastRps, duration));
    }
    return this;
  }

  /**
   * Simply combines {@link #rampTo(double, Duration)} and {@link #holdFor(Duration)} which are
   * usually used in combination.
   *
   * @param rps          target RPS to ramp up/down to. This value directly affects how often
   *                     threads and pauses are adjusted. For example, if you configure a ramp from
   *                     0.01 to 10 RPS with 10 seconds duration, after 1 request it will wait 100
   *                     seconds and then reevaluate, not honoring configured ramp. A value greater
   *                     than 1 should at least re adjust every second.
   * @param rampDuration duration taken to reach the given RPS.
   * @param holdDuration duration to hold the given RPS after the ramp, until moving to next stage
   *                     or ending the test plan.
   * @return the thread group for further configuration and usage.
   * @see #rampTo(double, Duration)
   * @see #holdFor(Duration)
   */
  public RpsThreadGroup rampToAndHold(double rps, Duration rampDuration, Duration holdDuration) {
    return rampTo(rps, rampDuration)
        .holdFor(holdDuration);
  }

  /**
   * Specifies to either control requests or iterations per second.
   * <p>
   * If you are only concerned on controlling the number of requests per second, then there is no
   * need to use this method since this is the default behavior. On the other hand, if you actually
   * want to control how many times per second the flow inside the thread group executes, then you
   * can use this method counting iterations.
   *
   * @param counting specifies what event type to use to control the RPS. When not specified
   *                 requests are counted.
   * @return the thread group for further configuration and usage.
   */
  public RpsThreadGroup counting(EventType counting) {
    this.counting = counting;
    return this;
  }

  /**
   * Specifies the maximum number of threads to use.
   * <p>
   * <b>Warning:</b> this value should be big enough to be able to reach the maximum desired RPS,
   * otherwise the maximum RPS will not be able to be met. If you have requests that have maximum
   * response time (or iteration time, if you are counting iteration instead of requests, see:
   * {@link #counting(EventType)}) R seconds, and need to reach T maximum RPS, then you should set
   * this value to R*T.
   * <p>
   * <b>Warning:</b> by default, maximum threads are unbounded, but this means that you may run out
   * of memory or consume too much CPU. Is a good practice to always set this value to avoid
   * unexpected load on generator that may affect performance test in some undesired ways.
   *
   * @param maxThreads specifies the maximum threads to use by the thread group. By default, is
   *                   unbounded.
   * @return the thread group for further configuration and usage.
   */
  public RpsThreadGroup maxThreads(int maxThreads) {
    this.maxThreads = maxThreads;
    return this;
  }

  /**
   * Specifies the initial number of threads to use.
   * <p>
   * Use this method to start with a bigger pool if you know beforehand that for inital RPS 1 thread
   * would not be enough.
   *
   * @param initThreads specifies the initial number of threads to use by the thread group. By
   *                    default, is 1.
   * @return the thread group for further configuration and usage.
   */
  public RpsThreadGroup initThreads(int initThreads) {
    this.initThreads = initThreads;
    return this;
  }

  /**
   * Specifies the number of spare (not used) threads to keep in the thread group.
   * <p>
   * When thread group identifies that can use less threads, it can still keep them in pool to avoid
   * the cost to re-create them later on if needed. This method controls how many threads to keep.
   *
   * @param spareThreads specifies either the number of spare threads (if the value is greater than
   *                     1) or the percent (if &lt;= 1) from the current active threads count. By
   *                     default, is 0.1 (10% of active threads).
   * @return the thread group for further configuration and usage.
   */
  public RpsThreadGroup spareThreads(double spareThreads) {
    this.spareThreads = spareThreads;
    return this;
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    HashTree ret = parent.add(buildConfiguredTestElement());
    HashTree timerParent = counting == EventType.ITERATIONS ? ret.add(buildTestAction()) : ret;
    timerParent.add(buildTimer());
    children.forEach(c -> context.buildChild(c, ret));
    return ret;
  }

  private TestElement buildTestAction() {
    TestAction ret = new TestAction();
    ret.setAction(TestAction.PAUSE);
    ret.setDuration("0");
    configureTestElement(ret, "Flow Control Action", TestActionGui.class);
    return ret;
  }

  private TestElement buildTimer() {
    VariableThroughputTimer ret = new VariableThroughputTimer();
    ret.setData(buildTimerSchedulesData());
    configureTestElement(ret, buildTimerName(timerId++), VariableThroughputTimerGui.class);
    return ret;
  }

  private String buildTimerName(int id) {
    return "rpsTimer" + id;
  }

  private CollectionProperty buildTimerSchedulesData() {
    PowerTableModel table = new PowerTableModel(
        new String[]{"Start RPS", "End RPS", "Duration, sec"},
        new Class[]{String.class, String.class, String.class});
    schedules.forEach(s -> table.addRow(s.buildTableRow()));
    return JMeterPluginsUtils.tableModelRowsToCollectionProperty(table, "load_profile");
  }

  @Override
  protected AbstractThreadGroup buildThreadGroup() {
    ConcurrencyThreadGroup ret = new ConcurrencyThreadGroup();
    ret.setTargetLevel(
        JmeterFunction.from("__tstFeedback", buildTimerName(timerId), initThreads, maxThreads,
            spareThreads));
    ret.setHold(String.valueOf(schedules.stream().mapToLong(s -> s.durationSecs).sum()));
    ret.setUnit(AbstractDynamicThreadGroup.UNIT_SECONDS);
    return ret;
  }

  public void showTimeline() {
    SingleSeriesTimelinePanel chart = new SingleSeriesTimelinePanel(counting.label + " per second");
    if (!schedules.isEmpty()) {
      chart.add(0, schedules.get(0).fromRps);
      schedules.forEach(s -> chart.add(s.durationSecs * 1000, s.toRps));
    }
    showAndWaitFrameWith(chart, name + " timeline", 800, 300);
  }

}
