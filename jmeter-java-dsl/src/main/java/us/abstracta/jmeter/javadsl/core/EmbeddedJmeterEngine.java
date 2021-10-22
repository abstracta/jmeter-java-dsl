package us.abstracta.jmeter.javadsl.core;

import java.awt.Component;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import kg.apc.jmeter.timers.functions.TSTFeedback;
import org.apache.commons.io.FileUtils;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.functions.EvalFunction;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jmeter.visualizers.backend.BackendListenerClient;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.listeners.DslVisualizer;

/**
 * Allows running test plans in an embedded JMeter instance.
 *
 * @since 0.1
 */
public class EmbeddedJmeterEngine implements DslJmeterEngine {

  private static final Logger LOG = LoggerFactory.getLogger(EmbeddedJmeterEngine.class);

  @Override
  public TestPlanStats run(DslTestPlan testPlan) throws IOException {
    try (JMeterEnvironment env = new JMeterEnvironment()) {
      StandardJMeterEngine engine = new StandardJMeterEngine();
      HashTree rootTree = new ListedHashTree();
      BuildTreeContext buildContext = new BuildTreeContext(rootTree);
      HashTree testPlanTree = testPlan.buildTreeUnder(rootTree, buildContext);

      AggregatingTestPlanStats stats = new AggregatingTestPlanStats();
      addTestStatsCollectorToTree(stats, testPlanTree);
      addTestSummariserToTree(testPlanTree);

      engine.configure(rootTree);
      List<Future<Void>> closedVisualizers = Collections.emptyList();
      if (!buildContext.getVisualizers().isEmpty()) {
        // this is required for proper visualization of labels and messages from resources bundle
        env.initLocale();
        closedVisualizers = showVisualizers(buildContext.getVisualizers());
      }
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
      awaitAllClosedVisualizers(closedVisualizers);
      return stats;
    }
  }

  private List<Future<Void>> showVisualizers(Map<DslVisualizer, Supplier<Component>> visualizers) {
    return visualizers.entrySet().stream()
        .map(e -> {
          CompletableFuture<Void> closedVisualizer = new CompletableFuture<>();
          e.getKey().showTestElementGui(e.getValue(), () -> closedVisualizer.complete(null));
          return closedVisualizer;
        })
        .collect(Collectors.toList());
  }

  public void awaitAllClosedVisualizers(List<Future<Void>> closedVisualizers) {
    try {
      for (Future<Void> closedVisualizer : closedVisualizers) {
        try {
          closedVisualizer.get();
        } catch (ExecutionException e) {
          LOG.warn("Problem waiting for a visualizer to close", e);
        }
      }
    } catch (InterruptedException e) {
      //just stop waiting for visualizers and reset interrupted flag
      Thread.interrupted();
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
        /*
         include functions and components jars paths so jmeter search methods can find classes in
         such jars
         */
        props.setProperty("search_paths",
            buildJarPathsFromClasses(EvalFunction.class, BackendListenerClient.class,
                TSTFeedback.class));
      } catch (IOException | RuntimeException e) {
        FileUtils.deleteDirectory(homeDir);
        throw e;
      }
    }

    private String buildJarPathsFromClasses(Class<?>... classes) {
      return Arrays.stream(classes)
          .map(this::getClassJarPath)
          .collect(Collectors.joining(";"));
    }

    private String getClassJarPath(Class<?> theClass) {
      try {
        return new File(
            theClass.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
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
          Path targetPath = binDir.toPath().resolve(configBinDir.relativize(p).toString());
          Files.copy(p, targetPath);
        }
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }

    public void saveTree(HashTree tree, FileOutputStream output) throws IOException {
      SaveService.saveTree(tree, output);
    }

    public HashTree loadTree(File file) throws IOException {
      return SaveService.loadTree(file);
    }

    public void initLocale() {
      JMeterUtils.initLocale();
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

}
