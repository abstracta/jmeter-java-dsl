package us.abstracta.jmeter.javadsl.dashboard;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import kg.apc.charting.GraphPanelChart;
import org.assertj.core.api.SoftAssertions;
import org.assertj.swing.data.TableCell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import us.abstracta.jmeter.javadsl.core.listeners.DslVisualizerTest;

public class DashboardVisualizerTest extends DslVisualizerTest {

  @Test
  public void shouldDisplayGraphsAndSummaryWhenRunTestPlanWithDashboard(TestInfo testInfo)
      throws Exception {
    testVisualizerTestPlan(
        testPlan(
            threadGroup(1, 1,
                httpSampler(wiremockUri),
                DashboardVisualizer.dashboardVisualizer()
            )),
        frame -> frame.table().rowCount() > 1,
        frame -> {
          SoftAssertions softly = new SoftAssertions();
          softly.assertThat(frame.robot().finder().findAll(c -> c instanceof GraphPanelChart))
              .hasSize(4);
          softly.assertThat(frame.table().valueAt(TableCell.row(0).column(0)))
              .isEqualTo("HTTP Request");
          softly.assertAll();
        },
        testInfo
    );
  }

}
