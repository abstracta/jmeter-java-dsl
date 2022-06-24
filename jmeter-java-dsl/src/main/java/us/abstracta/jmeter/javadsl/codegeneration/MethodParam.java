package us.abstracta.jmeter.javadsl.codegeneration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import us.abstracta.jmeter.javadsl.core.assertions.DslAssertion;
import us.abstracta.jmeter.javadsl.core.configs.DslConfig;
import us.abstracta.jmeter.javadsl.core.configs.DslVariables;
import us.abstracta.jmeter.javadsl.core.controllers.DslController;
import us.abstracta.jmeter.javadsl.core.listeners.DslListener;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslPostProcessor;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslPreProcessor;
import us.abstracta.jmeter.javadsl.core.samplers.DslSampler;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslThreadGroup;
import us.abstracta.jmeter.javadsl.core.timers.DslTimer;

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

  /**
   * Is a parameter associated to a test element name.
   * <p>
   * This type of parameter has the special consideration that when names are set to default values,
   * then they can be ignored.
   */
  public static class NameParam extends StringParam {

    public NameParam(String name, String defaultName) {
      super(name, defaultName);
    }

    @Override
    public boolean isIgnored() {
      return isDefault();
    }

  }

  /**
   * Is a parameter with a string value.
   * <p>
   * This implementation in particular takes care of proper escaping of characters for code
   * generation.
   */
  public static class StringParam extends FixedParam<String> {

    public StringParam(String expression, String defaultValue) {
      super(String.class, expression, v -> v, defaultValue);
    }

    public StringParam(String value) {
      this(value, null);
    }

    @Override
    public String buildCode(String indent) {
      return buildStringLiteral(value);
    }

  }

  public static class DynamicParam extends MethodParam {

    private static final Pattern DYNAMIC_VALUE_PATTERN = Pattern.compile(".*\\$\\{.+}.*");

    protected DynamicParam(String expression) {
      super(String.class, expression);
    }

    public static boolean matches(String propVal) {
      return DYNAMIC_VALUE_PATTERN.matcher(propVal).matches();
    }

  }

  /**
   * This represents parameters which are associated to a fixed value.
   *
   * @param <T> The type of the parameter value.
   */
  public abstract static class FixedParam<T> extends MethodParam {

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

  /**
   * Is a parameter with an integer value.
   */
  public static class IntParam extends FixedParam<Integer> {

    public IntParam(String expression, Integer defaultValue) {
      super(int.class, expression, Integer::valueOf, defaultValue);
    }

    public IntParam(int value) {
      super(int.class, value, null);
    }

  }

  /**
   * Is a parameter with a long value.
   */
  public static class LongParam extends FixedParam<Long> {

    public LongParam(String expression, Long defaultValue) {
      super(long.class, expression, Long::valueOf, defaultValue);
    }

    public LongParam(long value) {
      super(long.class, value, null);
    }

  }

  /**
   * Is a parameter with a double value.
   */
  public static class DoubleParam extends FixedParam<Double> {

    public DoubleParam(String expression, Double defaultValue) {
      super(double.class, expression, Double::valueOf, defaultValue);
    }

  }

  /**
   * Is a parameter with a boolean (true/false) value.
   */
  public static class BoolParam extends FixedParam<Boolean> {

    public BoolParam(String expression, Boolean defaultValue) {
      super(boolean.class,
          expression != null && expression.isEmpty() ? Boolean.FALSE.toString() : expression,
          BoolParam::parseBool, defaultValue);
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

  /**
   * Is a parameter with a Duration value.
   */
  public static class DurationParam extends FixedParam<Duration> {

    public DurationParam(String expression, Duration defaultValue) {
      super(Duration.class, expression, v -> Duration.ofSeconds(Long.parseLong(v)), defaultValue);
    }

    public DurationParam(Duration value) {
      super(Duration.class, value, null);
    }

    @Override
    public String buildCode(String indent) {
      return value.isZero() ? Duration.class.getSimpleName() + ".ZERO"
          : MethodCall.forStaticMethod(Duration.class, "ofSeconds",
              new LongParam(value.getSeconds())).buildCode();
    }

  }

  /**
   * Is a parameter used to specify DSL test element children methods.
   * <p>
   * This is usually used in TestElementContainer instances which usually provide a builder method
   * with basic required parameters and children elements (eg: thread groups &amp; controllers).
   *
   * @param <T> The type of the children DSl test elements.
   */
  public static class ChildrenParam<T> extends MethodParam {

    private static final Class<?>[][] EXECUTION_ORDERS = new Class[][]{
        {DslVariables.class},
        {DslConfig.class},
        {DslPreProcessor.class},
        {DslTimer.class},
        {DslThreadGroup.class, DslController.class, DslSampler.class},
        {DslPostProcessor.class},
        {DslAssertion.class},
        {DslListener.class}
    };

    private final List<MethodCall> children = new ArrayList<>();

    public ChildrenParam(Class<T> childrenClass) {
      super(checkChildrenType(childrenClass), null);
    }

    private static <T> Class<T> checkChildrenType(Class<T> childrenClass) {
      if (!childrenClass.isArray()) {
        throw new RuntimeException("You need always to provide an array class and not the raw "
            + "class for the children. Eg use TestPlanChild[].class");
      }
      return childrenClass;
    }

    @Override
    public String buildCode(String indent) {
      List<MethodCall> childrenCalls = children.stream()
          // order elements to provide the most intuitive representation and ease tests
          .sorted(Comparator.comparing(c -> findExecutionOrder(c.getReturnType())))
          .collect(Collectors.toList());
      String ret = childrenCalls.stream()
          .map(c -> c.buildCode(indent))
          .filter(s -> !s.isEmpty())
          .collect(Collectors.joining(",\n" + indent));
      return ret.isEmpty() ? ret : "\n" + indent + ret + "\n";
    }

    private static int findExecutionOrder(Class<?> returnType) {
      for (int i = 0; i < EXECUTION_ORDERS.length; i++) {
        if (Arrays.stream(EXECUTION_ORDERS[i])
            .anyMatch(c -> c.isAssignableFrom(returnType))) {
          return i;
        }
      }
      return -1;
    }

    public void addChild(MethodCall child) {
      Class<?> childrenType = paramType.getComponentType();
      if (!childrenType.isAssignableFrom(child.getReturnType())) {
        throw new IllegalArgumentException("Trying to add a child of type " + child.getReturnType()
            + " that is not compatible with the declared ones : " + childrenType);
      }
      children.add(child);
    }

  }

}
