package us.abstracta.jmeter.javadsl.jmx2dsl;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testResource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.bridge.SLF4JBridgeHandler;
import us.abstracta.jmeter.javadsl.codegeneration.TestClassTemplate;
import us.abstracta.jmeter.javadsl.core.StringTemplateAssert;
import us.abstracta.jmeter.javadsl.util.TestResource;

public class CliCommandsIT {

  @Test
  public void shouldGetConvertedFileWhenConvert() throws Exception {
    Process p = startCommand("jmx2dsl", resourcePath("test-plan.jmx"));
    assertThat(getProcessOutput(p))
        .isEqualTo(buildExpectedTestClass("TestPlan.java", ContentType.class));
  }

  private static String resourcePath(String resourcePath) {
    return new TestResource(resourcePath).filePath();
  }

  private Process startCommand(String command, String... args) throws IOException {
    ProcessBuilder ret = new ProcessBuilder()
        .command("java", "-jar", "target/jmdsl.jar", command);
    ret.command().addAll(Arrays.asList(args));
    return ret.start();
  }

  private String getProcessOutput(Process p) throws IOException, InterruptedException {
    String ret = inputStream2String(p.getInputStream()) + inputStream2String(p.getErrorStream());
    p.waitFor();
    return ret;
  }

  private String inputStream2String(InputStream inputStream) throws IOException {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream))) {
      StringBuilder builder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
        builder.append(System.getProperty("line.separator"));
      }
      return builder.toString();
    }
  }

  private static String buildExpectedTestClass(String template, Class<?>... dependencies)
      throws IOException {
    return new TestClassTemplate()
        .dependencies(Collections.singleton("us.abstracta.jmeter:jmeter-java-dsl:"
            + testResource("version.txt").contents()))
        .imports(Arrays.stream(dependencies).map(Class::getName).collect(Collectors.toSet()))
        .testPlan(new TestResource(template).contents())
        .solve() + "\n";
  }

  @Test
  public void shouldGetExpectedRecordingWhenRecord() throws Exception {
    WireMockServer mock = startRetailStoreMock();
    try {
      int browserDebuggingPort = 8087;
      Process p = startCommand("recorder", "--config=" + resourcePath("retailstore.jmdsl.yml"),
          "--browser-arguments=--headless=new,--remote-debugging-port=" + browserDebuggingPort);
      addProductToCart(browserDebuggingPort);
      StringTemplateAssert.assertThat(getProcessOutput(p))
          .matches(buildExpectedTestClass("RecordedTestPlan.template.java", ContentType.class,
              StandardCharsets.class, HTTPConstants.class));
    } finally {
      mock.stop();
    }
  }

  private static WireMockServer startRetailStoreMock() {
    WireMockServer ret = new WireMockServer(wireMockConfig()
        .withRootDirectory("src/test/resources/retailstore-mock")
        .extensions(new ResponseTemplateTransformer(false))
        .port(8088));
    ret.start();
    return ret;
  }

  private void addProductToCart(int browserDebuggingPort) {
    setupLog4jSlf4jBridge();
    ChromeDriver driver = buildDriverForBrowserOnPort(browserDebuggingPort);
    try {
      clickButton(findFirstProductCartButton(driver), driver);
    } finally {
      driver.close();
    }
  }

  private static void setupLog4jSlf4jBridge() {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  private static ChromeDriver buildDriverForBrowserOnPort(int browserDebuggingPort) {
    ChromeOptions opts = new ChromeOptions();
    opts.setExperimentalOption("debuggerAddress", "127.0.0.1:" + browserDebuggingPort);
    return new ChromeDriver(opts);
  }

  private static WebElement findFirstProductCartButton(ChromeDriver driver) {
    By productsLocator = By.cssSelector(".product");
    new WebDriverWait(driver, Duration.ofSeconds(10))
        .until(ExpectedConditions.numberOfElementsToBeMoreThan(productsLocator, 0));
    List<WebElement> products = driver.findElements(productsLocator);
    return products.get(0)
        .findElement(By.xpath("//a[contains(.,'Add to cart')]"));
  }

  private static void clickButton(WebElement button, JavascriptExecutor driver) {
    /*
     for some reason can't use element.click because we get: Element is not clickable at point
     (x, y). So using javascript is a way to make it work
     */
    driver.executeScript("arguments[0].click()", button);
  }

}
