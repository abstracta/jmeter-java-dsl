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
import us.abstracta.jmeter.javadsl.websocket.DslWebsocketFactory;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class Test {
    public static void main(String[] args) throws Exception {
        TestPlanStats stats = testPlan(
    threadGroup(1, 1,
        DslWebsocketFactory.websocketConnect("wss://ws.postman-echo.com/raw"),
        DslWebsocketFactory.websocketWrite("Hello WebSocket!"),
        DslWebsocketFactory.websocketRead()
            .children(
                responseAssertion()
                    .equalsToStrings("Hello WebSocket!")
            ),
        DslWebsocketFactory.websocketDisconnect()
        )
        ).run();
    }
}
```

::: warning
Only `ws://` and `wss://` protocols are supported. Using any other scheme will throw an `IllegalArgumentException`.
:::

::: tip
You can use a non blocking read if it is necessary in the following way

```java
DslWebsocketFactory.websocketRead().waitForResponse(false)
```

In this case is not recommended to add an assertion due the response could be empty
:::

::: warning
Web Socket plugin only supports one connection for threads at a time. If you want to change Web Socket server during execution you should add a disconnect sampler and then establish a new connection.
:::