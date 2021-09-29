package us.abstracta.jmeter.javadsl.dashboard;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import kg.apc.charting.GraphPanelChart;
import kg.apc.jmeter.graphs.AbstractGraphPanelVisualizer;
import kg.apc.jmeter.graphs.AbstractOverTimeVisualizer;
import kg.apc.jmeter.vizualizers.ResponseCodesPerSecondGui;
import kg.apc.jmeter.vizualizers.ResponseTimesOverTimeGui;
import kg.apc.jmeter.vizualizers.ThreadsStateOverTimeGui;
import kg.apc.jmeter.vizualizers.TransactionsPerSecondGui;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.SummaryReport;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.gui.NumberRenderer;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.listeners.DslVisualizer;

/**
 * Shows a popup window including live graphs and stats using JMeter built-in Summary Report and
 * graphs plugins.
 *
 * If dashboardVisualizer is added at testPlan level it will show information about all samples in
 * the test plan, if added at thread group level it will only show info for samplers contained
 * within it, if added as a sampler child, then only that sampler samples will be shown.
 *
 * @since 0.23
 */
public class DashboardVisualizer extends DslVisualizer {

  private DashboardVisualizer() {
    super(null, null);
  }

  public static DashboardVisualizer dashboardVisualizer() {
    return new DashboardVisualizer();
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    if (GraphicsEnvironment.isHeadless()) {
      logNonGuiExecutionWarning();
      return parent;
    }
    Component graphsPanel = buildGraphsPanel(parent, context);
    Component visualizerPanel = buildVisualizersPanel(parent, context, graphsPanel);
    CompletableFuture<Void> closeFuture = new CompletableFuture<>();
    context.addVisualizerCloseFuture(closeFuture);
    showFrameWith(visualizerPanel, "Dashboard", 1080, 600, () -> closeFuture.complete(null));
    return parent;
  }

  private Component buildVisualizersPanel(HashTree parent, BuildTreeContext context,
      Component graphsPanel) {
    Component summaryReportPanel = addVisualizer(new SummaryReport(), v -> v.getComponent(1), 0,
        parent, context);
    summaryReportPanel.setPreferredSize(new Dimension(0, 80));
    return new JSplitPane(JSplitPane.VERTICAL_SPLIT, graphsPanel, summaryReportPanel);
  }

  private JPanel buildGraphsPanel(HashTree parent, BuildTreeContext context) {
    JPanel graphsPanel = new JPanel();
    graphsPanel.setPreferredSize(new Dimension(0, 470));
    graphsPanel.setLayout(new GridLayout(0, 2));
    Arrays.asList(new ThreadsStateOverTimeGui(), new ResponseTimesOverTimeGui(),
            new TransactionsPerSecondGui(), new ResponseCodesPerSecondGui())
        .forEach(g -> graphsPanel.add(addGraph(g, parent, context)));
    return graphsPanel;
  }

  private Component addGraph(AbstractOverTimeVisualizer graph, HashTree parent,
      BuildTreeContext context) {
    GraphPanelChart graphPanelChart = graph.getGraphPanelChart();
    graphPanelChart.setxAxisLabelRenderer(new RelativeMinutesTimeRenderer());
    // we set this to false to avoid AbstractOverTimeVisualizer overwriting our custom renderer
    graphPanelChart.getChartSettings().setUseRelativeTime(false);
    return addVisualizer(graph, v -> ((AbstractGraphPanelVisualizer) v).getGraphPanelChart(), 500,
        parent,
        context);
  }

  private static final class RelativeMinutesTimeRenderer extends NumberRenderer {

    private long firstVal;

    @Override
    public void setValue(Object value) {
      setText(buildTimeString(value));
    }

    private String buildTimeString(Object value) {
      if (value == null) {
        return "";
      } else {
        if (value instanceof Double) {
          value = Math.round((Double) value);
        }
        firstVal = firstVal == 0 ? (long) value : Math.min(firstVal, (long) value);
        Duration time = Duration.ofMillis((long) value - firstVal);
        return String.format("%02d:%02d", time.toMinutes(), time.getSeconds() % 60);
      }
    }

  }

  public Component addVisualizer(AbstractVisualizer visualizer,
      Function<AbstractVisualizer, Component> subComponentLocator, int repaintIntervalMillis,
      HashTree parent, BuildTreeContext context) {
    AbstractListenerElement testElement = (AbstractListenerElement) visualizer.createTestElement();
    visualizer.configure(testElement);
    Component subComponent = subComponentLocator.apply(visualizer);
    Visualizer rePainter = buildSubComponentRePainter(visualizer, subComponent,
        repaintIntervalMillis);
    testElement.setListener(rePainter);
    /*
    we need to add it to context to avoid losing reference and being null while sampling due to
    weak reference of test element listeners
    */
    getRePainters(context).add(rePainter);
    parent.add(testElement);
    return subComponent;
  }

  private Visualizer buildSubComponentRePainter(AbstractVisualizer delegate,
      Component subComponent, long repaintIntervalMillis) {
    return new Visualizer() {

      private long lastRepaint;

      @Override
      public void add(SampleResult sample) {
        delegate.add(sample);
        if (repaintIntervalMillis <= 0) {
          subComponent.repaint();
        } else {
          long now = System.currentTimeMillis();
          if (now - lastRepaint >= repaintIntervalMillis) {
            subComponent.repaint();
            lastRepaint = now;
          }
        }
      }

      @Override
      public boolean isStats() {
        return false;
      }
    };
  }

  private List<Visualizer> getRePainters(BuildTreeContext context) {
    String contextKey = "DASHBOARD_REPAINTERS";
    List<Visualizer> ret = (List<Visualizer>) context.getEntry(contextKey);
    if (ret == null) {
      ret = new ArrayList<>();
      context.setEntry(contextKey, ret);
    }
    return ret;
  }

  @Override
  protected TestElement buildTestElement() {
    return new ResultCollector();
  }

}
