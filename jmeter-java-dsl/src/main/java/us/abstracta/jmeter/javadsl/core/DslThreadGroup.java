package us.abstracta.jmeter.javadsl.core;

import java.awt.Color;
import java.awt.Component;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import javax.swing.BorderFactory;
import kg.apc.charting.AbstractGraphRow;
import kg.apc.charting.DateTimeRenderer;
import kg.apc.charting.GraphPanelChart;
import kg.apc.charting.rows.GraphRowSimple;
import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.threads.UltimateThreadGroup;
import kg.apc.jmeter.threads.UltimateThreadGroupGui;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;

/**
 * Represents the standard thread group test element included by JMeter.
 * <p>
 * Additional methods should be added in the future to support setting rump-up, start and end
 * scheduling.
 *
 * @since 0.1
 */
public class DslThreadGroup extends TestElementContainer<ThreadGroupChild> implements
    TestPlanChild {

  private final List<Stage> stages = new ArrayList<>();

  // represents a stage in thread profiling (ramp up or down, hold duration or iterations).
  private static class Stage {

    private final int threadCount;
    private Duration duration;
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

  public DslThreadGroup(String name, int threads, int iterations, List<ThreadGroupChild> children) {
    super(solveName(name), null, children);
    checkThreadCount(threads);
    if (iterations <= 0) {
      throw new IllegalArgumentException("Iterations must be >=1");
    }
    stages.add(new Stage(threads, Duration.ZERO));
    stages.add(new Stage(threads, iterations));
  }

  public DslThreadGroup(String name, int threads, Duration duration,
      List<ThreadGroupChild> children) {
    super(solveName(name), null, children);
    checkThreadCount(threads);
    stages.add(new Stage(threads, Duration.ZERO));
    stages.add(new Stage(threads, duration));
  }

  public DslThreadGroup(String name) {
    super(solveName(name), null, Collections.emptyList());
  }

  private void checkThreadCount(int threads) {
    if (threads <= 0) {
      throw new IllegalArgumentException("Threads count must be >=1");
    }
  }

  private static String solveName(String name) {
    return name != null ? name : "Thread Group";
  }

  /**
   * Specifies the time taken to create the initial number of threads.
   *
   * @param rampUpPeriod the period to use as ramp up. Since JMeter supports specifying ramp up in
   * seconds, if you specify a smaller granularity (like milliseconds) it will be rounded up to
   * seconds.
   * @return the DslThreadGroup instance to use fluent API to set additional options.
   * @deprecated as of 0.18, use {@link #rampTo(int, Duration)} instead.
   */
  @Deprecated
  public DslThreadGroup rampUpPeriod(Duration rampUpPeriod) {
    if (stages.isEmpty()) {
      throw new IllegalStateException(
          "To use rampUpPeriod you need to specify threads in threadGroup");
    }
    stages.get(0).duration = rampUpPeriod;
    return this;
  }

  /**
   * Allows ramping up or down threads with a given duration.
   *
   * It is usually advised to use this method when working with considerable amount of threads to
   * avoid load of creating all the threads at once to affect test results.
   *
   * JMeter will create (or remove) a thread every {@code rampUp.seconds * 1000 / threadCount}
   * milliseconds.
   *
   * If you specify a thread duration time (instead of iterations), take into consideration that
   * ramp up is not considered as part of thread duration time. For example: if you have a thread
   * group duration of 10 seconds, and a ramp-up of 10 seconds, the last threads (and the test plan
   * run) will run at least (duration may vary depending on test plan contents) after 20 seconds of
   * starting the test.
   *
   * You can use this method multiple times in a thread group and in conjunction with {@link
   * #holdFor(Duration)} and {@link #rampToAndHold(int, Duration, Duration)} to elaborate complex
   * test plan profiles.
   *
   * Eg:
   * <pre>{@code
   *  threadGroup()
   *    .rampTo(10, Duration.seconds(10))
   *    .rampDown(5, Duration.seconds(10))
   *    .rampToAndHold(20, Duration.seconds(5), Duration.seconds(10))
   *    .rampTo(0, Duration.seconds(5))
   *    .children(...)
   * }</pre>
   *
   * @param threadCount specifies the final number of threads after the given period.
   * @param duration duration taken to reach the given threadCount and move to the next stage or end
   * the test plan. Since JMeter only supports specifying times in seconds, if you specify a smaller
   * granularity (like milliseconds) it will be rounded up to seconds.
   * @return the DslThreadGroup instance to use fluent API to set additional options.
   * @throws IllegalStateException if used after an iterations stage, since JMeter does not provide
   * built-in thread group to support such scenario.
   * @since 0.18
   */
  public DslThreadGroup rampTo(int threadCount, Duration duration) {
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
   *
   * This method is usually used in combination with {@link #rampTo(int, Duration)} to define the
   * profile of the test plan.
   *
   * @param duration duration to hold the current number of threads until moving to next stage or
   * ending the test plan. Since JMeter only supports specifying times in seconds, if you specify a
   * smaller granularity (like milliseconds) it will be rounded up to seconds.
   * @return the DslThreadGroup instance to use fluent API to set additional options.
   * @see #rampTo(int, Duration)
   * @since 0.18
   */
  public DslThreadGroup holdFor(Duration duration) {
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
   * thread group.
   * @since 0.18
   */
  public DslThreadGroup holdIterating(int iterations) {
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
   * @param threads number of threads to ramp threads up/down to.
   * @param rampDuration duration taken to reach the given threadCount to start holding that number
   * of threads.
   * @param holdDuration duration to hold the given number of threads, after the ramp, until moving
   * to next stage or ending the test plan.
   * @return the DslThreadGroup instance to use fluent API to set additional options.
   * @see #rampTo(int, Duration)
   * @see #holdFor(Duration)
   * @since 0.18
   */
  public DslThreadGroup rampToAndHold(int threads, Duration rampDuration, Duration holdDuration) {
    return rampTo(threads, rampDuration)
        .holdFor(holdDuration);
  }

  /**
   * Allows specifying children the thread group (samplers, listeners, post processors, etc.).
   *
   * This method is just an alternative to the constructor specification of children, and is handy
   * when you want to keep general thread group settings together and then specify children (instead
   * of specifying threadCount &amp; duration/iterations, then children, and at the end alternative
   * settings like ramp-up period).
   *
   * @param children list of test elements to add as children of the thread group.
   * @return the altered thread group to allow for fluent API usage.
   * @since 0.12
   */
  public DslThreadGroup children(ThreadGroupChild... children) {
    return (DslThreadGroup) super.children(children);
  }

  @Override
  public TestElement buildTestElement() {
    return isSimpleThreadGroup() ? buildSimpleThreadGroup() : buildUltimateThreadGroup();
  }

  private boolean isSimpleThreadGroup() {
    return stages.size() <= 1
        || stages.size() == 2 && (stages.get(0).threadCount == 0
        || stages.get(0).threadCount == stages.get(1).threadCount)
        || stages.size() == 3 && (stages.get(0).threadCount == 0
        && stages.get(1).threadCount == stages.get(2).threadCount);
  }

  private TestElement buildSimpleThreadGroup() {
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
    guiClass = ThreadGroupGui.class;
    return buildThreadGroup(threads, iterations, rampUpPeriod, duration, delay);
  }

  private ThreadGroup buildThreadGroup(int threads, int iterations, Duration rampUpPeriod,
      Duration duration, Duration delay) {
    ThreadGroup ret = new ThreadGroup();
    ret.setNumThreads(Math.max(threads, 1));
    ret.setRampUp(
        (int) extractDurationSeconds(rampUpPeriod == null ? Duration.ZERO : rampUpPeriod));
    LoopController loopController = new LoopController();
    ret.setSamplerController(loopController);
    if (duration != null) {
      loopController.setLoops(-1);
      ret.setDuration(extractDurationSeconds(duration));
    } else {
      loopController.setLoops(iterations);
    }
    if (delay != null) {
      ret.setDelay(extractDurationSeconds(delay));
    }
    if (duration != null || delay != null) {
      ret.setScheduler(true);
    }
    return ret;
  }

  private static long extractDurationSeconds(Duration duration) {
    return Math.round(Math.ceil((double) duration.toMillis() / 1000));
  }

  private TestElement buildUltimateThreadGroup() {
    guiClass = UltimateThreadGroupGui.class;
    UltimateThreadGroup ret = new UltimateThreadGroup();
    PowerTableModel table = new PowerTableModel(UltimateThreadGroupGui.columnIdentifiers,
        UltimateThreadGroupGui.columnClasses);
    buildUltimateThreadGroupSchedules().forEach(s -> table.addRow(s.buildTableRow()));
    ret.setData(JMeterPluginsUtils.tableModelRowsToCollectionProperty(table,
        UltimateThreadGroup.DATA_PROPERTY));
    LoopController loopController = new LoopController();
    loopController.setLoops(-1);
    loopController.setContinueForever(true);
    ret.setSamplerController(loopController);
    return ret;
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
    if (!stack.isEmpty()) {
      ret.add(curr);
      while (!stack.isEmpty()) {
        ret.add(stack.pop());
      }
      /*
       last added segment is the first segment which should be ignored since is covered by next
       (considering timeline order) segment delay.
       */
      ret.remove(ret.size() - 1);
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

    public Object[] buildTableRow() {
      return new Object[]{String.valueOf(threadCount),
          String.valueOf(extractDurationSeconds(delay)),
          String.valueOf(extractDurationSeconds(startup)),
          String.valueOf(extractDurationSeconds(hold)),
          String.valueOf(extractDurationSeconds(shutdown))};
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
   * The graph will be displayed in a popup window.
   *
   * This method is provided mainly to ease test plan designing when working with complex thread
   * group profiles (several stages with ramps and holds).
   *
   * @since 0.18
   */
  public void showThreadsTimeline() {
    showFrameWith(buildGraphPanel(), name + " threads timeline", 800, 300, null);
  }

  private Component buildGraphPanel() {
    GraphPanelChart ret = new GraphPanelChart(false, true);
    ret.getChartSettings().setDrawFinalZeroingLines(true);
    ret.setxAxisLabel("Time");
    ret.setYAxisLabel("Threads");
    ret.setxAxisLabelRenderer(new DateTimeRenderer(DateTimeRenderer.HHMMSS, 0));
    ret.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    ret.setForcedMinX(0);
    HashMap<String, AbstractGraphRow> model = new HashMap<>();
    model.put("Threads", buildThreadsSeries());
    ret.setRows(model);
    return ret;
  }

  private GraphRowSimple buildThreadsSeries() {
    GraphRowSimple ret = new GraphRowSimple();
    ret.setColor(Color.RED);
    ret.setDrawLine(true);
    ret.setMarkerSize(AbstractGraphRow.MARKER_SIZE_NONE);
    ret.add(0, 0);
    long delayMillis = 0;
    for (Stage s : stages) {
      delayMillis += s.duration.toMillis();
      ret.add(delayMillis, s.threadCount);
    }
    return ret;
  }

  /**
   * Test elements that can be added as direct children of a thread group in jmeter should implement
   * this interface.
   */
  public interface ThreadGroupChild extends DslTestElement {

  }

}
