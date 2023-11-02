package us.abstracta.jmeter.javadsl.dashboard;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import kg.apc.charting.GraphPanelChart;
import kg.apc.jmeter.graphs.AbstractOverTimeVisualizer;
import org.apache.jmeter.visualizers.SummaryReport;
import org.apache.jorphan.collections.HashTree;
import org.assertj.core.api.SoftAssertions;
import org.assertj.swing.data.TableCell;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.listeners.DslVisualizerTest;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
    @Test
    public void testShowTestElementGui() {
        DashboardVisualizer dashboardVisualizer = new DashboardVisualizer();
        JPanel panel = new JPanel();
        Runnable closeListener = () -> System.out.println("Closing");
        dashboardVisualizer.showTestElementGui(panel, closeListener);
    }
    @Test
    public void testShowInGui() {
        DashboardVisualizer dashboardVisualizer = new DashboardVisualizer();
        try {
            dashboardVisualizer.showInGui();
        } catch (UnsupportedOperationException e) {
            System.out.println("Expected exception thrown: " + e.getMessage());
        }
    }

}
