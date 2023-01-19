### Properties

You might reach a point where you want to pass some parameter to the test plan or want to share some object or data that is available for all threads to use. In such scenarios, you can use JMeter properties.

JMeter properties is a map of keys and values, that is accessible to all threads. To access them you can use `${__P(PROPERTY_NAME)}` or equivalent `${__property(PROPERTY_NAME)` inside almost any string, `props['PROPERTY_NAME']` inside groovy scripts or `props.get("PROPERTY_NAME")` in lambda expressions.

To set them, you can use `prop()` method included in `EmbeddedJmeterEngine` like in the following example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.engines.EmbeddedJmeterEngine;

public class PerformanceTest {

  @Test
  public void testProperties() {
    testPlan(
        threadGroup(1, 1,
            httpSampler("http://myservice.test/${__P(MY_PROP)}")
        )
    ).runIn(new EmbeddedJmeterEngine()
        .prop("MY_PROP", "MY_VAL"));
  }

}
```

Or you can set them in groovy or java code, like in the following example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void testProperties() {
    testPlan(
        threadGroup(1, 1,
            jsr223Sampler("props.put('MY_PROP', 'MY_VAL')"),
            httpSampler("http://myservice.test/${__P(MY_PROP)}")
        )
    ).run();
  }

}
```

Or you can even load them from a file, which might be handy to have different files with different values for different execution profiles (eg: different environments). Eg:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.engines.EmbeddedJmeterEngine;

public class PerformanceTest {

  @Test
  public void testProperties() {
    testPlan(
        threadGroup(1, 1,
            httpSampler("http://myservice.test/${__P(MY_PROP)}")
        )
    ).runIn(new EmbeddedJmeterEngine()
        .propertiesFile("my.properties"));
  }

}
```

::: tip
You can put any object (not just strings) in properties, but only strings can be accessed via `${__P(PROPERTY_NAME)}` and `${__property(PROPERTY_NAME)}`.

Being able to put any kind of object allows you to do very powerful stuff, like implementing a custom cache, or injecting some custom logic to a test plan.
:::

::: tip
You can also specify properties through JVM system properties either by setting JVM parameter `-D` or using `System.setProperty()` method.

When properties are set as JVM system properties, they are not accessible via `props[PROPERTY_NAME]` or `props.get("PROPERTY_NAME")`. If you need to access them from groovy or java code, then use `props.getProperty("PROPERTY_NAME")` instead.
:::

::: warning
JMeter properties can currently only be used with `EmbeddedJmeterEngine`, so use them sparingly and prefer other mechanisms when available.
:::
