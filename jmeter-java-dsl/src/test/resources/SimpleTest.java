///usr/bin/env jbang "$0" "$@" ; exit $?
/*
These commented lines make the class executable if you have jbang installed by making file
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
import org.apache.http.entity.ContentType;
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
    TestPlanStats stats = testPlan(
        threadGroup(1, 3,
          httpSampler("http://localhost")
            .post("{\"var\":\"val\"}", ContentType.APPLICATION_JSON),
          httpSampler("http://localhost")
        ),
        jtlWriter("", "results.jtl")
      )
      .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(0);
  }

  /*
   This method is only included to make test class self executable. You can remove it when
   executing tests with maven, gradle or some other tool.
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
