package us.abstracta.jmeter.javadsl.core.threadgroups.defaultthreadgroup;

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
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.AbstractThreadGroup;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.DurationParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.IntParam;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslDefaultThreadGroup;

public class UltimateThreadGroupHelper extends BaseThreadGroup<DslDefaultThreadGroup> {

  private final List<Stage> stages;

  public UltimateThreadGroupHelper(List<Stage> stages) {
    super(null, UltimateThreadGroupGui.class, Collections.emptyList());
    this.stages = stages;
  }

  @Override
  public AbstractThreadGroup buildThreadGroup() {
    UltimateThreadGroup ret = new UltimateThreadGroup();
    PowerTableModel table = buildTableModel();
    buildUltimateThreadGroupSchedules(stages).forEach(s -> table.addRow(s.buildTableRow()));
    ret.setData(JMeterPluginsUtils.tableModelRowsToCollectionProperty(table,
        UltimateThreadGroup.DATA_PROPERTY));
    LoopController loopController = new LoopController();
    loopController.setLoops(-1);
    loopController.setContinueForever(true);
    ret.setSamplerController(loopController);
    return ret;
  }

  private static PowerTableModel buildTableModel() {
    return new PowerTableModel(UltimateThreadGroupGui.columnIdentifiers,
        UltimateThreadGroupGui.columnClasses);
  }

  private List<UltimateThreadSchedule> buildUltimateThreadGroupSchedules(List<Stage> stages) {
    List<UltimateThreadSchedule> ret = new ArrayList<>();
    Duration delay = Duration.ZERO;
    int threads = 0;
    Stack<UltimateThreadSchedule> stack = new Stack<>();
    UltimateThreadSchedule curr = new UltimateThreadSchedule(0, Duration.ZERO, Duration.ZERO,
        Duration.ZERO, Duration.ZERO);
    for (Stage s : stages) {
      int stageThreads = (int) s.threadCount();
      Duration stageDuration = (Duration) s.duration();
      if (stageThreads == threads) {
        curr.hold = curr.hold.plus(stageDuration);
      } else if (stageThreads > threads) {
        stack.add(curr);
        curr = new UltimateThreadSchedule(stageThreads - threads, delay, stageDuration,
            Duration.ZERO, Duration.ZERO);
      } else {
        int diff = threads - stageThreads;
        Duration shutdown = stageDuration;
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
      threads = stageThreads;
      delay = delay.plus(stageDuration);
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

  public static class CodeBuilder extends MethodCallBuilder {

    public CodeBuilder(List<Method> builderMethods) {
      super(builderMethods);
    }

    @Override
    public boolean matches(MethodCallContext context) {
      return false;
    }

    @Override
    public MethodCall buildMethodCall(MethodCallContext context) {
      TestElementParamBuilder testElement = new TestElementParamBuilder(context.getTestElement());
      MethodParam name = testElement.nameParam("jp@gc - Ultimate Thread Group");
      MethodCall ret = buildMethodCall(name);
      return ThreadsTimeline.fromSchedules(schedulesProp(testElement))
          .addMethodCallsTo(ret);
    }

    private List<UltimateThreadSchedule> schedulesProp(TestElementParamBuilder testElement) {
      JMeterProperty schedulesProp = testElement.prop(UltimateThreadGroup.DATA_PROPERTY);
      PowerTableModel tableModel = buildTableModel();
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
      Iterator<Entry<Duration, Integer>> thisIter = this.points.entrySet()
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
      Iterator<Map.Entry<Duration, Integer>> pendingIter =
          thisPoint != null ? thisIter : otherIter;
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

}
