package us.abstracta.jmeter.javadsl.core.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.resultsTreeVisualizer;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.util.concurrent.TimeUnit;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.JTreeFixture;
import org.assertj.swing.timing.Condition;
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
