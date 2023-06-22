package us.abstracta.jmeter.javadsl.core.threadgroups.defaultthreadgroup;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.DurationParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.IntParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslDefaultThreadGroup;
import us.abstracta.jmeter.javadsl.core.util.JmeterFunction;

public class SimpleThreadGroupHelper extends BaseThreadGroup<DslDefaultThreadGroup> {

  private static final Integer ZERO = 0;
  private final List<Stage> stages;

  public SimpleThreadGroupHelper(List<Stage> stages) {
    super(null, ThreadGroupGui.class, Collections.emptyList());
    this.stages = stages;
  }

  @Override
  public AbstractThreadGroup buildThreadGroup() {
    Object threads = 1;
    Object iterations = 1;
    Object rampUpPeriod = null;
    Object duration = null;
    Object delay = null;
    if (!stages.isEmpty()) {
      Stage firstStage = stages.get(0);
      if (ZERO.equals(firstStage.threadCount())) {
        delay = firstStage.duration();
      } else {
        rampUpPeriod = firstStage.duration();
        threads = firstStage.threadCount();
      }
      iterations = firstStage.iterations();
      if (stages.size() > 1) {
        Stage secondStage = stages.get(1);
        threads = secondStage.threadCount();
        iterations = secondStage.iterations();
        if (ZERO.equals(firstStage.threadCount())) {
          rampUpPeriod = secondStage.duration();
          if (stages.size() > 2) {
            Stage lastStage = stages.get(2);
            duration = lastStage.duration();
            iterations = lastStage.iterations();
          }
        } else {
          duration = secondStage.duration();
        }
      }
    }
    if (rampUpPeriod != null && !Duration.ZERO.equals(rampUpPeriod) &&
        (iterations == null || duration != null)) {
      duration = duration != null ? sumDurations(duration, rampUpPeriod) : rampUpPeriod;
    }
    return buildSimpleThreadGroupFrom(threads, iterations, rampUpPeriod, duration, delay);
  }

  private Object sumDurations(Object duration, Object rampUpPeriod) {
    if (duration instanceof Duration && rampUpPeriod instanceof Duration) {
      return ((Duration) duration).plus((Duration) rampUpPeriod);
    } else {
      if (duration instanceof Duration) {
        duration = String.valueOf(durationToSeconds((Duration) duration));
      } else if (rampUpPeriod instanceof Duration) {
        rampUpPeriod = String.valueOf(durationToSeconds((Duration) rampUpPeriod));
      }
      return JmeterFunction.groovy(buildGroovySolvingIntExpression((String) duration) + " + "
          + buildGroovySolvingIntExpression((String) rampUpPeriod));
    }
  }

  private static String buildGroovySolvingIntExpression(String expr) {
    /*
     * Replacing $ with # (or alternative, depending on level of nesting of groovy expression)
     * to avoid Jmeter to interpret this property, and delegate evaluation to CompoundVariable for
     * proper calculation.
     */
    StringBuilder altPlaceHolder = new StringBuilder("#");
    while (expr.contains(altPlaceHolder + "{")) {
      altPlaceHolder.append("#");
    }
    return "(new org.apache.jmeter.engine.util.CompoundVariable('"
        + expr.replace("${", altPlaceHolder + "{")
        // Escape chars that are unescaped by groovy script
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        + "'.replace('" + altPlaceHolder + "','$')).execute() as int)";
  }

  private ThreadGroup buildSimpleThreadGroupFrom(Object threads, Object iterations,
      Object rampUpPeriod, Object duration, Object delay) {
    ThreadGroup ret = new ThreadGroup();
    setIntProperty(ret, ThreadGroup.NUM_THREADS, threads);
    setIntProperty(ret, ThreadGroup.RAMP_TIME, rampUpPeriod == null ? Duration.ZERO : rampUpPeriod);
    LoopController loopController = new LoopController();
    ret.setSamplerController(loopController);
    if (duration != null) {
      loopController.setLoops(-1);
      setLongProperty(ret, ThreadGroup.DURATION, duration);
    } else {
      setIntProperty(loopController, LoopController.LOOPS, iterations);
    }
    if (delay != null) {
      setLongProperty(ret, ThreadGroup.DELAY, delay);
    }
    if (duration != null || delay != null) {
      ret.setScheduler(true);
    }
    ret.setIsSameUserOnNextIteration(false);
    return ret;
  }

  private void setIntProperty(TestElement ret, String propName, Object value) {
    if (value instanceof Duration) {
      ret.setProperty(propName, (int) durationToSeconds((Duration) value));
    } else if (value instanceof Integer) {
      ret.setProperty(propName, (Integer) value);
    } else {
      ret.setProperty(propName, (String) value);
    }
  }

  private void setLongProperty(TestElement ret, String propName, Object value) {
    if (value instanceof Duration) {
      ret.setProperty(propName, durationToSeconds((Duration) value));
    } else {
      ret.setProperty(propName, (String) value);
    }
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
      MethodParam name = testElement.nameParam("Thread Group");
      MethodParam threads = testElement.intParam(ThreadGroup.NUM_THREADS);
      MethodParam rampTime = testElement.durationParam(ThreadGroup.RAMP_TIME,
          Duration.ofSeconds(1));
      MethodParam duration = testElement.durationParam(ThreadGroup.DURATION);
      MethodParam delay = testElement.durationParam(ThreadGroup.DELAY);
      MethodParam iterations = testElement.intParam(
          ThreadGroup.MAIN_CONTROLLER + "/" + LoopController.LOOPS);
      if (threads instanceof IntParam && duration instanceof DurationParam
          && iterations instanceof IntParam && isDefaultOrZeroDuration(rampTime)
          && isDefaultOrZeroDuration(delay)) {
        return buildMethodCall(name, threads,
            isDefaultOrZeroDuration(duration) ? iterations : duration,
            new ChildrenParam<>(ThreadGroupChild[].class));
      } else {
        MethodCall ret = buildMethodCall(name);
        if (!delay.isDefault()) {
          ret.chain("holdFor", delay);
        }
        if (!isDefaultOrZeroDuration(duration)) {
          duration = buildDurationParam(duration, rampTime, ret);
          if (!(threads instanceof IntParam) || !(rampTime instanceof DurationParam)
              || !(duration instanceof DurationParam)) {
            threads = new StringParam(threads.getExpression());
            rampTime = new StringParam(rampTime.getExpression());
            duration = new StringParam(duration.getExpression());
          }
          ret.chain("rampToAndHold", threads, rampTime, duration);
        } else {
          if (!(threads instanceof IntParam) || !(rampTime instanceof DurationParam)) {
            threads = new StringParam(threads.getExpression());
            rampTime = new StringParam(rampTime.getExpression());
          }
          ret.chain("rampTo", threads, rampTime)
              .chain("holdIterating", iterations);
        }
        return ret;
      }
    }

    private boolean isDefaultOrZeroDuration(MethodParam duration) {
      return duration.isDefault()
          || duration instanceof DurationParam && ((DurationParam) duration).getValue().isZero();
    }

    private MethodParam buildDurationParam(MethodParam duration, MethodParam rampTime,
        MethodCall ret) {
      if (duration instanceof DurationParam && rampTime instanceof DurationParam) {
        return new DurationParam(rampTime.isDefault() ? ((DurationParam) duration).getValue()
            : ((DurationParam) duration).getValue().minus(((DurationParam) rampTime).getValue()));
      } else {
        if (!isDefaultOrZeroDuration(rampTime)) {
          ret.chainComment("To keep generated DSL simple, the original duration is used as hold "
              + "for time. But, you should use as hold for time the original duration - ramp up "
              + "period.");
        }
        return duration;
      }
    }

  }

}
