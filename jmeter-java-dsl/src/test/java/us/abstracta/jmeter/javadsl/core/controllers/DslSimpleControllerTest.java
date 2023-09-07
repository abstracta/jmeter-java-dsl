package us.abstracta.jmeter.javadsl.core.controllers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.dummySampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.simpleController;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

public class DslSimpleControllerTest extends JmeterDslTest {

    @Test
    public void shouldOnlyIncludeSpecifiedSamples()
            throws Exception {

        TestPlanStats testPlanStats = testPlan(
                threadGroup(1, 1,
                        simpleController("Simple Controller",
                                httpSampler(wiremockUri),
                                dummySampler("Test")
                        )
                )
        ).run();
        assertThat(testPlanStats.overall().samplesCount()).isEqualTo(2);
        assertThat(testPlanStats.byLabel("jp@gc - Dummy Sampler").samplesCount()).isEqualTo(1);
        assertThat(testPlanStats.byLabel("HTTP Request").samplesCount()).isEqualTo(1);
        assertThat(testPlanStats.byLabel("Simple Controller")).isNull();
    }

    @Test
    public void allowsNestedSimpleControllers()
            throws Exception {

        TestPlanStats testPlanStats = testPlan(
                threadGroup(1, 1,
                        simpleController("Simple Controller",
                                simpleController("Simple Controller",
                                        httpSampler(wiremockUri)
                                ),
                                httpSampler(wiremockUri),
                                dummySampler("Test")
                        )
                )
        ).run();
        assertThat(testPlanStats.overall().samplesCount()).isEqualTo(3);
        assertThat(testPlanStats.byLabel("jp@gc - Dummy Sampler").samplesCount()).isEqualTo(1);
        assertThat(testPlanStats.byLabel("HTTP Request").samplesCount()).isEqualTo(2);
    }

  @SuppressWarnings("unused")
  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithSimpleController() {
      return testPlan(
          threadGroup(1, 1,
              simpleController("test",
                  httpSampler("http://localhost"),
                  httpSampler("http://mysite.com")
              )
          )
      );
    }

  }

}
