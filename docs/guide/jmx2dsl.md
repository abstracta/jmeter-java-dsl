## DSL code generation from JMX file

To ease migrating existing JMeter test plans and ease learning about DSL features, the DSL provides `jmx2dsl` cli command (download the latest cli version from [releases page](https://github.com/abstracta/jmeter-java-dsl/releases) or use [jbang](https://www.jbang.dev/documentation/guide/latest/index.html)) command line tool which you can use to generate DSL code from existing JMX files.

As an example:

:::: code-group
::: code-group-item Java
```bash
java -jar jmdsl.jar jmx2dsl test-plan.jmx
```
:::

::: code-group-item Jbang
```bash
jbang us.abstracta.jmeter:jmeter-java-dsl-cli:1.19 jmx2dsl test-plan.jmx
```
:::
::::

Could generate something like the following output:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
/*
These commented lines make the class executable if you have jbang installed by making the file
executable (eg: chmod +x ./PerformanceTest.java) and just executing it with ./PerformanceTest.java
*/
//DEPS org.assertj:assertj-core:3.23.1
//DEPS org.junit.jupiter:junit-jupiter-engine:5.9.1
//DEPS org.junit.platform:junit-platform-launcher:1.9.1
//DEPS us.abstracta.jmeter:jmeter-java-dsl:1.19

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.io.PrintWriter;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void test() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            httpSampler("http://my.service")
        ),
        jtlWriter("target/jtls")
    ).run();
    assertThat(stats.overall().errorsCount()).isEqualTo(0);
  }

  /*
   This method is only included to make the test class self-executable. You can remove it when
   executing tests with maven, gradle, or some other tool.
   */
  public static void main(String[] args) {
    SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
    LauncherFactory.create()
        .execute(LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(PerformanceTest.class))
                .build(),
            summaryListener);
    TestExecutionSummary summary = summaryListener.getSummary();
    summary.printFailuresTo(new PrintWriter(System.err));
    System.exit(summary.getTotalFailureCount() > 0 ? 1 : 0);
  }

}
```

::: tip
Review and try generated code before executing it as is. I.e: tune thread groups and iterations to 1 to give it a try.
:::

::: tip
Always review generated DSL code. You should add proper assertions to it, might want to clean it up, add to your maven or gradle project dependencies listed on initial comments of generated code, modularize it better, check that conversion is accurate according to DSL, or even propose improvements for it in the GitHub repository.
:::

::: tip
Conversions can always be improved, and since there are many combinations and particular use cases, different semantics, etc, getting a perfect conversion for every scenario can get tricky.

If you find any potential improvement to code generation, **please help us by creating an [issue](https://github.com/abstracta/jmeter-java-dsl/issues) or [discussion](https://github.com/abstracta/jmeter-java-dsl/discussions)** in GitHub repository.
:::