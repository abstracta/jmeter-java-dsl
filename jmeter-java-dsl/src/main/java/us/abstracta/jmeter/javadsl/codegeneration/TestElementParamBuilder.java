package us.abstracta.jmeter.javadsl.codegeneration;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.BiFunction;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import us.abstracta.jmeter.javadsl.codegeneration.params.BoolParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.DoubleParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.DurationParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.DynamicParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.EncodingParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.FloatParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.IntParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.LongParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.NameParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;

/**
 * Is a wrapper class for {@link TestElement} for easy creation of {@link MethodParam} instances.
 *
 * @since 0.45
 */
public class TestElementParamBuilder {

  private final TestElement testElement;
  private final String propsPrefix;

  /**
   * Creates an instance for a given {@link TestElement}.
   * <p>
   * When creating parameters from properties names, fully qualified property names are required.
   * <p>
   * If the test element has a common prefix for properties, consider using
   * {@link #TestElementParamBuilder(TestElement, String)} instead.
   *
   * @param testElement is the JMeter test element backing this parameter builder.
   */
  public TestElementParamBuilder(TestElement testElement) {
    this(testElement, null);
  }

  /**
   * Same as {@link #TestElementParamBuilder(TestElement)} but allowing to define a common
   * properties prefix for the test element.
   *
   * @param testElement is the JMeter test element backing this parameter builder.
   * @param propsPrefix is the common prefix for all properties of the test element.
   * @see #TestElementParamBuilder(TestElement)
   */
  public TestElementParamBuilder(TestElement testElement, String propsPrefix) {
    this.testElement = testElement;
    this.propsPrefix = propsPrefix != null ? propsPrefix + "." : "";
  }

  /**
   * Generates a MethodParam representing a test element name.
   *
   * @param defaultName is the default name used by the JMeter test element.
   * @return the MethodParam instance.
   */
  public NameParam nameParam(String defaultName) {
    return new NameParam(testElement.getName(), defaultName);
  }

  /**
   * Generates a MethodParam representing an integer test element property.
   *
   * @param propName     is the name of the property holding an integer value. For nested properties
   *                     (a property that is inside another object property) you can use the slash
   *                     character to separate the levels (eg: http_config/use_proxy).
   * @param defaultValue is the default value used by the test element for this property.
   * @return the MethodParam instance.
   * @throws UnsupportedOperationException when no integer can be parsed from the property value.
   * @since 0.61
   */
  public MethodParam intParam(String propName, Integer defaultValue) {
    return buildParam(propName, IntParam::new, defaultValue);
  }

  /**
   * Same as {@link #intParam(String, Integer)} but with no default value.
   *
   * @param propName is the name of the property. For nested properties (a property that is inside
   *                 another object property) you can use the slash character to separate the levels
   *                 (eg: http_config/use_proxy).
   * @return the {@link MethodParam} instance.
   * @see #intParam(String, Integer)
   */
  public MethodParam intParam(String propName) {
    return intParam(propName, null);
  }

  public <V, T extends MethodParam> MethodParam buildParam(String propName,
      BiFunction<String, V, T> builder, V defaultValue) {
    String propVal = prop(propName).getStringValue();
    if (propVal != null && DynamicParam.matches(propVal)) {
      return new DynamicParam(propVal);
    }
    try {
      return builder.apply(propVal, defaultValue);
    } catch (Exception e) {
      throw new UnsupportedOperationException(
          String.format("DSL does not currently support '%s' as value for %s. If you need this "
                  + "support please open an issue in GitHub repository.", propVal,
              propsPrefix + propName), e);
    }
  }

  /**
   * Gets the {@link JMeterProperty} instance for the given test element and property name.
   * <p>
   * This is useful in general to access raw values of properties, and abstract how to access them
   * (eg: if they are nested in object properties you can easily access them using slashes)
   *
   * @param propName is the name of the property. For nested properties (a property that is inside
   *                 another object property) you can use the slash character to separate the levels
   *                 (eg: http_config/use_proxy).
   * @return the {@link JMeterProperty} instance.
   */
  public JMeterProperty prop(String propName) {
    propName = propsPrefix + propName;
    String[] propLevels = propName.split("/");
    TestElement propHolder = testElement;
    for (int i = 0; i < propLevels.length - 1; i++) {
      propHolder = (TestElement) testElement.getProperty(propLevels[i]).getObjectValue();
    }
    return propHolder.getProperty(propLevels[propLevels.length - 1]);
  }

  /**
   * Generates a MethodParam representing a long test element property.
   *
   * @param propName     is the name of the property holding a long value. For nested properties (a
   *                     property that is inside another object property) you can use the slash
   *                     character to separate the levels (eg: http_config/use_proxy).
   * @param defaultValue is the default value used by the test element for this property.
   * @return the MethodParam instance.
   * @throws UnsupportedOperationException when no long can be parsed from the property value.
   * @since 1.10
   */
  public MethodParam longParam(String propName, Long defaultValue) {
    return buildParam(propName, LongParam::new, defaultValue);
  }

  /**
   * Same as {@link #longParam(String, Long)} but with no default value.
   *
   * @param propName is the name of the property holding a long value. For nested properties (a
   *                 property that is inside another object property) you can use the slash
   *                 character to separate the levels (eg: http_config/use_proxy).
   * @return the MethodParam instance.
   * @throws UnsupportedOperationException when no long can be parsed from the property value.
   * @since 0.61
   */
  public MethodParam longParam(String propName) {
    return longParam(propName, null);
  }

  /**
   * Generates a MethodParam representing a float test element property.
   *
   * @param propName is the name of the property holding a float value. For nested properties (a
   *                 property that is inside another object property) you can use the slash
   *                 character to separate the levels (eg: http_config/use_proxy).
   * @return the MethodParam instance.
   * @throws UnsupportedOperationException when no float can be parsed from the property value.
   * @since 0.63
   */
  public MethodParam floatParam(String propName) {
    return buildParam(propName, FloatParam::new, null);
  }

  /**
   * Generates a MethodParam representing a double test element property.
   *
   * @param propName is the name of the property holding a double value. For nested properties (a
   *                 property that is inside another object property) you can use the slash
   *                 character to separate the levels (eg: http_config/use_proxy).
   * @return the MethodParam instance.
   * @throws UnsupportedOperationException when no double can be parsed from the property value.
   * @since 0.61
   */
  public MethodParam doubleParam(String propName) {
    return buildParam(propName, DoubleParam::new, null);
  }

  /**
   * Gets a MethodParam for a string test element property.
   *
   * @param propName     is the name of the property. For nested properties (a property that is
   *                     inside another object property) you can use the slash character to separate
   *                     the levels (eg: http_config/use_proxy).
   * @param defaultValue the default value assigned to the JMeter test element property.
   * @return the {@link MethodParam} instance.
   */
  public MethodParam stringParam(String propName, String defaultValue) {
    return new StringParam(prop(propName).getStringValue(), defaultValue);
  }

  /**
   * Same as {@link #stringParam(String, String)} but with no default value.
   *
   * @param propName is the name of the property. For nested properties (a property that is inside
   *                 another object property) you can use the slash character to separate the levels
   *                 (eg: http_config/use_proxy).
   * @return the {@link MethodParam} instance.
   * @see #stringParam(String, String)
   */
  public MethodParam stringParam(String propName) {
    return stringParam(propName, null);
  }

  /**
   * Gets a MethodParam representing a boolean test element property.
   *
   * @param propName     is the name of the property. For nested properties (a property that is
   *                     inside another object property) you can use the slash character to separate
   *                     the levels (eg: http_config/use_proxy).
   * @param defaultValue the default value assigned to the JMeter test element property.
   * @return the {@link MethodParam} instance.
   */
  public MethodParam boolParam(String propName, boolean defaultValue) {
    return buildParam(propName, BoolParam::new, defaultValue);
  }

  /**
   * Gets a MethodParam representing a test element property containing a duration (in seconds).
   *
   * @param propName     is the name of the property. For nested properties (a property that is
   *                     inside another object property) you can use the slash character to separate
   *                     the levels (eg: http_config/use_proxy).
   * @param defaultValue the default value assigned to the JMeter test element property.
   * @return the {@link MethodParam} instance.
   */
  public MethodParam durationParam(String propName, Duration defaultValue) {
    return buildParam(propName, DurationParam::new, defaultValue);
  }

  /**
   * Same as {@link #durationParam(String, Duration)} but with no default value.
   *
   * @param propName is the name of the property. For nested properties (a property that is inside
   *                 another object property) you can use the slash character to separate the levels
   *                 (eg: http_config/use_proxy).
   * @return the {@link MethodParam} instance.
   * @see #durationParam(String, Duration)
   */
  public MethodParam durationParam(String propName) {
    return durationParam(propName, null);
  }

  /**
   * Gets a MethodParam representing a test element property containing a duration (in
   * milliseconds).
   *
   * @param propName     is the name of the property. For nested properties (a property that is
   *                     inside another object property) you can use the slash character to separate
   *                     the levels (eg: http_config/use_proxy).
   * @param defaultValue the default value assigned to the JMeter test element property.
   * @return the {@link MethodParam} instance.
   */
  public MethodParam durationParamMillis(String propName, Duration defaultValue) {
    return buildParam(propName,
        (expression, defValue) -> new DurationParam(expression, defValue, ChronoUnit.MILLIS),
        defaultValue);
  }

  /**
   * Gets a MethodParam representing a test element property with a restricted set (enumerated) of
   * string values.
   *
   * @param propName     is the name of the property. For nested properties (a property that is
   *                     inside another object property) you can use the slash character to separate
   *                     the levels (eg: http_config/use_proxy).
   * @param defaultValue the default value assigned to the JMeter test element property.
   * @return the {@link MethodParam} instance.
   * @since 0.62
   */
  public <T extends Enum<?> & EnumParam.EnumPropertyValue> MethodParam enumParam(String propName,
      T defaultValue) {
    Class<T> enumType = (Class<T>) defaultValue.getClass();
    return buildParam(propName, (s, d) -> new EnumParam<>(enumType, s, d), defaultValue);
  }

  /**
   * Gets a MethodParam representing a test element property containing an encoding (Charset).
   *
   * @param propName     is the name of the property. For nested properties (a property that is
   *                     inside another object property) you can use the slash character to separate
   *                     the levels (eg: http_config/use_proxy).
   * @param defaultValue the default value assigned to the JMeter test element property.
   * @return the {@link MethodParam} instance.
   * @since 0.62
   */
  public MethodParam encodingParam(String propName, Charset defaultValue) {
    return buildParam(propName, EncodingParam::new, defaultValue);
  }

}
