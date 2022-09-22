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

  private final TemporalUnit unit;

  public DurationParam(String expression, Duration defaultValue) {
    this(expression, defaultValue, ChronoUnit.SECONDS);
  }

  public DurationParam(Duration value) {
    super(Duration.class, value, null);
    this.unit = ChronoUnit.SECONDS;
  }

  public DurationParam(String expression, Duration defaultValue, TemporalUnit unit) {
    super(Duration.class, expression, v -> Duration.of(Long.parseLong(v), unit), defaultValue);
    this.unit = unit;
  }

  @Override
  public Set<Class<?>> getImports() {
    return Collections.singleton(Duration.class);
  }

  @Override
  public String buildCode(String indent) {
    return value.isZero() ? Duration.class.getSimpleName() + ".ZERO"
        : MethodCall.forStaticMethod(Duration.class, "of" + unit,
                new LongParam(unit == ChronoUnit.MILLIS ? value.toMillis() : value.get(unit)))
            .buildCode();
  }

}
