package us.abstracta.jmeter.javadsl.core.assertions;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.responseAssertion;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.assertions.DslResponseAssertion.TargetField;
import us.abstracta.jmeter.javadsl.core.testelements.DslScopedTestElement.Scope;

public class DslResponseAssertionTest extends JmeterDslTest {

  @Test
  public void shouldFailRequestWhenResponseAssertionDoesNotMatch() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .children(
                    responseAssertion()
                        .containsSubstrings("test")
                )
        )
    ).run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldMarkRequestAsSuccessWhenInvalidRequestButResponseAssertionIgnoresStatus()
      throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            httpSampler("invalidUrl")
                .children(
                    responseAssertion()
                        .ignoreStatus()
                )
        )
    ).run();
    assertThat(stats.overall().errorsCount()).isEqualTo(0);
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithDefaultResponseAssertion() {
      return testPlan(
          responseAssertion()
              .containsSubstrings("Test")
      );
    }

    public DslTestPlan testPlanWithCustomResponseAssertion() {
      return testPlan(
          responseAssertion()
              .scope(Scope.ALL_SAMPLES)
              .fieldToTest(TargetField.RESPONSE_HEADERS)
              .ignoreStatus()
              .anyMatch()
              .invertCheck()
              .containsSubstrings(
                  "Test",
                  "Test2"
              )
      );
    }

    public DslTestPlan testPlanWithEqualsResponseAssertion() {
      return testPlan(
          responseAssertion()
              .equalsToStrings("Test")
      );
    }

    public DslTestPlan testPlanWithMatchesResponseAssertion() {
      return testPlan(
          responseAssertion()
              .matchesRegexes("Test")
      );
    }

    public DslTestPlan testPlanWithContainsRegexesResponseAssertion() {
      return testPlan(
          responseAssertion()
              .containsRegexes("Test")
      );
    }

  }

}
