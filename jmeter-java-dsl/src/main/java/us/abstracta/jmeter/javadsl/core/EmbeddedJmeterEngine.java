package us.abstracta.jmeter.javadsl.core;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.functions.EvalFunction;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * Allows to run test plans in an embedded JMeter instance.
 */
public class EmbeddedJmeterEngine implements DslJmeterEngine {

  @Override
  public TestPlanStats run(DslTestPlan testPlan) throws IOException {
    try (JMeterEnvironment env = new JMeterEnvironment()) {
      StandardJMeterEngine engine = new StandardJMeterEngine();
      HashTree rootTree = new HashTree();
      HashTree testPlanTree = testPlan.buildTreeUnder(rootTree);

      AggregatingTestPlanStats stats = new AggregatingTestPlanStats();
      addTestStatsCollectorToTree(stats, testPlanTree);
      addTestSummariserToTree(testPlanTree);

      engine.configure(rootTree);
      engine.run();
      return stats;
    }
  }

  private static class JMeterEnvironment implements Closeable {

    private final File propsDir;

    private JMeterEnvironment() throws IOException {
      propsDir = Files.createTempDirectory("jmeter-java-dsl").toFile();
      try {
        setupJMeterProperties(propsDir);
      } catch (IOException | RuntimeException e) {
        FileUtils.deleteDirectory(propsDir);
        throw e;
      }
    }

    private void setupJMeterProperties(File propsDir) throws IOException {
      File propsFile = new File(propsDir, "jmeter.properties");
      propsFile.createNewFile();
      Properties props = JMeterUtils.getProperties(propsFile.toString());
      props.setProperty("search_paths", getFunctionsJarPath());
      props.setProperty("saveservice_properties",
          installPropertiesFile(propsDir, "saveservice.properties"));
      props.setProperty("upgrade_properties",
          installPropertiesFile(propsDir, "upgrade.properties"));
    }

    private String getFunctionsJarPath() {
      try {
        return new File(EvalFunction.class.getProtectionDomain().getCodeSource().getLocation()
            .toURI()).getPath();
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }

    private String installPropertiesFile(File propsDir, String propsFileName)
        throws IOException {
      Path saveServicePropsPath = propsDir.toPath().resolve(propsFileName);
      Files.copy(getClass().getResourceAsStream("/bin/" + propsFileName), saveServicePropsPath);
      return saveServicePropsPath.toString();
    }

    @Override
    public void close() throws IOException {
      FileUtils.deleteDirectory(propsDir);
    }

  }

  private void addTestStatsCollectorToTree(AggregatingTestPlanStats stats, HashTree tree) {
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

  public static void saveTestPlanToJmx(DslTestPlan dslTestPlan, String filePath)
      throws IOException {
    try (JMeterEnvironment env = new JMeterEnvironment();
        FileOutputStream output = new FileOutputStream(filePath)) {
      HashTree tree = new ListedHashTree();
      dslTestPlan.buildTreeUnder(tree);
      SaveService.saveTree(tree, output);
    }
  }

  public static DslTestPlan loadTestPlanFromJmx(String filePath) throws IOException {
    try (JMeterEnvironment env = new JMeterEnvironment()) {
      HashTree tree = SaveService.loadTree(new File(filePath));
      return DslTestPlan.fromTree(tree);
    }
  }

}
