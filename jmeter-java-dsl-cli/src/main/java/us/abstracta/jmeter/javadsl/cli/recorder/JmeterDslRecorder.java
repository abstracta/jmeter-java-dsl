package us.abstracta.jmeter.javadsl.cli.recorder;

import com.blazemeter.jmeter.correlation.core.CorrelationRule;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.cli.Cli.ManifestVersionProvider;
import us.abstracta.jmeter.javadsl.codegeneration.DslCodeGenerator;
import us.abstracta.jmeter.javadsl.core.engines.JmeterEnvironment;

public class JmeterDslRecorder implements AutoCloseable {

  public static final String DEFAULT_EXCLUDED_URLS =
      "(?i).*\\.(bmp|css|js|gif|ico|jpe?g|png|svg|swf|ttf|woff2?|webp)(\\?.*)?";
  public static final String DEFAULT_EXCLUDED_HEADERS =
      "(?i)(Sec-.*|Accept|Accept-(Language|Encoding)|Upgrade-Insecure-Requests|User-Agent|"
          + "Referer|Origin|X-Requested-With|Cache-Control)";

  private final List<Pattern> urlIncludes = new ArrayList<>();
  private final List<Pattern> urlExcludes = new ArrayList<>(
      Collections.singletonList(Pattern.compile(DEFAULT_EXCLUDED_URLS)));
  private final List<Pattern> headerExcludes = new ArrayList<>(
      Collections.singletonList(Pattern.compile(DEFAULT_EXCLUDED_HEADERS)));
  private final List<CorrelationRule> correlations = new ArrayList<>();
  private File logsDirectory;
  private boolean logFilteredRequests;
  private JmeterProxyRecorder proxy;

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

  public JmeterDslRecorder urlsExcludes(List<Pattern> regexes) {
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
    return "localhost:8888";
  }

  public JmeterDslRecorder start() throws IOException {
    new JmeterEnvironment();
    proxy = new JmeterProxyRecorder()
        .logsDirectory(logsDirectory)
        .logFilteredRequests(logFilteredRequests)
        .headerExcludes(headerExcludes)
        .urlIncludes(urlIncludes)
        .urlExcludes(urlExcludes)
        .correlationRules(correlations);
    proxy.startRecording();
    return this;
  }

  @Override
  public void close() throws Exception {
    stop();
  }

  public void stop() throws InterruptedException, TimeoutException, IOException {
    proxy.stopRecording();
    Path jmx = Files.createTempFile("recording", ".jmx");
    try {
      proxy.saveRecordingTo(jmx.toFile());
      String code = new DslCodeGenerator()
          .addBuilders(new JmeterProxyRecorder.CodeBuilder())
          .addDependency(JmeterDsl.class,
              "us.abstracta.jmeter:jmeter-java-dsl" + getJmeterDslVersion())
          .generateCodeFromJmx(jmx.toFile());
      System.out.println(code);
    } finally {
      jmx.toFile().delete();
    }
  }

  private String getJmeterDslVersion() {
    try {
      String ret = new ManifestVersionProvider().getVersion()[0];
      return (ret != null ? ":" + ret : "");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

}
