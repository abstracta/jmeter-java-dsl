import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.websocket.WebsocketJMeterDsl.*;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.samplers.DslSampler;

public class DslWebsocketSamplerTest {

  @Test
  public void shouldConnectAndEchoMessageWhenWebSocketTestPlanWithEchoServer() throws Exception {
    WebSocketEchoServer echoServer = new WebSocketEchoServer(0);
    echoServer.start();
    echoServer.awaitStart(5, TimeUnit.SECONDS);
    String wsUri = echoServer.getUri();
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
          vars().set("stream_key", "1234567890"),
            websocketConnect(wsUri + "/test?stream_key=${stream_key}"),
            websocketWrite("Hello WebSocket Test!", "text"),
            websocketRead("text")
                .children(
                    responseAssertion()
                        .containsSubstrings("Hello WebSocket Test!")),
            websocketDisconnect()))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(0);
  }

  @Test
  public void shouldThrowIllegalArgumentExceptionWhenConnectWithInvalidUrl() {
    assertThrows(IllegalArgumentException.class, () -> {
      websocketConnect("http://localhost:80/test");
    });
  }

  @Test
  public void shouldErrorSamplerWhenConnectToUnavailableServer() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            websocketConnect("ws://localhost:9999/nonexistent")))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldErrorSamplerWhenConnectWithVeryShortTimeout() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            websocketConnect("ws://localhost:8080/test")
                .connectionTimeout(1)
                .responseTimeout(1)))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldErrorSamplerWhenWriteOperationWhenNoPreviousConnection() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            websocketWrite("Test message", "text")))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldErrorSamplerWhenReadOperationWhenNoPreviousConnection() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            websocketRead("text")))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldErrorSamplerWhenDisconnectOperationWhenNoPreviousConnection() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            websocketDisconnect()))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }
}