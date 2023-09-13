package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.dummySampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.simpleController;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslSimpleControllerTest extends JmeterDslTest {

  @Test
  public void shouldApplyPostProcessorToSimpleControllerScopedElements() throws Exception {
    String defaultName = "dummy";
    String body = "OK";
    String overridenName = "test";
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            simpleController(
                jsr223PostProcessor(v -> v.prev.setSampleLabel(overridenName)),
                dummySampler(defaultName, body),
                dummySampler(defaultName, body)
            ),
            dummySampler(defaultName, body)
        )
    ).run();
    Map<String, Long> labelsCounts = new HashMap<>();
    labelsCounts.put(overridenName, 2L);
    labelsCounts.put(defaultName, 1L);
    assertThat(extractLabelsCounts(stats)).isEqualTo(labelsCounts);
  }

  private Map<String, Long> extractLabelsCounts(TestPlanStats stats) {
    return stats.labels().stream()
        .collect(Collectors.toMap(l -> l, l -> stats.byLabel(l).samplesCount()));
  }

  @SuppressWarnings("unused")
  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithSimpleController() {
      return testPlan(
          threadGroup(1, 1,
              simpleController(
                  httpSampler("http://localhost")
              )
          )
      );
    }

    public DslTestPlan testPlanWithSimpleControllerWithName() {
      return testPlan(
          threadGroup(1, 1,
              simpleController("test",
                  httpSampler("http://localhost")
              )
          )
      );
    }

  }

}
