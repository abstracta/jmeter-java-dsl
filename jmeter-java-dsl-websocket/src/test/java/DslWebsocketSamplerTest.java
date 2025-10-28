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


// import eu.luminis.jmeter.wssampler.*;

// import java.net.URISyntaxException;

// import static org.junit.jupiter.api.Assertions.*;

// class DslWebsocketSamplerTest {

//   @Test
//   void shouldParseWsUrlCorrectly() {
//     DslWebsocketSampler.DslConnectSampler sampler = DslWebsocketSampler.connect("ws://localhost:8080/chat");

//     assertEquals("localhost", sampler.server);
//     assertEquals("8080", sampler.port);
//     assertEquals("/chat", sampler.getPath());
//     assertFalse(sampler.tls);
//   }

//   @Test
//   void shouldParseWssUrlWithDefaultPortAndQuery() {
//     DslWebsocketSampler.DslConnectSampler sampler = DslWebsocketSampler.connect("wss://example.com/socket?token=abc");

//     assertEquals("example.com", sampler.server);
//     assertEquals("443", sampler.port);
//     assertEquals("/socket?token=abc", sampler.getPath());
//     assertTrue(sampler.tls);
//   }

//   @Test
//   void shouldThrowExceptionWhenParsingInvalidUrl() {
//     IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
//       DslWebsocketSampler.connect("http://invalid.com");
//     });

//     assertTrue(ex.getMessage().contains("Invalid WebSocket URL"));
//   }

//   @Test
//   void shouldBuildOpenWebSocketSamplerWithAllFields() {
//     DslWebsocketSampler.DslConnectSampler sampler = new DslWebsocketSampler.DslConnectSampler()
//         .server("localhost")
//         .port("9000")
//         .path("/ws")
//         .tls(true)
//         .connectionTimeout("10000")
//         .responseTimeout("5000");

//     OpenWebSocketSampler element = (OpenWebSocketSampler) sampler.buildTestElement();

//     assertEquals("localhost", element.getServer());
//     assertEquals("9000", element.getPort());
//     assertEquals("/ws", element.getPath());
//     assertTrue(element.isTLS());
//     assertEquals("10000", element.getConnectTimeout());
//     assertEquals("5000", element.getReadTimeout());
//   }

//   @Test
//   void shouldBuildCloseWebSocketSamplerWithAllFields() {
//     DslWebsocketSampler.DslDisconnectSampler sampler = new DslWebsocketSampler.DslDisconnectSampler()
//         .responseTimeout("7000")
//         .statusCode("1001");

//     CloseWebSocketSampler element = (CloseWebSocketSampler) sampler.buildTestElement();

//     assertEquals("7000", element.getReadTimeout());
//     assertEquals("1001", element.getStatusCode());
//   }

//   @Test
//   void shouldBuildWriteSamplerWithCustomValues() {
//     DslWebsocketSampler.DslWriteSampler sampler = new DslWebsocketSampler.DslWriteSampler()
//         .connectionTimeout("15000")
//         .requestData("Hello WebSocket")
//         .createNewConnection(true);

//     SingleWriteWebSocketSampler element = (SingleWriteWebSocketSampler) sampler.buildTestElement();

//     assertEquals("15000", element.getConnectTimeout());
//     assertEquals("Hello WebSocket", element.getRequestData());
//     assertTrue(element.isCreateNewConnection());
//   }

//   @Test
//   void shouldBuildReadSamplerWithCustomValues() {
//     DslWebsocketSampler.DslReadSampler sampler = new DslWebsocketSampler.DslReadSampler()
//         .connectionTimeout("12000")
//         .responseTimeout("9000")
//         .createNewConnection(true);

//     SingleReadWebSocketSampler element = (SingleReadWebSocketSampler) sampler.buildTestElement();

//     assertEquals("12000", element.getConnectTimeout());
//     assertEquals("9000", element.getReadTimeout());
//     assertTrue(element.isCreateNewConnection());
//     assertEquals(SingleReadWebSocketSampler.DataType.Text, element.getDataType());
//     assertFalse(element.isOptional());
//   }

//   @Test
//   void shouldThrowExceptionWhenUsingBaseWebsocketSamplerDirectly() {
//     DslWebsocketSampler sampler = DslWebsocketSampler.webSocketSampler();
//     assertThrows(UnsupportedOperationException.class, sampler::buildTestElement);
//   }
// }
