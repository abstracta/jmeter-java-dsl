### WebSocket

The `DslWebsocketSampler` class provides a Java DSL for creating WebSocket performance tests using JMeter. It supports the full WebSocket lifecycle including connection, data transmission, and disconnection operations. It is based on [WebSocket Samplers by Peter Doornbosch](https://bitbucket.org/pjtr/jmeter-websocket-samplers/src/master/) plugin.

To use it, add the following dependency to your project:

:::: code-group
::: code-group-item Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-websocket</artifactId>
  <version>2.2</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-websocket:2.2'
```
:::
::::



Following you can see a basic usage example of Web Socket protocol.

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.websocket.DslWebsocketSampler.webSocketSampler;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class Test {
    public static void main(String[] args) throws Exception {
        TestPlanStats stats = testPlan(
    threadGroup(1, 1,
        webSocketSampler().connect("wss://ws.postman-echo.com/raw"),
        webSocketSampler().write("Hello WebSocket!"),
        webSocketSampler().read()
            .children(
                responseAssertion()
                    .equalsToStrings("Hello WebSocket!")
            ),
        webSocketSampler().disconnect()
        )
        ).run();
    }
}
```

::: warning
Only `ws://` and `wss://` protocols are supported. Using any other scheme will throw an `IllegalArgumentException`.
:::

You can use a non blocking read if it is necessary in the following way

```java
webSocketSampler().read().waitForResponse(false)
```

::: warning
In this case is not recommended to add an assertion due the response could be empty
:::

::: tip
Web Socket protocol only supports one connection at a time. If you want to change Web Socket server during execution you should add a disconnect sampler and then establish a new connection.
:::