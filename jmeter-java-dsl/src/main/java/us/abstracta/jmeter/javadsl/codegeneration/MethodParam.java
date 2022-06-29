package us.abstracta.jmeter.javadsl.codegeneration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generates code for a MethodCall parameter.
 * <p>
 * This class should be extended to implement custom types of parameters (eg: check
 * {@link us.abstracta.jmeter.javadsl.http.ContentTypeParam}
 *
 * @since 0.45
 */
public abstract class MethodParam {

  protected final Class<?> paramType;
  protected final String expression;

  protected MethodParam(Class<?> paramType, String expression) {
    this.paramType = paramType;
    this.expression = expression != null && expression.isEmpty() ? null : expression;
  }

  public String getExpression() {
    return expression == null ? "" : expression;
  }

  protected Class<?> getType() {
    return paramType;
  }

  /**
   * Allows checking if a parameter is set to the default value.
   * <p>
   * This is usually used in {@link MethodCallBuilder} instances to check if a parameter is set or
   * not to some custom value, and some method chaingin is required or not.
   * <p>
   * This method may, and is, overwritten by subclasses depending on the semantics of each type of
   * parameter.
   *
   * @return true when the value is the default one or not specified (null), false otherwise.
   */
  public boolean isDefault() {
    return expression == null;
  }

  protected boolean isIgnored() {
    return false;
  }

  /**
   * Gets all classes that are required to be imported by generated code.
   * <p>
   * Override this method if you implement a custom MethodParam that requires some particular
   * import.
   *
   * @return the set of required classes that need to be imported by generated code.
   */
  public Set<Class<?>> getStaticImports() {
    return Collections.emptySet();
  }

  /**
   * Gets all classes that are required to be statically imported by generated code.
   * <p>
   * Override this method if you implement a custom MethodParam that requires some particular static
   * import.
   *
   * @return the set of required classes that need to be statically imported by generated code.
   */
  public Set<Class<?>> getImports() {
    return Collections.emptySet();
  }

  protected String buildCode(String indent) {
    return buildStringLiteral(getExpression());
  }

  protected static String buildStringLiteral(String value) {
    return "\"" + value.replaceAll("[\\\\\"\n\t\r]", "\\\\$0") + "\"";
  }

  protected static <T> Map<T, String> findConstantNames(Class<?> constantsHolderClass,
      Class<T> constantClass, Predicate<Field> filter) {
    return Arrays.stream(constantsHolderClass.getDeclaredFields())
        .filter(f -> Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
            && Modifier.isFinal(f.getModifiers()) && f.getType() == constantClass
            && filter.test(f))
        .collect(Collectors.toMap(f -> {
          try {
            return constantClass.cast(f.get(null));
          } catch (IllegalAccessException e) {
            // this should never happen since we are iterating over public static fields
            throw new RuntimeException(e);
          }
        }, Field::getName));
  }

}
