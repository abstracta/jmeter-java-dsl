package us.abstracta.jmeter.javadsl.core.threadgroups.defaultthreadgroup;

import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Represents a stage in thread profiling (ramp up or down, hold duration or iterations).
 */
public class Stage {

  private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");

  private final Object threadCount;
  private final Object duration;
  private final Object iterations;

  public Stage(Object threadCount, Object duration, Object iterations) {
    // parsing simplifies calculations and allow for further optimizations
    this.threadCount = tryParseInt(threadCount);
    this.duration = tryParseDuration(duration);
    this.iterations = tryParseInt(iterations);
  }

  public Object threadCount() {
    return threadCount;
  }

  public Object duration() {
    return duration;
  }

  public Object iterations() {
    return iterations;
  }

  private Object tryParseInt(Object val) {
    return (val instanceof String && INT_PATTERN.matcher((String) val).matches())
        ? Integer.valueOf((String) val)
        : val;
  }

  private Object tryParseDuration(Object val) {
    Object ret = tryParseInt(val);
    return ret instanceof Integer ? Duration.ofSeconds((Integer) ret) : ret;
  }

  public boolean isFixedStage() {
    return threadCount instanceof Integer
        && (duration == null || duration instanceof Duration)
        && (iterations == null || iterations instanceof Integer);
  }

}
