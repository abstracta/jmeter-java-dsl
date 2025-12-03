import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static us.abstracta.jmeter.javadsl.JmeterDsl.responseAssertion;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.websocket.DslWebsocketSampler.webSocketSampler;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslWebsocketSamplerTest {

  @Test
  public void shouldConnectAndEchoMessageWhenWebSocketTestPlanWithEchoServer() throws Exception {

    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            webSocketSampler()
                .connect("wss://ws.postman-echo.com/raw")
                .connectionTimeout("10000")
                .responseTimeout("5000"),
            webSocketSampler()
                .write()
                .requestData("Hello WebSocket Test!")
                .createNewConnection(false),
            webSocketSampler()
                .read()
                .connectionTimeout("10000")
                .createNewConnection(false)
                .responseTimeout("5000")
                .children(
                    responseAssertion()
                        .containsSubstrings("Hello WebSocket Test!")),
            webSocketSampler()
                .disconnect()
                .responseTimeout("1000")
                .statusCode("1000")))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(0);
  }

  @Test
  public void shouldThrowIllegalArgumentExceptionWhenConnectWithInvalidUrl() {
    assertThrows(IllegalArgumentException.class, () -> {
      webSocketSampler().connect("http://localhost:80/test");
    });
  }

  @Test
  public void shouldHandleConnectionFailureWhenConnectToUnavailableServer() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            webSocketSampler()
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
            webSocketSampler()
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
            webSocketSampler()
                .write()
                .requestData("Test message")
                .createNewConnection(false)))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldHandleReadOperationWhenNoPreviousConnection() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            webSocketSampler()
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
            webSocketSampler()
                .disconnect()
                .responseTimeout("1000")
                .statusCode("1000")))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }
}