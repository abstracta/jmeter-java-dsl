///usr/bin/env jbang "$0" "$@" ; exit $?
/*
These commented lines make the class executable if you have jbang installed by making the file
executable (eg: chmod +x ./PerformanceTest.java) and just executing it with ./PerformanceTest.java
*/
//DEPS org.assertj:assertj-core:3.23.1
//DEPS org.junit.jupiter:junit-jupiter-engine:5.9.0
//DEPS org.junit.platform:junit-platform-launcher:1.9.0
//DEPS us.abstracta.jmeter:jmeter-java-dsl

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void test() throws IOException {
    TestPlanStats stats = testPlan()
        .tearDownOnlyAfterMainThreadsDone()
        .children(
          vars()
            .set("host", "abstracta.us")
            .set("scheme", "https"),
          httpDefaults()
            .url("https://abstracta.us"),
          httpCache()
            .disable(),
          threadGroup(1, 1,
            transaction("/-66",
              httpSampler("/-66", "/")
                .port(443)
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "none")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("DNT", "1")
                .header("Sec-Fetch-User", "?1")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:97.0) Gecko/20100101 Firefox/97.0")
                .header("Sec-Fetch-Dest", "document")
                .encoding(StandardCharsets.UTF_8)
            ),
            transaction("/solutions/software-testing-107",
              httpSampler("/solutions/software-testing-107", "/solutions/software-testing")
                .port(443)
                .header("Sec-Fetch-Mode", "navigate")
                .header("Referer", "${scheme}://${host}/")
                .header("Sec-Fetch-Site", "same-origin")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("DNT", "1")
                .header("Sec-Fetch-User", "?1")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:97.0) Gecko/20100101 Firefox/97.0")
                .header("Sec-Fetch-Dest", "document")
                .encoding(StandardCharsets.UTF_8)
            )
          ),
          resultsTreeVisualizer()
        )
      .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(0);
  }

  /*
   This method is only included to make the test class self-executable. You can remove it when
   executing tests with maven, gradle, or some other tool.
   */
  public static void main(String[] args) {
    SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
    LauncherFactory.create()
        .execute(LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(PerformanceTest.class))
                .build(),
            summaryListener);
    TestExecutionSummary summary = summaryListener.getSummary();
    summary.printFailuresTo(new PrintWriter(System.err));
    System.exit(summary.getTotalFailureCount() > 0 ? 1 : 0);
  }

}
