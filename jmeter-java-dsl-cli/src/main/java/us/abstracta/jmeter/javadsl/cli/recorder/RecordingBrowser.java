package us.abstracta.jmeter.javadsl.cli.recorder;

import java.net.URL;
import java.time.Duration;
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

  public RecordingBrowser(URL url, String proxyHost) {
    driver = new ChromeDriver(buildDriverService(),
        buildChromeOptions(proxyHost));
      if (url != null) {
        driver.get(url.toString());
      }
  }

  private ChromeDriverService buildDriverService() {
    ChromeDriverService ret = new Builder().build();
    ret.sendOutputTo(NullOutputStream.NULL_OUTPUT_STREAM);
    return ret;
  }

  private ChromeOptions buildChromeOptions(String proxyHost) {
    ChromeOptions ret = new ChromeOptions();
    Proxy proxy = new Proxy();
    proxy.setHttpProxy(proxyHost);
    proxy.setSslProxy(proxyHost);
    ret.setProxy(proxy);
    ret.setAcceptInsecureCerts(true);
    return ret;
  }

  public void awaitClosed() throws InterruptedException {
    try {
      while (true) {
        driver.getWindowHandle();
        Thread.sleep(BROWSER_OPEN_POLL_PERIOD.toMillis());
      }
    } catch (NoSuchWindowException e) {
      LOG.debug("Detected window close", e);
    }
  }

  public void close() {
    driver.quit();
  }

}
