package us.abstracta.jmeter.javadsl.core.samplers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.dummySampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PreProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class DslDummySamplerTest {

  private static final String SAMPLE_START_TIMESTAMP_NAME = "startTimeStamp";
  private static final String PROCESSING_TIME_NAME = "processingTime";
  private static final String RESPONSE_BODY = "TestResponse";
  private static final String RESPONSE_TIME_NAME = "responseTime";

  @Test
  public void shouldGetExpectedResultWhenDummySamplerWithMinimumConfig() throws Exception {
    Map<String, Object> result = new HashMap<>();
    testPlan(
        threadGroup(1, 1,
            dummySampler(RESPONSE_BODY)
                .children(
                    jsr223PreProcessor(s -> s.vars.putObject(SAMPLE_START_TIMESTAMP_NAME,
                        System.currentTimeMillis())),
                    jsr223PostProcessor(s -> result.putAll(
                        buildResultMap(s.prev, (Long) s.vars.getObject(
                            SAMPLE_START_TIMESTAMP_NAME))))
                )
        )
    ).run();
    long responseTime = (long) result.remove(RESPONSE_TIME_NAME);
    long processingTime = (long) result.remove(PROCESSING_TIME_NAME);
    assertThat(result).isEqualTo(
        buildResultMap("jp@gc - Dummy Sampler", "", "", true, "200", "OK", RESPONSE_BODY));
    assertThat(responseTime).isGreaterThanOrEqualTo(50);
    assertThat(responseTime).isLessThanOrEqualTo(500);
    assertThat(processingTime).isLessThan(3000);
  }

  private Map<String, Object> buildResultMap(String label, String url, String requestBody,
      boolean successful, String responseCode, String responseMessage, String responseBody) {
    Map<String, Object> ret = new HashMap<>();
    ret.put("label", label);
    ret.put("url", url);
    ret.put("requestBody", requestBody);
    ret.put("successful", successful);
    ret.put("responseCode", responseCode);
    ret.put("responseMessage", responseMessage);
    ret.put("responseBody", responseBody);
    return ret;
  }

  private Map<String, Object> buildResultMap(SampleResult result, long preTs) {
    Map<String, Object> ret = buildResultMap(result.getSampleLabel(), result.getUrlAsString(),
        result.getSamplerData(), result.isSuccessful(), result.getResponseCode(),
        result.getResponseMessage(), result.getResponseDataAsString());
    ret.put(RESPONSE_TIME_NAME, result.getTime());
    ret.put(PROCESSING_TIME_NAME, System.currentTimeMillis() - preTs);
    return ret;
  }

  @Test
  public void shouldGetExpectedResultWhenDummySamplerWithFullConfig() throws Exception {
    Map<String, Object> result = new HashMap<>();
    String label = "sampler";
    String url = "http://myservice.com";
    String requestBody = "TestRequest";
    boolean successful = false;
    String responseCode = "400";
    String responseMessage = "NOT FOUND";
    Duration responseTime = Duration.ofSeconds(3);
    testPlan(
        threadGroup(1, 1,
            dummySampler(label, RESPONSE_BODY)
                .url(url)
                .requestBody(requestBody)
                .successful(successful)
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .responseTime(responseTime)
                .simulateResponseTime(true)
                .children(
                    jsr223PreProcessor(s -> s.vars.putObject(SAMPLE_START_TIMESTAMP_NAME,
                        System.currentTimeMillis())),
                    jsr223PostProcessor(s -> result.putAll(
                        buildResultMap(s.prev, (Long) s.vars.getObject(
                            SAMPLE_START_TIMESTAMP_NAME))))
                )
        )
    ).run();
    long actualResponseTime = (long) result.remove(RESPONSE_TIME_NAME);
    long processingTime = (long) result.remove(PROCESSING_TIME_NAME);
    assertThat(result).isEqualTo(
        buildResultMap(label, url, requestBody, successful, responseCode, responseMessage,
            RESPONSE_BODY));
    /*
     sometimes measured time may differ for a few milliseconds. Add some threshold to avoid
     fragility.
     */
    long timeThresholdMillis = 10;
    assertThat(actualResponseTime).isGreaterThanOrEqualTo(responseTime.toMillis() - timeThresholdMillis);
    assertThat(processingTime).isGreaterThanOrEqualTo(actualResponseTime - timeThresholdMillis);
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithDummySampler() {
      return testPlan(
          threadGroup(1, 1,
              dummySampler("OK")
                  .responseTime(Duration.ofMillis(100))
          )
      );
    }

    public DslTestPlan testPlanWithDummySamplerAndNonDefaultSettings() {
      return testPlan(
          threadGroup(1, 1,
              dummySampler("mySampler", "OK")
                  .successful(false)
                  .responseCode("404")
                  .responseMessage("NOT_FOUND")
                  .responseTime("${__Random(50, 500)}")
                  .simulateResponseTime(true)
                  .url("http://localhost")
                  .requestBody("TEST")
          )
      );
    }

  }

}
