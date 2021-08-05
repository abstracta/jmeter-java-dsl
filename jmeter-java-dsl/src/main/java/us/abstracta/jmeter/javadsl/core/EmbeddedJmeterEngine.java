package us.abstracta.jmeter.javadsl.core;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
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
 * Allows running test plans in an embedded JMeter instance.
 */
public class EmbeddedJmeterEngine implements DslJmeterEngine {

  @Override
  public TestPlanStats run(DslTestPlan testPlan) throws IOException {
    try (JMeterEnvironment env = new JMeterEnvironment()) {
      StandardJMeterEngine engine = new StandardJMeterEngine();
      HashTree rootTree = new ListedHashTree();
      HashTree testPlanTree = testPlan.buildTreeUnder(rootTree, new BuildTreeContext(rootTree));

      AggregatingTestPlanStats stats = new AggregatingTestPlanStats();
      addTestStatsCollectorToTree(stats, testPlanTree);
      addTestSummariserToTree(testPlanTree);

      engine.configure(rootTree);
      /*
       we register the start and end of test since calculating it from sample results may be
       inaccurate when timers or post processors are used outside of transactions, since such time
       is not included in sample results. Additionally, we want to provide a consistent meaning for
       start, end and elapsed time for samplers, transactions and test plan (which would not be if
       we only use sample results times).
       */
      stats.setStart(Instant.now());
      engine.run();
      stats.setEnd(Instant.now());
      return stats;
    }
  }

  public static class JMeterEnvironment implements Closeable {

    private final File homeDir;

    public JMeterEnvironment() throws IOException {
      homeDir = Files.createTempDirectory("jmeter-java-dsl").toFile();
      try {
        JMeterUtils.setJMeterHome(homeDir.getPath());
        File binDir = new File(homeDir, "bin");
        installConfig(binDir);
        Properties props = JMeterUtils
            .getProperties(new File(binDir, "jmeter.properties").getPath());
        props.setProperty("search_paths", getFunctionsJarPath());
      } catch (IOException | RuntimeException e) {
        FileUtils.deleteDirectory(homeDir);
        throw e;
      }
    }

    private String getFunctionsJarPath() {
      try {
        return new File(EvalFunction.class.getProtectionDomain().getCodeSource().getLocation()
            .toURI()).getPath();
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }

    private void installConfig(File binDir) throws IOException {
      try (FileSystem fs = FileSystems
          .newFileSystem(getClass().getResource("/bin/jmeter.properties").toURI(),
              Collections.emptyMap())) {
        Path configBinDir = fs.getPath("/bin");
        for (Path p : (Iterable<Path>) Files.walk(configBinDir)::iterator) {
          Files.copy(p, binDir.toPath().resolve(configBinDir.relativize(p).toString()));
        }
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void close() throws IOException {
      FileUtils.deleteDirectory(homeDir);
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
      dslTestPlan.buildTreeUnder(tree, new BuildTreeContext(tree));
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
