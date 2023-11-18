package us.abstracta.jmeter.javadsl.codegeneration.params;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collections;
import java.util.Set;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.params.timeconverter.*;

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

    TimeConverter timeConverter;
    if (value.getNano() != 0) {
      timeConverter = new MillisConverter();
    } else if (value.toMinutes() * 60 != value.getSeconds()) {
      timeConverter = new SecondsConverter();
    } else if (value.toHours() * 60 != value.toMinutes()) {
      timeConverter = new MinutesConverter();
    } else if (value.toDays() * 24 != value.toHours()) {
      timeConverter = new HoursConverter();
    } else {
      timeConverter = new DaysConverter();
    }
    ChronoUnit outputUnit = timeConverter.getOutputUnit();
    long outputValue = timeConverter.convert(value);
    return MethodCall.forStaticMethod(Duration.class, "of" + outputUnit, new LongParam(outputValue))
        .buildCode();
  }

}
