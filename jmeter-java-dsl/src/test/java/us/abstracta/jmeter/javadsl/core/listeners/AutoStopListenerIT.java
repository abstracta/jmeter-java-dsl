package us.abstracta.jmeter.javadsl.core.listeners;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.lessThan;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static us.abstracta.jmeter.javadsl.JmeterDsl.autoStop;
import static us.abstracta.jmeter.javadsl.JmeterDsl.constantTimer;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.engines.AutoStoppedTestException;
import us.abstracta.jmeter.javadsl.core.listeners.AutoStopListener.AutoStopCondition;

public class AutoStopListenerIT extends JmeterDslTest {

  @BeforeEach
  public void setUp() {
    WireMock.stubFor(any(anyUrl())
        .willReturn(aResponse().withStatus(400)));
  }

  @Test
  public void shouldThrowExceptionWhenAutoStopConditionIsMet() {
    assertThrows(AutoStoppedTestException.class, () ->
        testPlan(
            threadGroup(1, 3,
                httpSampler(wiremockUri),
                autoStop()
                    .when(AutoStopCondition.errors().total().greaterThan(0L))
            )
        ).run());
  }

  @Test
  public void shouldStopTestPlanWhenAutoStopConditionIsMet() throws Exception {
    int samples = 3;
    try {
      testPlan(
          threadGroup(1, samples,
              httpSampler(wiremockUri),
              constantTimer(Duration.ofSeconds(1)),
              autoStop()
                  .when(AutoStopCondition.errors().total().greaterThan(0L))
          )
      ).run();
    } catch (AutoStoppedTestException e) {
    }
    verify(lessThan(samples), getRequestedFor(anyUrl()));
  }

}
