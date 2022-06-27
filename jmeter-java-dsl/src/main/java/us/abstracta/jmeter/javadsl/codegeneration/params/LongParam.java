package us.abstracta.jmeter.javadsl.codegeneration.params;

/**
 * Is a parameter with a long value.
 *
 * @since 0.45
 */
public class LongParam extends FixedParam<Long> {

  public LongParam(String expression, Long defaultValue) {
    super(long.class, expression, Long::valueOf, defaultValue);
  }

  public LongParam(long value) {
    super(long.class, value, null);
  }

}
