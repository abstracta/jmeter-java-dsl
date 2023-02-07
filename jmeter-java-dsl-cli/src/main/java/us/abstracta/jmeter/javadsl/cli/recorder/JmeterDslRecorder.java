package us.abstracta.jmeter.javadsl.cli.recorder;

import com.blazemeter.jmeter.correlation.core.CorrelationRule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import us.abstracta.jmeter.javadsl.codegeneration.DslCodeGenerator;
import us.abstracta.jmeter.javadsl.core.engines.JmeterEnvironment;

public class JmeterDslRecorder implements AutoCloseable {

  public static final String DEFAULT_EXCLUDED_URLS =
      "(?i).*\\.(bmp|css|js|gif|ico|jpe?g|png|svg|swf|ttf|woff2?|webp)(\\?.*)?";
  public static final List<String> DEFAULT_EXCLUDED_HEADERS = Arrays.asList("Accept-Language",
      "Upgrade-Insecure-Requests", "Accept-Encoding", "User-Agent", "Accept", "Referer", "Origin",
      "X-Requested-With", "Cache-Control");

  private final List<String> urlIncludes = new ArrayList<>();
  private final List<String> urlExcludes = new ArrayList<>(
      Collections.singletonList(DEFAULT_EXCLUDED_URLS));
  private final List<String> headerExcludes = new ArrayList<>(DEFAULT_EXCLUDED_HEADERS);
  private final List<CorrelationRule> correlations = new ArrayList<>();
  private JmeterProxyRecorder proxy;
  private String logsDirectory;

  public JmeterDslRecorder logsDirectory(String logsDirectory) {
    this.logsDirectory = logsDirectory;
    return this;
  }

  public JmeterDslRecorder clearUrlFilter() {
    urlExcludes.clear();
    return this;
  }

  public JmeterDslRecorder urlIncludes(List<String> regexes) {
    urlIncludes.addAll(regexes);
    return this;
  }

  public JmeterDslRecorder urlsExcludes(List<String> regexes) {
    urlExcludes.addAll(regexes);
    return this;
  }

  public JmeterDslRecorder clearHeaderFilter() {
    headerExcludes.clear();
    return this;
  }

  public JmeterDslRecorder headerExcludes(List<String> regexes) {
    headerExcludes.addAll(regexes);
    return this;
  }

  public String getProxy() {
    return "localhost:8888";
  }

  public JmeterDslRecorder start() throws IOException {
    new JmeterEnvironment();
    proxy = new JmeterProxyRecorder()
        .logsDirectory(logsDirectory)
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
          .generateCodeFromJmx(jmx.toFile());
      System.out.println(code);
    } finally {
      jmx.toFile().delete();
    }
  }

}
