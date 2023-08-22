## Custom or yet not supported test elements

Whenever you find some JMeter test element or feature that is not yet supported by the DSL, **we strongly encourage you to request it as an issue [here](https://github.com/abstracta/jmeter-java-dsl/issues)** or even contribute it to the DSL (check [Contributing guide](/CONTRIBUTING.md)) so the entire community can benefit from it.

In some cases though, you might have some private custom test element that you don't want to publish or share with the rest of the community, or you are just really in a hurry and want to use it while the proper support is included in the DSL.

For such cases, the preferred approach is implementing a builder class for the test element. Eg:

```java
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.samplers.BaseSampler;

public class DslCustomSampler extends BaseSampler<DslCustomSampler> {

  private String myProp;

  private DslCustomSampler(String name) {
    super(name, CustomSamplerGui.class); // you can pass null here if custom sampler is a test bean
  }

  public DslCustomSampler myProp(String val) {
    this.myProp = val;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    CustomSampler ret = new CustomSampler();
    ret.setMyProp(myProp);
    return ret;
  }

  public static DslCustomSampler customSampler(String name) {
    return new DslCustomSampler(name);
  }

}
```

Which you can use as any other JMeter DSL component, like in this example:

```java
import static us.abstracta.jmeter.javadsl.DslCustomSampler.*;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void test() throws Exception {
    testPlan(
        threadGroup(1, 1,
            customSampler("mySampler")
                .myProp("myVal")
            )
    ).run();
  }

}
```

This approach allows for easy reuse, compact and simple usage in tests, and you might even create your own `CustomJmeterDsl` class containing builder methods for many custom components.

Alternatively, when you want to skip creating subclasses, you might use the DSL wrapper module.

Include the module on your project:

:::: code-group
::: code-group-item Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-wrapper</artifactId>
  <version>1.19</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-wrapper:1.19'
```
:::
::::

And use a wrapper like in the following example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.wrapper.WrapperJmeterDsl.*;

import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void test() throws Exception {
    testPlan(
        threadGroup(1, 1,
            testElement("mySampler", new CustomSamplerGui()) // for test beans you can just provide the test bean instance
                .prop("myProp","myVal")
            )
    ).run();
  }

}
```

Check [WrapperJmeterDsl](/jmeter-java-dsl-wrapper/src/main/java/us/abstracta/jmeter/javadsl/wrapper/WrapperJmeterDsl.java) for more details and additional wrappers.
