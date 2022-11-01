package us.abstracta.jmeter.javadsl.java;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223Sampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jtlWriter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testResource;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.listeners.JtlWriter;

public class DslJsr223SamplerTest {

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
