package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.transaction;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslTransactionControllerTest extends JmeterDslTest {

  @Test
  public void shouldIncludeTransactionSampleInResultsWhenTestPlanWithTransaction()
      throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            transaction("My Transaction",
                httpSampler(wiremockUri)
            )
        )
    ).run();
    assertThat(stats.overall().samplesCount()).isEqualTo(2);
  }

  @Test
  public void shouldOnlyReportTransactionResultWhenTestPlanWithTransactionAndGenerateParentSample()
      throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            transaction("My Transaction")
                .generateParentSample()
                .children(
                    httpSampler(wiremockUri)
                )
        )
    ).run();
    assertThat(stats.overall().samplesCount()).isEqualTo(1);
  }

  @SuppressWarnings("unused")
  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithSimpleTransaction() {
      return testPlan(
          threadGroup(1, 1,
              transaction("test",
                  httpSampler("http://localhost"),
                  httpSampler("http://mysite.com")
              )
          )
      );
    }

    // This is required for proper conversion of automatically recorded transactions
    public DslTestPlan testPlanWithEmptyTransactionName() {
      return testPlan(
          threadGroup(1, 1,
              // TODO you should set a proper transaction name to avoid incorrect metrics reporting
              transaction("",
                  httpSampler("http://localhost"),
                  httpSampler("http://mysite.com")
              )
          )
      );
    }

    public DslTestPlan testPlanWithTransactionAndNonDefaultFlags() {
      return testPlan(
          threadGroup(1, 1,
              transaction("test")
                  .generateParentSample()
                  .includeTimersAndProcessorsTime()
                  .children(
                      httpSampler("http://localhost"),
                      httpSampler("http://mysite.com")
                  )
          )
      );
    }

  }

}
