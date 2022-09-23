package us.abstracta.jmeter.javadsl.codegeneration.params;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collections;
import java.util.Set;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;

/**
 * Is a parameter with a Duration value.
 *
 * @since 0.45
 */
public class DurationParam extends FixedParam<Duration> {

  public DurationParam(String expression, Duration defaultValue) {
    this(expression, defaultValue, ChronoUnit.SECONDS);
  }

  public DurationParam(Duration value) {
    super(Duration.class, value, null);
  }

  public DurationParam(String expression, Duration defaultValue, TemporalUnit unit) {
    super(Duration.class, expression, v -> Duration.of(Long.parseLong(v), unit), defaultValue);
  }

  @Override
  public Set<String> getImports() {
    return Collections.singleton(Duration.class.getName());
  }

  @Override
  public String buildCode(String indent) {
    if (value.isZero()) {
      return Duration.class.getSimpleName() + ".ZERO";
    }
    TemporalUnit outputUnit;
    long outputValue;
    if (value.getNano() != 0) {
      outputUnit = ChronoUnit.MILLIS;
      outputValue = value.toMillis();
    } else if (value.toMinutes() * 60 != value.getSeconds()) {
      outputUnit = ChronoUnit.SECONDS;
      outputValue = value.getSeconds();
    } else if (value.toHours() * 60 != value.toMinutes()) {
      outputUnit = ChronoUnit.MINUTES;
      outputValue = value.toMinutes();
    } else if (value.toDays() * 24 != value.toHours()) {
      outputUnit = ChronoUnit.HOURS;
      outputValue = value.toHours();
    } else {
      outputUnit = ChronoUnit.DAYS;
      outputValue = value.toDays();
    }
    return MethodCall.forStaticMethod(Duration.class, "of" + outputUnit, new LongParam(outputValue))
        .buildCode();
  }

}
