package us.abstracta.jmeter.javadsl.core.postprocessors;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslJsr223PostProcessorTest extends JmeterDslTest {

  @Test
  public void shouldReportNoFailureWhenJsr223PostProcessorModifiesFailedRequest() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            httpSampler("invalidUrl")
                .children(
                    jsr223PostProcessor("prev.successful = true")
                )
        )
    ).run();
    assertThat(stats.overall().errorsCount()).isEqualTo(0);
  }

  @Test
  public void shouldReportNoFailureWhenJsr223PostProcessorModifiesFailedRequestWithLambdaScript()
      throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            httpSampler("invalidUrl")
                .children(
                    jsr223PostProcessor(s -> s.prev.setSuccessful(true))
                )
        )
    ).run();
    assertThat(stats.overall().errorsCount()).isEqualTo(0);
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithJsr223PostProcessor() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      jsr223PostProcessor("println prev.responseCode")
                  )

          )
      );
    }

    public DslTestPlan testPlanWithJsr223PostProcessorAndNonDefaultSettings() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      jsr223PostProcessor("myScript", "console.log(prev.getResponseCode())")
                          .language("javascript")
                  )

          )
      );
    }

  }

}
