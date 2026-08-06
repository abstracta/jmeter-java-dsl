import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.websocket.WebsocketJMeterDsl.*;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

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
            websocketWrite("Hello WebSocket Test!"),
            websocketRead()
                .children(
                    responseAssertion()
                        .containsSubstrings("Hello WebSocket Test!")),
            websocketDisconnect()
          ))
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
            websocketWrite("Test message")))
        .run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldErrorSamplerWhenReadOperationWhenNoPreviousConnection() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            websocketRead()))
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

  private static class WebSocketEchoServer extends WebSocketServer {

    private final CountDownLatch startLatch = new CountDownLatch(1);

    WebSocketEchoServer(int port) {
      super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
      conn.send(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
      ex.printStackTrace();
    }

    @Override
    public void onStart() {
      startLatch.countDown();
    }

    void awaitStart(long timeout, TimeUnit unit) throws InterruptedException {
      if (!startLatch.await(timeout, unit)) {
        throw new RuntimeException("WebSocket server failed to start within timeout");
      }
    }

    String getUri() {
      return "ws://localhost:" + getPort();
    }
  }
}