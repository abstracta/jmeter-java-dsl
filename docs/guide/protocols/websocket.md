### WebSocket

The `DslWebsocketSampler` class provides a Java DSL for creating WebSocket performance tests using JMeter. It supports the full WebSocket lifecycle including connection, data transmission, and disconnection operations. It is based on [WebSocket Samplers by Peter Doornbosch](https://bitbucket.org/pjtr/jmeter-websocket-samplers/src/master/) plugin.

To use it, add the following dependency to your project:

:::: code-group
::: code-group-item Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-websocket</artifactId>
  <version>2.2.1</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-websocket:2.2.1'
```
:::
::::



Below you can see a basic usage example of WebSocket protocol.

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.websocket.WebsocketJMeterDsl.*;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class Test {
    public static void main(String[] args) throws Exception {
        TestPlanStats stats = testPlan(
    threadGroup(1, 1,
        websocketConnect("wss://ws.postman-echo.com/raw"),
        websocketWrite("Hello WebSocket!"),
        websocketRead()
            .children(
                responseAssertion()
                    .equalsToStrings("Hello WebSocket!")
            ),
        websocketDisconnect()
        )
        ).run();
    }
}
```

::: tip
You can use JMeter variables or functions in the connect URL parts (host, port, path or query). Prefer this over putting the whole URL in a single variable:

```java
vars()
    .set("HOST", "ws.postman-echo.com")
    .set("PORT", "443")
    .set("TOKEN", "abc123"),
websocketConnect("wss://${HOST}:${PORT}/raw?token=${TOKEN}")
```

You can also use a Java variable when the full URL is known when building the test plan:

```java
String wsUrl = "wss://ws.postman-echo.com/raw";
websocketConnect(wsUrl)
```
:::

::: warning
Only `ws://` and `wss://` protocols are supported. Using any other scheme will throw an `IllegalArgumentException`.
:::

::: warning
A full URL as a single JMeter expression (for example `websocketConnect("${URL}")`) is not supported. The underlying WebSocket plugin requires separate server, port, path and TLS fields, and the DSL parses the URL at build time to fill them. Without a literal `ws://` or `wss://` scheme in the string, those fields cannot be set correctly. Use expressions in URL parts (for example `ws://${HOST}:${PORT}/path`) instead.
:::

::: tip
You can use a non-blocking read if necessary in the following way:

```java
websocketRead().waitForResponse(false)
```

In this case, it is not recommended to add an assertion because the response could be empty.
:::

::: warning
The WebSocket plugin only supports one connection per thread at a time. If you want to change the WebSocket server during execution, you should add a disconnect sampler and then establish a new connection.
:::
