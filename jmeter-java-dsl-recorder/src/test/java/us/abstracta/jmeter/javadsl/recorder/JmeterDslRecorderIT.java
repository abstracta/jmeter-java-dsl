package us.abstracta.jmeter.javadsl.recorder;

import static us.abstracta.jmeter.javadsl.JmeterDsl.testResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.bridge.SLF4JBridgeHandler;
import us.abstracta.jmeter.javadsl.codegeneration.TestClassTemplate;
import us.abstracta.jmeter.javadsl.core.StringTemplateAssert;
import us.abstracta.jmeter.javadsl.recorder.correlations.CorrelationRuleBuilder;
import us.abstracta.jmeter.javadsl.util.TestResource;

public class JmeterDslRecorderIT {

  @BeforeClass
  public static void setupClass() {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  @Test
  public void shouldGetExpectedRecordingWhenRecord() throws Exception {
    try (RetailStoreMock mock = new RetailStoreMock(0)) {
      JmeterDslRecorder recorder = buildRecorder(mock);
      recorder.start();
      try (RecordingBrowser browser = new RecordingBrowser(mock.getUrl(), recorder.getProxy(),
          Collections.singletonList("--headless=new"))) {
        addFirstProductToCart(browser);
      } finally {
        recorder.stop();
      }
      StringTemplateAssert.assertThat(recorder.getRecording())
          .matches(buildExpectedRecordedTestClass());
    }
  }

  private static String buildExpectedRecordedTestClass() throws IOException {
    return new TestClassTemplate()
        .dependencies(Collections.singleton("us.abstracta.jmeter:jmeter-java-dsl:"
            + testResource("us/abstracta/jmeter/javadsl/version.txt").rawContents()))
        .imports(Stream.of(ContentType.class, StandardCharsets.class, HTTPConstants.class)
            .map(Class::getName)
            .collect(Collectors.toSet()))
        .testPlan(new TestResource("RecordedTestPlan.template.java").rawContents())
        .solve();
  }

  private static void addFirstProductToCart(RecordingBrowser browser) {
    new RetailStoreHomePage(browser.getDriver()).addFirstProductToCart();
  }

  private static JmeterDslRecorder buildRecorder(RetailStoreMock mock) {
    return new JmeterDslRecorder()
        .urlIncludes(Collections.singletonList(
            Pattern.compile(mock.getUrl().toString().substring("http://".length()) + ".*")))
        .correlationRule(
            new CorrelationRuleBuilder("productId",
                Pattern.compile("name=\"productId\" value=\"([^\"]+)\""),
                Pattern.compile("productId=(.*)")));
  }

}
