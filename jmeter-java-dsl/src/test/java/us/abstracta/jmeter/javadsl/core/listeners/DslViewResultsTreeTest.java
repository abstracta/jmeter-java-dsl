package us.abstracta.jmeter.javadsl.core.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.resultsTreeVisualizer;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class DslViewResultsTreeTest extends DslVisualizerTest {

  @Test
  public void shouldShowResultsInWindowWhenRunTestPlanWithViewResultsTree(TestInfo testInfo)
      throws Exception {
    testVisualizerTestPlan(
        testPlan(
            threadGroup(1, 1,
                httpSampler(wiremockUri)
            ),
            resultsTreeVisualizer()
        ),
        frame -> frame.tree().target().getRowCount() > 0,
        frame -> assertThat(frame.tree().valueAt(0)).isEqualTo("HTTP Request"), testInfo);
  }

}
