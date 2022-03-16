package us.abstracta.jmeter.javadsl.codegeneration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
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
 * This class should be extended to implement custom types of parameters (eg: check {@link
 * us.abstracta.jmeter.javadsl.http.ContentTypeParam}
 *
 * @param <T> Is the type of the parameter value.
 * @since 0.45
 */
public abstract class MethodParam<T> {

  protected final T value;
  protected final T defaultValue;
  protected final Class<T> paramType;

  protected MethodParam(Class<T> paramType, T value, T defaultValue) {
    this.paramType = paramType;
    this.value = value;
    this.defaultValue = defaultValue;
  }

  protected Class<?> getType() {
    return this.paramType;
  }

  /**
   * Gets the value associated to the parameter.
   *
   * @return the value.
   */
  public T getValue() {
    return value;
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
    return defaultValue != null && defaultValue.equals(value) || value == null;
  }

  protected boolean isIgnored() {
    return false;
  }

  protected abstract String buildCode(String indent);

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
  public static class StringParam extends MethodParam<String> {

    public StringParam(String value, String defaultValue) {
      super(String.class, value, defaultValue);
    }

    public StringParam(String value) {
      this(value, null);
    }

    @Override
    public boolean isDefault() {
      return super.isDefault() || value.isEmpty();
    }

    @Override
    public String buildCode(String indent) {
      return "\"" + value.replaceAll("[\\\\\"\n\t\r]", "\\\\$0") + "\"";
    }

  }

  /**
   * This represents parameters for which generated code does not need any sort of transformation on
   * the assigned value.
   *
   * @param <T> The type of the parameter value.
   */
  public abstract static class LiteralParam<T> extends MethodParam<T> {

    protected LiteralParam(Class<T> paramType, T value, T defaultValue) {
      super(paramType, value, defaultValue);
    }

    @Override
    public String buildCode(String indent) {
      return String.valueOf(value);
    }

  }

  /**
   * Is a parameter with an integer value.
   */
  public static class IntParam extends LiteralParam<Integer> {

    public IntParam(Integer value, Integer defaultValue) {
      super(int.class, value, defaultValue);
    }

    public IntParam(Integer value) {
      this(value, null);
    }

  }

  /**
   * Is a parameter with an integer value.
   */
  public static class LongParam extends LiteralParam<Long> {

    public LongParam(Long value, Long defaultValue) {
      super(long.class, value, defaultValue);
    }

    public LongParam(Long value) {
      this(value, null);
    }

  }

  /**
   * Is a parameter with a boolean (true/false) value.
   */
  public static class BoolParam extends LiteralParam<Boolean> {

    public BoolParam(Boolean value, Boolean defaultValue) {
      super(boolean.class, value == null ? Boolean.FALSE : value, defaultValue);
    }

  }

  /**
   * Is a parameter with a Duration value.
   */
  public static class DurationParam extends MethodParam<Duration> {

    public DurationParam(Duration value, Duration defaultValue) {
      super(Duration.class, value, defaultValue);
    }

    public DurationParam(Duration value) {
      this(value, null);
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
  public static class ChildrenParam<T> extends MethodParam<T> {

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
      super(checkChildrenType(childrenClass), null, null);
    }

    private static <T> Class<T> checkChildrenType(Class<T> childrenClass) {
      if (!childrenClass.isArray()) {
        throw new IllegalArgumentException(
            "You need always to provide an array class and not the raw class for the children. "
                + "Eg use TestPlanChild[].class");
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
