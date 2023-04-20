package us.abstracta.jmeter.javadsl.core.configs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class DslCounterTest extends JmeterDslTest {

  @Test
  public void shouldUseIncrementalValuesInRequestWhenCounterInRequest() throws Exception {
    int startingValue = 1;
    testPlan(
        threadGroup(1, 2,
            counter("USER_ID")
                .startingValue(startingValue),
            httpSampler(wiremockUri + "/${USER_ID}")
        )
    ).run();
    verify(getRequestedFor(urlEqualTo("/" + startingValue)));
    verify(getRequestedFor(urlEqualTo("/" + (startingValue + 1))));
  }

}
