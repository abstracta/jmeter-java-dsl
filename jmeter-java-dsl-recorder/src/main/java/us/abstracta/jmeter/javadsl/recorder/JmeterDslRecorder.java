package us.abstracta.jmeter.javadsl.recorder;

import com.blazemeter.jmeter.correlation.core.CorrelationRule;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.codegeneration.DslCodeGenerator;
import us.abstracta.jmeter.javadsl.core.engines.JmeterEnvironment;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;
import us.abstracta.jmeter.javadsl.recorder.correlations.CorrelationRuleBuilder;
import us.abstracta.jmeter.javadsl.util.TestResource;

public class JmeterDslRecorder {

  public static final String DEFAULT_EXCLUDED_URLS =
      "(?i).*\\.(bmp|css|js|gif|ico|jpe?g|png|svg|swf|ttf|woff2?|webp)(\\?.*)?";
  public static final String DEFAULT_EXCLUDED_HEADERS =
      "(?i)(Sec-.*|Accept|Accept-(Language|Encoding)|Upgrade-Insecure-Requests|User-Agent|"
          + "Referer|Origin|X-Requested-With|Cache-Control)";
  private static final Logger LOG = LoggerFactory.getLogger(JmeterDslRecorder.class);

  private final List<Pattern> urlIncludes = new ArrayList<>();
  private final List<Pattern> urlExcludes = new ArrayList<>(
      Collections.singletonList(Pattern.compile(DEFAULT_EXCLUDED_URLS)));
  private final List<Pattern> headerExcludes = new ArrayList<>(
      Collections.singletonList(Pattern.compile(DEFAULT_EXCLUDED_HEADERS)));
  private final List<CorrelationRule> correlations = new ArrayList<>();
  private final int port = findAvailablePort();
  private String recording;
  private File logsDirectory;
  private boolean logFilteredRequests;
  private JmeterProxyRecorder proxy;

  private int findAvailablePort() {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      return serverSocket.getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException("Could not find an available port", e);
    }
  }

  public JmeterDslRecorder logsDirectory(File logsDirectory) {
    this.logsDirectory = logsDirectory;
    return this;
  }

  public JmeterDslRecorder logFilteredRequests(boolean enabled) {
    this.logFilteredRequests = enabled;
    return this;
  }

  public JmeterDslRecorder clearUrlFilter() {
    urlExcludes.clear();
    return this;
  }

  public JmeterDslRecorder urlIncludes(List<Pattern> regexes) {
    urlIncludes.addAll(regexes);
    return this;
  }

  public JmeterDslRecorder urlExcludes(List<Pattern> regexes) {
    urlExcludes.addAll(regexes);
    return this;
  }

  public JmeterDslRecorder clearHeaderFilter() {
    headerExcludes.clear();
    return this;
  }

  public JmeterDslRecorder headerExcludes(List<Pattern> regexes) {
    headerExcludes.addAll(regexes);
    return this;
  }

  public JmeterDslRecorder correlationRule(CorrelationRuleBuilder ruleBuilder) {
    correlations.add(ruleBuilder.build());
    return this;
  }

  public String getProxy() {
    return "localhost:" + port;
  }

  public JmeterDslRecorder start() throws IOException {
    LOG.info("Starting recorder proxy to record flow requests.");
    new JmeterEnvironment();
    proxy = new JmeterProxyRecorder()
        .port(port)
        .logsDirectory(logsDirectory)
        .logFilteredRequests(logFilteredRequests)
        .headerExcludes(headerExcludes)
        .urlIncludes(urlIncludes)
        .urlExcludes(urlExcludes)
        .correlationRules(correlations);
    proxy.startRecording();
    return this;
  }

  public void stop() throws IOException, InterruptedException, TimeoutException {
    LOG.info("Stopping recorder proxy. This may take some time since it needs to wait for all "
        + "requests finish their recording processing (like applying correlation rules).");
    proxy.stopRecording();
    LOG.info("Converting recorded test plan to JMeter DSL.");
    Path jmx = Files.createTempFile("recording", ".jmx");
    try {
      proxy.saveRecordingTo(jmx.toFile());
      recording = new DslCodeGenerator()
          .addBuilders(new JmeterProxyRecorder.CodeBuilder())
          .addDependency(JmeterDsl.class,
              "us.abstracta.jmeter:jmeter-java-dsl:" + getJmeterDslVersion())
          .setBuilderOption(DslHttpSampler.CodeBuilder.PREFER_ENCODED_PARAMS, true)
          .generateCodeFromJmx(jmx.toFile());
    } finally {
      jmx.toFile().delete();
    }
  }

  public String getRecording() {
    return recording;
  }

  private String getJmeterDslVersion() {
    try {
      return new TestResource("us/abstracta/jmeter/javadsl/version.txt").rawContents();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
