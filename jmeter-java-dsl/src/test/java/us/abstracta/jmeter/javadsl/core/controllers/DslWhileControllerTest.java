package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.whileController;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

public class DslWhileControllerTest extends JmeterDslTest {

  private static final String CALLS_VAR = "CALLS";
  private static final int CALLS = 3;

  @Test
  public void shouldExecuteWhileTrueWhenWhileControllerWithGroovyExpression() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            whileController("${__groovy(vars.getObject('" + CALLS_VAR + "') != " + CALLS + ")}",
                buildSamplerCountingInVar(CALLS_VAR)
            )
        )
    ).run();
    assertThat(stats.overall().samplesCount()).isEqualTo(CALLS);
  }

  private DslHttpSampler buildSamplerCountingInVar(String callsVar) {
    return httpSampler(wiremockUri)
        .children(jsr223PostProcessor(s -> {
          Integer prevCalls = (Integer) s.vars.getObject(callsVar);
          s.vars.putObject(callsVar, prevCalls == null ? 1 : prevCalls + 1);
        }));
  }

  @Test
  public void shouldExecuteOnlyTrueControllersWhenIfControllersWithLambdaScript() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            whileController(s -> {
                  Integer val = (Integer) s.vars.getObject(CALLS_VAR);
                  return val == null || val != CALLS;
                },
                buildSamplerCountingInVar(CALLS_VAR)
            )
        )
    ).run();
    assertThat(stats.overall().samplesCount()).isEqualTo(CALLS);
  }

  @Test
  public void shouldIterateGivenNumberOfTimesWhenWhileControllerWithConditionUsingIndexVariable()
      throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            whileController("${__groovy(vars.getObject('__jm__while__idx') < " + CALLS + ")}",
                buildSamplerCountingInVar(CALLS_VAR)
            )
        )
    ).run();
    assertThat(stats.overall().samplesCount()).isEqualTo(CALLS);
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithWhileController() {
      return testPlan(
          threadGroup(1, 1,
              whileController("while","${X} == 1",
                  httpSampler("http://localhost")
              )
          )
      );
    }

  }

}
