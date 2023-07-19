package us.abstracta.jmeter.javadsl.core.assertions;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.dummySampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsonAssertion;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsonExtractor.JsonQueryLanguage;

public class DslJsonAssertionTest {

  private static final String PROPERTY_NAME = "stringProp";
  private static final String JSON_BODY = "{"
      + "\"stringProp\": \"val\", "
      + "\"nullProp\": null, "
      + "\"intProp\": 1, "
      + "\"listProp\": [1, 2], "
      + "\"objProp\": {"
      + "\"prop\": 1"
      + "}}";

  @Test
  public void shouldGetFailureWhenJsonAssertionWithNotExistingPath() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion("prop"));
    isFailure(stats);
  }

  private static TestPlanStats runTestPlanWithAssertion(DslJsonAssertion assertion)
      throws IOException {
    return testPlan(
        threadGroup(1, 1,
            dummySampler(JSON_BODY),
            assertion
        )
    ).run();
  }

  private static void isFailure(TestPlanStats stats) {
    isFailureCount(stats, 1);
  }

  private static void isFailureCount(TestPlanStats stats, int expected) {
    assertThat(stats.overall().errorsCount()).isEqualTo(expected);
  }

  @Test
  public void shouldGetSuccessWhenJsonAssertionWithExistingPath() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion(PROPERTY_NAME));
    isSuccess(stats);
  }

  private static void isSuccess(TestPlanStats stats) {
    isFailureCount(stats, 0);
  }

  @Test
  public void shouldGetFailureWhenJsonAssertionWithNonMatchingRegex() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion(PROPERTY_NAME)
        .matches("v.l2"));
    isFailure(stats);
  }

  @Test
  public void shouldGetSuccessWhenJsonAssertionWithMatchingRegex() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion(PROPERTY_NAME)
        .matches("v.l"));
    isSuccess(stats);
  }

  @Test
  public void shouldGetFailureWhenJsonAssertionWithNonEqualValue() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion(PROPERTY_NAME)
        .equalsTo("val2"));
    isFailure(stats);
  }

  @Test
  public void shouldGetSuccessWhenJsonAssertionWithEqualStringValue() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion(PROPERTY_NAME)
        .equalsTo("val"));
    isSuccess(stats);
  }

  @Test
  public void shouldGetSuccessWhenJsonAssertionWithEqualIntValue() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion("intProp")
        .equalsTo(1));
    isSuccess(stats);
  }

  @Test
  public void shouldGetSuccessWhenJsonAssertionWithEqualListValue() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion("listProp")
        .equalsTo(Arrays.asList(1, 2)));
    isSuccess(stats);
  }

  @Test
  public void shouldGetSuccessWhenJsonAssertionWithEqualMapValue() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion("objProp")
        .equalsTo(Collections.singletonMap("prop", 1)));
    isSuccess(stats);
  }

  @Test
  public void shouldGetFailureWhenJsonAssertionWithNonNullValue() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion(PROPERTY_NAME)
        .equalsTo(null));
    isFailure(stats);
  }

  @Test
  public void shouldGetSuccessWhenJsonAssertionWithNullValue() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion("nullProp")
        .equalsTo(null));
    isSuccess(stats);
  }

  @Test
  public void shouldGetSuccessWhenJsonAssertionWithEqualJsonStringValue() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion(PROPERTY_NAME)
        .equalsToJson("\"val\""));
    isSuccess(stats);
  }

  @Test
  public void shouldGetSuccessWhenJsonAssertionWithEqualJsonIntValue() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion("intProp")
        .equalsToJson("1"));
    isSuccess(stats);
  }

  @Test
  public void shouldGetSuccessWhenJsonAssertionWithEqualJsonObjectValue() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion("objProp")
        .equalsToJson("{\"prop\": 1}"));
    isSuccess(stats);
  }

  @Test
  public void shouldGetFailureWhenJsonAssertionWithNegatedExistingPath() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion(PROPERTY_NAME).not());
    isFailure(stats);
  }

  @Test
  public void shouldGetSuccessWhenJsonAssertionWithNegatedNonExistingPath() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion("prop").not());
    isSuccess(stats);
  }

  @Test
  public void shouldGetFailureWhenJsonAssertionWithNonExistingJsonPath() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion("$.prop")
        .queryLanguage(JsonQueryLanguage.JSON_PATH));
    isFailure(stats);
  }

  @Test
  public void shouldGetSuccessWhenJsonAssertionWithExistingJsonPath() throws Exception {
    TestPlanStats stats = runTestPlanWithAssertion(jsonAssertion("$.stringProp")
        .queryLanguage(JsonQueryLanguage.JSON_PATH));
    isSuccess(stats);
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan simpleJsonAssertion() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      jsonAssertion("[].name")
                  )
          )
      );
    }

    public DslTestPlan jsonAssertionWithMatch() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      jsonAssertion("name", "[].name")
                          .matches("v.l")
                  )
          )
      );
    }

    public DslTestPlan jsonAssertionWithEqualsToJson() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      jsonAssertion("[].name")
                          .equalsToJson("\"val\"")
                  )
          )
      );
    }

    public DslTestPlan jsonAssertionWithEqualsToNull() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      jsonAssertion("[].name")
                          .equalsTo(null)
                  )
          )
      );
    }

    public DslTestPlan jsonAssertionWithNot() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      jsonAssertion("[].name")
                          .not()
                  )
          )
      );
    }

    public DslTestPlan jsonAssertionWithJsonPath() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      jsonAssertion("$[*].name")
                          .queryLanguage(JsonQueryLanguage.JSON_PATH)
                  )
          )
      );
    }

  }

}
