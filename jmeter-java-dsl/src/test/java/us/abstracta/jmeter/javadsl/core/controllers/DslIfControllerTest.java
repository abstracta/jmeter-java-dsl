package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.ifController;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslIfControllerTest extends JmeterDslTest {

  @Test
  public void shouldExecuteOnlyTrueControllersWhenIfControllersWithGroovyExpression()
      throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            ifController("${__groovy(true)}", httpSampler(SAMPLE_1_LABEL, wiremockUri)),
            ifController("${__groovy(false)}", httpSampler(SAMPLE_2_LABEL, wiremockUri))
        )
    ).run();
    assertThat(extractCounts(stats)).isEqualTo(buildExpectedCounts());
  }

  private Map<String, Long> buildExpectedCounts() {
    Map<String, Long> expectedStats = new HashMap<>();
    expectedStats.put(OVERALL_STATS_LABEL, (long) 1);
    expectedStats.put(SAMPLE_1_LABEL, (long) 1);
    return expectedStats;
  }

  @Test
  public void shouldExecuteOnlyTrueControllersWhenIfControllersWithLambdaScript() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            ifController(v -> true, httpSampler(SAMPLE_1_LABEL, wiremockUri)),
            ifController(v -> false, httpSampler(SAMPLE_2_LABEL, wiremockUri))
        )
    ).run();
    assertThat(extractCounts(stats)).isEqualTo(buildExpectedCounts());
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithIfController() {
      return testPlan(
          threadGroup(1, 1,
              ifController("${X} == 1",
                  httpSampler("http://localhost")
              )
          )
      );
    }

  }

}
