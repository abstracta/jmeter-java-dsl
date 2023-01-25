### Variables

In general, when you want to reuse a certain value of your script, you can, and is the preferred way, just to use Java variables. In some cases though, you might need to pre-initialize some JMeter thread variable (for example to later be used in an `ifController`) or easily update its value without having to use a jsr223 element for that. For these cases, the DSL provides the `vars()` method.

Here is an example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void testPerformance() throws Exception {
    String pageVarName = "PAGE";
    String firstPage = "1";
    String endPage = "END";
    testPlan(
        vars()
            .set(pageVarName, firstPage),
        threadGroup(2, 10,
            ifController(s -> !s.vars.get(pageVarName).equals(endPage),
                httpSampler("http://my.service/accounts?page=${" + pageVarName +"}")
                    .children(
                        regexExtractor(pageVarName, "next=.*?page=(\\d+)")
                            .defaultValue(endPage)
                    )
            ),
            ifController(s -> s.vars.get(pageVarName).equals(endPage),
                vars()
                    .set(pageVarName, firstPage)
            )
        )
    ).run();
  }

}
```

::: warning
For special consideration of existing JMeter users:

`vars()` internally uses JMeter User Defined Variables (aka UDV) when placed as a test plan child, but a JSR223 sampler otherwise. This decision avoids several non-intuitive behaviors of JMeter UDV which are listed in red blocks in [the JMeter component documentation](https://jmeter.apache.org/usermanual/component_reference.html#User_Defined_Variables).

Internally using a JSR223 sampler, allows DSL users to properly scope a variable to where it is placed (eg: defining a variable in one thread has no effect on other threads or thread groups), set the value when it's actually needed (not just at the beginning of test plan execution), and support cross-variable references (i.e.: if `var1=test` and `var2=${var1}`, then the value of `var2` would be solved to `test`).

When `vars()` is located as a direct child of the test plan, due to the usage of UDV, declared variables will be available to all thread groups and no variable cross-reference is supported.
:::

Check [DslVariables](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/configs/DslVariables.java) for more details.
