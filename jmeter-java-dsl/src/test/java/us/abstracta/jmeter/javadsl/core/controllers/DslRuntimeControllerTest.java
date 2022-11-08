package us.abstracta.jmeter.javadsl.core.controllers;

import org.junit.jupiter.api.*;
import us.abstracta.jmeter.javadsl.*;
import us.abstracta.jmeter.javadsl.codegeneration.*;
import us.abstracta.jmeter.javadsl.core.*;

import static org.assertj.core.api.Assertions.*;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class DslRuntimeControllerTest extends JmeterDslTest {

  @Test
  public void shouldExecuteMultipleTimesWhenRuntimeControllerInPlan() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            runtimeController ("1",
                httpSampler(wiremockUri)
            )
        )
    ).run();
    assertThat(stats.overall().samplesCount()).isGreaterThan(1);
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithRuntimeController() {
      return testPlan(
          threadGroup(1, 1,
                  runtimeController("5",
                  httpSampler("http://localhost")
              )
          )
      );
    }

  }

}
