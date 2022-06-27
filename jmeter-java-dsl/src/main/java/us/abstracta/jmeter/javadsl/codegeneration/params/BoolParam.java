package us.abstracta.jmeter.javadsl.codegeneration.params;

/**
 * Is a parameter with a boolean (true/false) value.
 *
 * @since 0.45
 */
public class BoolParam extends FixedParam<Boolean> {

  public BoolParam(String expression, Boolean defaultValue) {
    super(boolean.class,
        expression != null && expression.isEmpty() ? Boolean.FALSE.toString() : expression,
        us.abstracta.jmeter.javadsl.codegeneration.params.BoolParam::parseBool, defaultValue);
  }

  public BoolParam(Boolean value, Boolean defaultValue) {
    super(boolean.class, value, defaultValue);
  }

  private static Boolean parseBool(String value) {
    if (!String.valueOf(true).equals(value) && !String.valueOf(false).equals(value)) {
      throw new IllegalArgumentException();
    }
    return Boolean.valueOf(value);
  }

}
