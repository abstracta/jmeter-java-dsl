package us.abstracta.jmeter.javadsl.core;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.functions.EvalFunction;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jorphan.collections.HashTree;

/**
 * This class allows to run test plans in an embedded JMeter instance.
 *
 * Additional engines might be implemented in the future to allow running test plans in other ways
 * (e.g.: in BlazeMeter).
 */
public class EmbeddedJmeterEngine {

  public TestPlanStats run(DslTestPlan testPlan) throws IOException {
    Path propsFilePath = Files.createTempFile("jmeter", ".properties");
    try {
      setupJMeterProperties(propsFilePath);
      StandardJMeterEngine engine = new StandardJMeterEngine();
      HashTree rootTree = new HashTree();
      HashTree testPlanTree = testPlan.buildTreeUnder(rootTree);

      TestPlanStats stats = new TestPlanStats();
      addTestStatsCollectorToTree(stats, testPlanTree);
      addTestSummariserToTree(testPlanTree);

      engine.configure(rootTree);
      engine.run();
      return stats;
    } finally {
      propsFilePath.toFile().delete();
    }
  }

  private void setupJMeterProperties(Path propsFilePath) throws IOException {
    propsFilePath.toFile().delete();
    Files.copy(getClass().getResourceAsStream("/jmeter.properties"), propsFilePath);
    JMeterUtils.loadJMeterProperties(propsFilePath.toString());
    JMeterUtils.setProperty("search_paths", getFunctionsJarPath());
    JMeterUtils.setProperty("saveservice_properties", propsFilePath.toString());
  }

  private String getFunctionsJarPath() {
    try {
      return new File(EvalFunction.class.getProtectionDomain().getCodeSource().getLocation()
          .toURI()).getPath();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private void addTestStatsCollectorToTree(TestPlanStats stats, HashTree tree) {
    ResultCollector collector = new ResultCollector();
    Visualizer statsVisualizer = new Visualizer() {

      @Override
      public void add(SampleResult r) {
        stats.addSampleResult(r);
      }

      @Override
      public boolean isStats() {
        return true;
      }

    };
    collector.setListener(statsVisualizer);
    tree.add(collector);
    tree.add(statsVisualizer);
  }

  private void addTestSummariserToTree(HashTree tree) {
    tree.add(new ResultCollector(new Summariser()));
  }

}
