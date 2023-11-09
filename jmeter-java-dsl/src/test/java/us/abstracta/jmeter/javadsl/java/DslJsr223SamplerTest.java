package us.abstracta.jmeter.javadsl.java;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.forLoopController;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpHeaders;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223Sampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jtlWriter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testResource;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.transaction;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.listeners.JtlWriter;
import us.abstracta.jmeter.javadsl.java.DslJsr223Sampler.SamplerScript;
import us.abstracta.jmeter.javadsl.java.DslJsr223Sampler.SamplerVars;

public class DslJsr223SamplerTest extends JmeterDslTest {

  private static final String RESULTS_JTL = "results.jtl";

  @Test
  public void shouldGetExpectedSampleResultWhenJsr223SamplerWithGroovyScriptAndCustomResponse(
      @TempDir Path tempDir)
      throws Exception {
    Path resultsFilePath = tempDir.resolve(RESULTS_JTL);
    testPlan(
        threadGroup(1, 1,
            jsr223Sampler("SampleResult.responseCode = '202'; 'Tested'"),
            buildJtlWriter(resultsFilePath)
        )
    ).run();
    assertThatJtlContentIsExpectedForCustomSample(resultsFilePath);
  }

  private JtlWriter buildJtlWriter(Path resultsFilePath) {
    return jtlWriter(resultsFilePath.getParent().toString(),
        resultsFilePath.getFileName().toString())
        .withAllFields(false)
        .saveAsXml(true)
        .withResponseCode(true)
        .withResponseData(true);
  }

  private void assertThatJtlContentIsExpectedForCustomSample(Path resultsFilePath) {
    assertThat(resultsFilePath.toFile())
        .hasSameTextualContentAs(testResource("jtls/custom-sample-jtl.xml").file());
  }

  @Test
  public void shouldGetExpectedSampleResultWhenJsr223SamplerWithLambdaAndCustomResponse(
      @TempDir Path tempDir) throws Exception {
    Path resultsFilePath = tempDir.resolve(RESULTS_JTL);
    testPlan(
        threadGroup(1, 1,
            jsr223Sampler(v -> {
              SampleResult result = v.sampleResult;
              result.setResponseData("Tested", StandardCharsets.UTF_8.name());
              result.setResponseCode("202");
            }),
            buildJtlWriter(resultsFilePath)
        )
    ).run();
    assertThatJtlContentIsExpectedForCustomSample(resultsFilePath);
  }

  public static class MySampler implements SamplerScript, ThreadListener, TestIterationListener,
      LoopIterationListener {

    private static final Map<String, Integer> COUNTS = new ConcurrentHashMap<>();
    private int count;

    @Override
    public void threadStarted() {
      count = 1;
    }

    @Override
    public void testIterationStart(LoopIterationEvent event) {
      count++;
    }

    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
      count++;
    }

    @Override
    public void runScript(SamplerVars vars) {
      count++;
    }

    @Override
    public void threadFinished() {
      COUNTS.put(Thread.currentThread().getName(), count);
    }

  }

  @Test
  public void shouldGetExpectedCountsWhenSamplerWithListeners() throws Exception {
    int threadCount = 2;
    int iterations = 3;
    int loops = 3;
    testPlan(threadGroup(threadCount, iterations,
        forLoopController(loops,
            jsr223Sampler(MySampler.class))))
        .run();
    assertThat(MySampler.COUNTS).isEqualTo(
        buildExpectedThreadCountsMap(threadCount, iterations, loops));
  }

  private Map<String, Integer> buildExpectedThreadCountsMap(int threadCount, int iterations,
      int loops) {
    Map<String, Integer> ret = new HashMap<>();
    for (int i = 0; i < threadCount; i++) {
      ret.put("Thread Group 1-" + (i + 1), 1 + iterations + iterations * loops * 2);
    }
    return ret;
  }

  @Test
  public void shouldNotAlterHttpHeadersWhenSamplerAndHttpHeadersAndSampler() throws Exception {
    String headerNamePrefix = "HEADER";
    String header1Name = headerNamePrefix + "1";
    String header2Name = headerNamePrefix + "2";
    String headerValue = "VALUE";
    testPlan(threadGroup(1, 1,
            httpHeaders().header(header1Name, headerValue),
            transaction("test",
                httpHeaders().header(header2Name, headerValue),
                jsr223Sampler(MySampler.class),
                httpSampler(wiremockUri))
        )
    ).run();
    verify(getRequestedFor(urlEqualTo("/"))
        .withHeader(header1Name, equalTo(headerValue))
        .withHeader(header2Name, equalTo(headerValue)));
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithJsr223Sampler() {
      return testPlan(
          threadGroup(1, 1,
              jsr223Sampler("println 'sample'")
          )
      );
    }

    public DslTestPlan testPlanWithJsr223SamplerAndNonDefaultSettings() {
      return testPlan(
          threadGroup(1, 1,
              jsr223Sampler("console.log(\"sample\")")
                  .language("javascript")
          )
      );
    }

  }

}
