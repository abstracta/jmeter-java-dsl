package us.abstracta.jmeter.javadsl.wrapper.wrappers;

import us.abstracta.jmeter.javadsl.core.DslTestElement;

/**
 * Is a test element which allows easy usage in DSL of custom or not supported JMeter test
 * elements.
 *
 * @param <T> Is the type of the test element wrapper for usage in fluent API style.
 * @since 0.41
 */
public interface TestElementWrapper<T> extends DslTestElement {

  /**
   * Allows specifying a property to be set on wrapped test element.
   *
   * @param name  is the name of the property to be set.
   * @param value is the value to be associated to the property.
   * @return the test element wrapper for further configuration and usage.
   */
  T prop(String name, Object value);

}
