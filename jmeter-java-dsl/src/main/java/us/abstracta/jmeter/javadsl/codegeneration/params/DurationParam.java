package us.abstracta.jmeter.javadsl.codegeneration.params;

import java.time.Duration;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;

/**
 * Is a parameter with a Duration value.
 *
 * @since 0.45
 */
public class DurationParam extends FixedParam<Duration> {

  public DurationParam(String expression, Duration defaultValue) {
    super(Duration.class, expression, v -> Duration.ofSeconds(Long.parseLong(v)), defaultValue);
  }

  public DurationParam(Duration value) {
    super(Duration.class, value, null);
  }

  @Override
  public String buildCode(String indent) {
    return value.isZero() ? Duration.class.getSimpleName() + ".ZERO"
        : MethodCall.forStaticMethod(Duration.class, "ofSeconds",
            new LongParam(value.getSeconds())).buildCode();
  }

}
