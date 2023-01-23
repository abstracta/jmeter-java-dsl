package us.abstracta.jmeter.javadsl.codegeneration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates code for a MethodCall parameter.
 * <p>
 * This class should be extended to implement custom types of parameters (eg: check
 * {@link us.abstracta.jmeter.javadsl.http.ContentTypeParam}
 *
 * @since 0.45
 */
/*
This class should extend CodeSegment, but we haven't changed it yet since such change would break
compatibility for users extending from MethodParam. They would be required to change buildCode
method visibility from protected to public.
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
   * @return the set of required classes names that need to be imported by generated code.
   */
  public Set<String> getStaticImports() {
    return Collections.emptySet();
  }

  /**
   * Gets all classes that are required to be statically imported by generated code.
   * <p>
   * Override this method if you implement a custom MethodParam that requires some particular static
   * import.
   *
   * @return the set of required classes names that need to be statically imported by generated
   * code.
   */
  public Set<String> getImports() {
    return Collections.emptySet();
  }

  public Map<String, MethodCall> getMethodDefinitions() {
    return Collections.emptyMap();
  }

  protected String buildCode(String indent) {
    return buildStringLiteral(getExpression(), indent);
  }

  protected static String buildStringLiteral(String value, String indent) {
    return "\"" + value.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\t", "\\t")
        .replace("\r", "\\r")
        .replace("\n", "\\n" + (!indent.isEmpty() ? "\"\n" + indent + "+ \"" : ""))
        .replaceAll("\n" + indent + "\\+ \"\"$", "")
        + "\"";
  }

  protected static <T> Map<T, String> findConstantNamesMap(Class<?> constantsHolderClass,
      Class<T> constantClass, Predicate<Field> filter) {
    return findConstantNamesFields(constantsHolderClass, constantClass, filter)
        .collect(Collectors.toMap(f -> {
          try {
            return constantClass.cast(f.get(null));
          } catch (IllegalAccessException e) {
            // this should never happen since we are iterating over public static fields
            throw new RuntimeException(e);
          }
        }, Field::getName));
  }

  private static Stream<Field> findConstantNamesFields(Class<?> constantsHolderClass,
      Class<?> constantClass, Predicate<Field> filter) {
    return Arrays.stream(constantsHolderClass.getDeclaredFields())
        .filter(f -> Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
            && Modifier.isFinal(f.getModifiers()) && f.getType() == constantClass
            && filter.test(f));
  }

  protected static Set<String> findConstantNames(Class<?> constantsHolderClass,
      Class<?> constantClass, Predicate<Field> filter) {
    return findConstantNamesFields(constantsHolderClass, constantClass, filter)
        .map(Field::getName)
        .collect(Collectors.toSet());
  }

}
