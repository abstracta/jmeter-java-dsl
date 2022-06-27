package us.abstracta.jmeter.javadsl.codegeneration.params;

/**
 * Is a parameter with a double value.
 *
 * @since 0.61
 */
public class DoubleParam extends FixedParam<Double> {

  public DoubleParam(String expression, Double defaultValue) {
    super(double.class, expression, Double::valueOf, defaultValue);
  }

}
