import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static us.abstracta.jmeter.javadsl.JmeterDsl.responseAssertion;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.*;

import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.websocket.*;

public class DslWebsocketSamplerTest {

  @Test
  public void shouldConnectAndEchoMessageWhenWebSocketTestPlanWithEchoServer() throws Exception {

    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            DslWebsocketSampler
                .connect("wss://ws.postman-echo.com/raw")
                .connectionTimeout("10000")
                .responseTimeout("5000"),
            DslWebsocketSampler
                .write()
                .requestData("Hello WebSocket Test!")
                .createNewConnection(false),
            DslWebsocketSampler
                .read()
                .connectionTimeout("10000")
                .createNewConnection(false)
                .responseTimeout("5000")
                .children(
                    responseAssertion()
                        .containsSubstrings("Hello WebSocket Test!")
                ),
            DslWebsocketSampler
                .disconnect()
                .responseTimeout("1000")
                .statusCode("1000")))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(0);
  }
  
  @Test
  public void shouldThrowIllegalArgumentExceptionWhenConnectWithInvalidUrl() {
    assertThrows(IllegalArgumentException.class, () -> {
      DslWebsocketSampler.connect("http://localhost:80/test");
    });
  }

  @Test
  public void shouldHandleConnectionFailureWhenConnectToUnavailableServer() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            DslWebsocketSampler
                .connect("ws://localhost:9999/nonexistent")
                .connectionTimeout("2000")
                .responseTimeout("1000")))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldHandleTimeoutWhenConnectWithVeryShortTimeout() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            DslWebsocketSampler
                .connect("ws://localhost:8080/test")
                .connectionTimeout("1")
                .responseTimeout("1")))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldHandleWriteOperationWhenNoPreviousConnection() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            DslWebsocketSampler
                .write()
                .requestData("Test message")
                .createNewConnection(false)
        )).run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldHandleReadOperationWhenNoPreviousConnection() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            DslWebsocketSampler
                .read()
                .createNewConnection(false)
                .responseTimeout("1000")))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldHandleDisconnectOperationWhenNoPreviousConnection() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            DslWebsocketSampler
                .disconnect()
                .responseTimeout("1000")
                .statusCode("1000")))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }
}