package us.abstracta.jmeter.javadsl.dashboard;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
 * <p>
 * If dashboardVisualizer is added at testPlan level it will show information about all samples in
 * the test plan, if added at thread group level it will only show info for samplers contained
 * within it, if added as a sampler child, then only that sampler samples will be shown.
 *
 * @since 0.23
 */
public class DashboardVisualizer extends DslVisualizer {

  public DashboardVisualizer() {
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
    List<AbstractOverTimeVisualizer> graphs = Arrays.asList(new ThreadsStateOverTimeGui(),
        new ResponseTimesOverTimeGui(),
        new TransactionsPerSecondGui(), new ResponseCodesPerSecondGui());
    graphs.forEach(g -> parent.add(buildGraphTestElement(g, context)));
    SummaryReport summary = new SummaryReport();
    parent.add(buildVisualizerTestElement(summary, v -> v.getComponent(1), 0, context));
    context.addVisualizer(this, () -> buildGui(graphs, summary));
    return parent;
  }

  private TestElement buildGraphTestElement(AbstractOverTimeVisualizer graph,
      BuildTreeContext buildContext) {
    GraphPanelChart graphPanelChart = graph.getGraphPanelChart();
    graphPanelChart.setxAxisLabelRenderer(new RelativeMinutesTimeRenderer());
    // we set this to false to avoid AbstractOverTimeVisualizer overwriting our custom renderer
    graphPanelChart.getChartSettings().setUseRelativeTime(false);
    return buildVisualizerTestElement(graph,
        v -> ((AbstractGraphPanelVisualizer) v).getGraphPanelChart(), 500, buildContext);
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

  public TestElement buildVisualizerTestElement(AbstractVisualizer visualizer,
      Function<AbstractVisualizer, Component> subComponentLocator, int repaintIntervalMillis,
      BuildTreeContext context) {
    AbstractListenerElement testElement = (AbstractListenerElement) visualizer.createTestElement();
    visualizer.configure(testElement);
    Component subComponent = subComponentLocator.apply(visualizer);
    Visualizer rePainter = buildSubComponentRePainter(visualizer, subComponent,
        repaintIntervalMillis);
    testElement.setListener(rePainter);
    /*
    we need to add it to the root context to avoid losing reference and being null while sampling
    due to weak reference of test element listeners
    */
    context.getRoot().getOrCreateEntry(getClass().getSimpleName(), ArrayList::new).add(rePainter);
    return testElement;
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

  @Override
  protected TestElement buildTestElement() {
    return null;
  }

  protected Component buildGui(List<AbstractOverTimeVisualizer> graphs, SummaryReport summary) {
    JSplitPane ret = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildGraphsPanel(graphs),
        buildSummaryPanel(summary));
    ret.setResizeWeight(1.0);
    return ret;
  }

  private JPanel buildGraphsPanel(List<AbstractOverTimeVisualizer> graphs) {
    JPanel graphsPanel = new JPanel();
    graphsPanel.setLayout(new GridLayout(0, 2));
    graphs.forEach(g -> graphsPanel.add(g.getGraphPanelChart()));
    return graphsPanel;
  }

  private Component buildSummaryPanel(SummaryReport summary) {
    return summary.getComponent(1);
  }

  @Override
  public void showTestElementGui(Component guiComponent, Runnable closeListener) {
    showFrameWith(guiComponent, "Dashboard", 1080, 600, closeListener);
  }

  @Override
  public void showInGui() {
    throw new UnsupportedOperationException("Dashboard has no built-in JMeter GUI");
  }

}
