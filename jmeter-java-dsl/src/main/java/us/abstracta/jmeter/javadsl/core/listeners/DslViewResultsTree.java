package us.abstracta.jmeter.javadsl.core.listeners;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.CompletableFuture;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.BaseTestElement;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.MultiLevelTestElement;

/**
 * Shows a popup window including live results tree using JMeter built-in View Results Tree
 * element.
 *
 * @since 0.19
 */
public class DslViewResultsTree extends BaseTestElement implements MultiLevelTestElement {

  private static final Logger LOG = LoggerFactory.getLogger(DslViewResultsTree.class);
  private static final String MAX_RESULTS_PROPERTY_NAME = "view.results.tree.max_results";
  private int resultsLimit;

  public DslViewResultsTree() {
    super("View Results Tree", ViewResultsFullVisualizer.class);
  }

  /**
   * Specifies the maximum number of sample results to show.
   *
   * When the limit is reached, only latest sample results are shown.
   *
   * Take into consideration that the greater the number of displayed results, the more system
   * memory is required, which might cause an OutOfMemoryError depending on JVM settings.
   *
   * @param resultsLimit the maximum number of sample results to show. When not set the default
   * value is 500.
   * @return the dsl element to allow using it in fluent API style.
   */
  public DslViewResultsTree resultsLimit(int resultsLimit) {
    this.resultsLimit = resultsLimit;
    return this;
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    if (GraphicsEnvironment.isHeadless()) {
      LOG.warn("The test plan contains a View Results Tree which is of no use in non GUI "
          + "executions (like this one). Ignoring it for this execution. Remember removing them "
          + "once your test plan is ready for load testing execution.");
      return parent;
    }
    TestElement testElement = buildConfiguredTestElement();
    CompletableFuture<Void> closeFuture = new CompletableFuture<>();
    context.addVisualizerCloseFuture(closeFuture);
    int prevLimit = JMeterUtils.getPropDefault(MAX_RESULTS_PROPERTY_NAME, 500);
    JMeterUtils.setProperty(MAX_RESULTS_PROPERTY_NAME, String.valueOf(resultsLimit));
    showTestElementInGui(testElement, () -> closeFuture.complete(null));
    JMeterUtils.setProperty(MAX_RESULTS_PROPERTY_NAME, String.valueOf(prevLimit));
    return parent.add(testElement);
  }

  @Override
  protected TestElement buildTestElement() {
    return new ResultCollector();
  }

}
