package us.abstracta.jmeter.javadsl.codegeneration.params;

import java.util.function.Function;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;

/**
 * Is a parameter with a fixed value.
 *
 * @param <T> The type of the parameter value.
 * @since 0.57
 */
public abstract class FixedParam<T> extends MethodParam {

  protected final T value;
  protected final T defaultValue;

  protected FixedParam(Class<T> paramType, String expression, Function<String, T> parser,
      T defaultValue) {
    super(paramType, expression);
    this.value = this.expression != null ? parser.apply(expression) : null;
    this.defaultValue = defaultValue;
  }

  protected FixedParam(Class<T> paramType, T value, T defaultValue) {
    super(paramType, value == null ? null : value.toString());
    this.value = value;
    this.defaultValue = defaultValue;
  }

  @Override
  public boolean isDefault() {
    return super.isDefault() || defaultValue != null && defaultValue.equals(value);
  }

  /**
   * Gets the value associated to the parameter.
   *
   * @return the value.
   */
  public T getValue() {
    return value;
  }

  @Override
  public String buildCode(String indent) {
    return String.valueOf(value);
  }

}
