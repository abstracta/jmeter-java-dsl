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
                .requestData("Hello WebSocket Test!"),
            webSocketSampler()
                .read()
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
  public void shouldErrorSamplerWhenConnectToUnavailableServer() throws Exception {
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
  public void shouldErrorSamplerWhenConnectWithVeryShortTimeout() throws Exception {
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
  public void shouldErrorSamplerWhenWriteOperationWhenNoPreviousConnection() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            webSocketSampler()
                .write()
                .requestData("Test message")))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldErrorSamplerWhenReadOperationWhenNoPreviousConnection() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            webSocketSampler()
                .read()
                .responseTimeout("1000")))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldErrorSamplerWhenDisconnectOperationWhenNoPreviousConnection() throws Exception {
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