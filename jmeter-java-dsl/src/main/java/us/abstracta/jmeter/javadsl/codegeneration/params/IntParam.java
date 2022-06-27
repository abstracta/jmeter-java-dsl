package us.abstracta.jmeter.javadsl.codegeneration.params;

/**
 * Is a parameter with an integer value.
 *
 * @since 0.45
 */
public class IntParam extends FixedParam<Integer> {

  public IntParam(String expression, Integer defaultValue) {
    super(int.class, expression, Integer::valueOf, defaultValue);
  }

  public IntParam(int value) {
    super(int.class, value, null);
  }

}
