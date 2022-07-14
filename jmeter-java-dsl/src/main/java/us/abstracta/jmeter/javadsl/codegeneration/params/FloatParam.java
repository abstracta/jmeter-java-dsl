package us.abstracta.jmeter.javadsl.codegeneration.params;

/**
 * Is a parameter with a float value.
 *
 * @since 0.63
 */
public class FloatParam extends FixedParam<Float> {

  public FloatParam(String expression, Float defaultValue) {
    super(float.class, expression, Float::valueOf, defaultValue);
  }

  @Override
  public String buildCode(String indent) {
    return super.buildCode(indent) + "f";
  }

}
