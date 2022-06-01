package us.abstracta.jmeter.javadsl.codegeneration;

import java.time.Duration;
import java.util.function.BiFunction;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.BoolParam;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.DurationParam;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.DynamicParam;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.IntParam;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.NameParam;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.StringParam;

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
   * Generates a new {@link NameParam} instance for the test element.
   *
   * @param defaultName is the default name used by the JMeter test element.
   * @return a {@link NameParam} instance.
   */
  public MethodParam nameParam(String defaultName) {
    return new NameParam(testElement.getName(), defaultName);
  }

  /**
   * Generates a new {@link IntParam} instance for a test element property.
   *
   * @param propName is the name of the property holding an integer value. For nested properties (a
   *                 property that is inside another object property) you can use the slash
   *                 character to separate the levels (eg: http_config/use_proxy).
   * @return the {@link IntParam} instance.
   * @throws UnsupportedOperationException when no integer can be parsed from the property value.
   */
  public MethodParam intParam(String propName) {
    return buildParam(propName, IntParam::new, (Integer) null);
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
   * Gets a {@link StringParam} instance for the given property name and default value.
   *
   * @param propName     is the name of the property. For nested properties (a property that is
   *                     inside another object property) you can use the slash character to separate
   *                     the levels (eg: http_config/use_proxy).
   * @param defaultValue the default value assigned to the JMeter test element property.
   * @return the {@link StringParam} instance.
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
   * @return the {@link StringParam} instance.
   * @see #stringParam(String, String)
   */
  public MethodParam stringParam(String propName) {
    return stringParam(propName, null);
  }

  /**
   * Gets a {@link BoolParam} instance for the given property name and default value.
   *
   * @param propName     is the name of the property. For nested properties (a property that is
   *                     inside another object property) you can use the slash character to separate
   *                     the levels (eg: http_config/use_proxy).
   * @param defaultValue the default value assigned to the JMeter test element property.
   * @return the {@link BoolParam} instance.
   */
  public MethodParam boolParam(String propName, boolean defaultValue) {
    return buildParam(propName, BoolParam::new, defaultValue);
  }

  /**
   * Gets a {@link DurationParam} instance for the given property name and default value.
   * <p>
   * The property is considered to be a given number of seconds.
   *
   * @param propName     is the name of the property. For nested properties (a property that is
   *                     inside another object property) you can use the slash character to separate
   *                     the levels (eg: http_config/use_proxy).
   * @param defaultValue the default value assigned to the JMeter test element property.
   * @return the {@link DurationParam} instance.
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
   * @return the {@link DurationParam} instance.
   * @see #durationParam(String, Duration)
   */
  public MethodParam durationParam(String propName) {
    return durationParam(propName, null);
  }

}
