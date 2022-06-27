package us.abstracta.jmeter.javadsl.codegeneration.params;

import java.util.regex.Pattern;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;

/**
 * Is a parameter with no fixed value (ie: a variable or jmeter function reference).
 *
 * @since 0.57
 */
public class DynamicParam extends MethodParam {

  private static final Pattern DYNAMIC_VALUE_PATTERN = Pattern.compile(".*\\$\\{.+}.*");

  public DynamicParam(String expression) {
    super(String.class, expression);
  }

  public static boolean matches(String propVal) {
    return DYNAMIC_VALUE_PATTERN.matcher(propVal).matches();
  }

}
