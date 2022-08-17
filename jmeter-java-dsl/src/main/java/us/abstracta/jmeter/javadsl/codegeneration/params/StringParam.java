package us.abstracta.jmeter.javadsl.codegeneration.params;

/**
 * Is a parameter with a string value.
 * <p>
 * This implementation in particular takes care of proper escaping of characters for code
 * generation.
 *
 * @since 0.45
 */
public class StringParam extends FixedParam<String> {

  public StringParam(String expression, String defaultValue) {
    super(String.class, expression, v -> v, defaultValue);
  }

  public StringParam(String value) {
    this(value, null);
  }

  @Override
  public String buildCode(String indent) {
    return buildStringLiteral(value == null ? "" : value, indent);
  }

}
