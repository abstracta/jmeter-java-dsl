package us.abstracta.jmeter.javadsl.core.engines;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import kg.apc.jmeter.samplers.DummySampler;
import kg.apc.jmeter.timers.functions.TSTFeedback;
import org.apache.jmeter.functions.EvalFunction;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.BackendListenerClient;
import org.apache.jorphan.collections.HashTree;

/**
 * Allows configuring a local JMeter environment required for getting resource messages, running
 * test plans, saving test plans, etc.
 *
 * @since 0.29
 */
public class JmeterEnvironment {

  private static final String BIN_DIR = "bin";
  private static final String JMETER_PROPS_FILE_NAME = "jmeter.properties";

  public JmeterEnvironment() throws IOException {
    File homeDir = Files.createTempDirectory("jmeter-java-dsl").toFile();
    homeDir.deleteOnExit();
    JMeterUtils.setJMeterHome(homeDir.getPath());
    File binDir = new File(homeDir, BIN_DIR);
    binDir.deleteOnExit();
    installConfig(binDir);
    JMeterUtils.getProperties(new File(binDir, JMETER_PROPS_FILE_NAME).getPath());
  }

  public void updateSearchPath(HashTree tree) {
    Set<Class<?>> classes = findTestElementClasses(tree);
    /*
    This is required since test plans don't directly reference function classes which are in a
    separate jar
     */
    classes.add(EvalFunction.class);
    /*
    This is required since JMeter loads functions in static instance only once, and if a test does
    not use this function but a later one does, then the function will not be loaded for later one.
     */
    classes.add(TSTFeedback.class);
    /*
    This is required since JMeter GUI breaks when it doesn't find a loaded timer, assertion, etc.
    And BackendListenerClient components jar contains main JMeter components which avoid this issue.
     */
    classes.add(BackendListenerClient.class);
    /*
    This is required since no sampler is included in core jmeter package that has no order,
    and such condition breaks JMeter GUI
     */
    classes.add(DummySampler.class);
    Set<String> jarPaths = classes.stream()
        .map(this::getClassJarPath)
        .collect(Collectors.toSet());
    JMeterUtils.getJMeterProperties().setProperty("search_paths", String.join(";", jarPaths));
  }

  private Set<Class<?>> findTestElementClasses(HashTree tree) {
    Set<Class<?>> ret = new HashSet<>();
    for (Object elem : tree.list()) {
      ret.add(elem.getClass());
      ret.addAll(findTestElementClasses(tree.getTree(elem)));
    }
    return ret;
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
    Pattern whiteListPattern = Pattern.compile(
        "/" + BIN_DIR + "(?:/(?:report-template.*|.*\\.properties))?");
    try (FileSystem fs = FileSystems
        .newFileSystem(getClass().getResource("/" + BIN_DIR + "/" + JMETER_PROPS_FILE_NAME).toURI(),
            Collections.emptyMap())) {
      Path configBinDir = fs.getPath("/" + BIN_DIR);
      for (Path p : (Iterable<Path>) Files.walk(configBinDir)::iterator) {
        if (whiteListPattern.matcher(p.toString()).matches()) {
          Path targetPath = binDir.toPath().resolve(configBinDir.relativize(p).toString());
          Files.copy(p, targetPath);
          targetPath.toFile().deleteOnExit();
        }
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

}
