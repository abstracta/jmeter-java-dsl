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

  private final List<String> includingUrls = new ArrayList<>();
  private final List<String> excludingUrls = new ArrayList<>(
      Collections.singletonList(DEFAULT_EXCLUDED_URLS));
  private final List<String> excludingHeaders = new ArrayList<>(DEFAULT_EXCLUDED_HEADERS);
  private final List<CorrelationRule> correlations = new ArrayList<>();
  private JmeterProxyRecorder proxy;
  private String logsDirectory;

  public JmeterDslRecorder logsDirectory(String logsDirectory) {
    this.logsDirectory = logsDirectory;
    return this;
  }

  public JmeterDslRecorder clearUrlsFilter() {
    excludingUrls.clear();
    return this;
  }

  public JmeterDslRecorder urlsMatching(List<String> regexes) {
    includingUrls.addAll(regexes);
    return this;
  }

  public JmeterDslRecorder notUrlsMatching(List<String> regexes) {
    excludingUrls.addAll(regexes);
    return this;
  }

  public JmeterDslRecorder clearHeadersFilter() {
    excludingHeaders.clear();
    return this;
  }

  public JmeterDslRecorder notHeadersMatching(List<String> regexes) {
    excludingHeaders.addAll(regexes);
    return this;
  }

  public String getProxy() {
    return "localhost:8888";
  }

  public JmeterDslRecorder start() throws IOException {
    new JmeterEnvironment();
    proxy = new JmeterProxyRecorder()
        .logsDirectory(logsDirectory)
        .excludeHeaders(excludingHeaders)
        .includeUrls(includingUrls)
        .excludeUrls(excludingUrls)
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
