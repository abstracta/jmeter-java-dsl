package us.abstracta.jmeter.javadsl.recorder;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import org.apache.commons.io.output.NullOutputStream;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeDriverService.Builder;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordingBrowser implements AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(RecordingBrowser.class);
  private static final Duration BROWSER_OPEN_POLL_PERIOD = Duration.ofMillis(500);

  private final ChromeDriver driver;

  public RecordingBrowser(URL url, String recordingProxy, List<String> args) {
    LOG.info("Starting browser. Wait until a browser window appears and use provided browser to "
        + "record the flow.");
    driver = new ChromeDriver(buildDriverService(), buildChromeOptions(recordingProxy, args));
    if (url != null) {
      driver.get(url.toString());
    }
  }

  private ChromeDriverService buildDriverService() {
    ChromeDriverService ret = new Builder().build();
    if (!LOG.isDebugEnabled()) {
      ret.sendOutputTo(NullOutputStream.NULL_OUTPUT_STREAM);
    }
    return ret;
  }

  private ChromeOptions buildChromeOptions(String recordingProxy, List<String> args) {
    ChromeOptions ret = new ChromeOptions();
    Proxy proxy = new Proxy();
    proxy.setHttpProxy(recordingProxy);
    proxy.setSslProxy(recordingProxy);
    ret.setProxy(proxy);
    /*
     remote-allow-origins is required due to this issue:
     https://github.com/SeleniumHQ/selenium/issues/11750
     */
    ret.addArguments("--incognito", "--proxy-bypass-list=<-loopback>", "--remote-allow-origins=*");
    ret.addArguments(args);
    ret.setAcceptInsecureCerts(true);
    return ret;
  }

  public ChromeDriver getDriver() {
    return driver;
  }

  public void awaitClosed() throws InterruptedException {
    try {
      while (true) {
        driver.getWindowHandle();
        Thread.sleep(BROWSER_OPEN_POLL_PERIOD.toMillis());
      }
    } catch (NoSuchWindowException e) {
      LOG.info("Detected browser close");
    }
  }

  public void close() {
    driver.quit();
  }

}
