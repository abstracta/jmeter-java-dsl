package us.abstracta.jmeter.javadsl.parallel;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.parallel.ParallelController.parallelController;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class ParallelControllerTest extends JmeterDslTest {

  @Test
  public void shouldExecuteRequestsInParallelWhenRequestsInsideParallelController()
      throws Exception {
    int responseTimeMillis = 3000;
    wiremockServer.stubFor(WireMock.any(WireMock.anyUrl())
        .willReturn(WireMock.aResponse().withFixedDelay(responseTimeMillis)));
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            parallelController(
                httpSampler(wiremockUri),
                httpSampler(wiremockUri)
            )
        )
    ).run();
    assertThat(stats.duration()).isLessThan(Duration.ofMillis(responseTimeMillis * 2));

  }

}
