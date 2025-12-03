### WebSocket Sampler Documentation

The `DslWebsocketSampler` class provides a Java DSL for creating WebSocket performance tests using JMeter. It supports the full WebSocket lifecycle including connection, data transmission, and disconnection operations. It is based on [WebSocket Samplers by Peter Doornbosch](https://bitbucket.org/pjtr/jmeter-websocket-samplers/src/master/) plugin.

To use it, add the following dependency to your project:

```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-websocket</artifactId>
  <version>2.2</version>
  <scope>test</scope>
</dependency>
```

#### Main Components

- `webSocketSampler().connect()` - Creates a WebSocket connection sampler
- `webSocketSampler().connect(String url)` - Creates a WebSocket connection sampler with URL parsing
- `webSocketSampler().disconnect()` - Creates a WebSocket disconnection sampler
- `webSocketSampler().write()` - Creates a WebSocket write sampler
- `webSocketSampler().read()` - Creates a WebSocket read sampler

##### Connect configuration

- `connectionTimeout(String timeout)` - Sets connection timeout in milliseconds
- `responseTimeout(String timeout)` - Sets response timeout in milliseconds
- `server(String server)` - Sets the WebSocket server hostname
- `port(String port)` - Sets the WebSocket server port
- `path(String path)` - Sets the WebSocket path
- `tls(boolean tls)` - Enables/disables TLS encryption

##### URL Parsing

The `connect(String url)` method automatically parses WebSocket URLs and extracts:
- Protocol (ws:// or wss://)
- Hostname
- Port (defaults to 80 for ws://, 443 for wss://)
- Path and query parameters
- TLS configuration

**Supported URL formats:**
- `ws://localhost:8080/websocket`
- `wss://example.com:8443/chat?room=general`
- `wss://api.example.com/ws`

##### Disconnection configuration

- `responseTimeout(String timeout)` - Sets response timeout in milliseconds
- `statusCode(String statusCode)` - Sets the close status code (e.g., "1000" for normal closure)

##### Write configuration

- `connectionTimeout(String timeout)` - Sets connection timeout in milliseconds
- `requestData(String requestData)` - Sets the data to send
- `createNewConnection(boolean createNewConnection)` - Whether to create a new connection
- `loadDataFromFile(boolean loadDataFromFile)` - Whether to load data from a file

##### Read configuration 

- `connectionTimeout(String timeout)` - Sets connection timeout in milliseconds
- `responseTimeout(String timeout)` - Sets response timeout in milliseconds
- `createNewConnection(boolean createNewConnection)` - Whether to create a new connection

#### Usage Examples

##### Basic WebSocket Test

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import us.abstracta.jmeter.javadsl.websocket.DslWebsocketSampler;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class Test {
    public static void main(String[] args) throws Exception {
        TestPlanStats stats = testPlan(
    threadGroup(1, 1,
        // Connect to WebSocket server
        webSocketSampler()
            .connect("wss://ws.postman-echo.com/raw")
            .connectionTimeout("10000")
            .responseTimeout("5000"),
        
        // Send a message
        webSocketSampler()
            .write()
            .requestData("Hello WebSocket!")
            .createNewConnection(false),
        
        // Read the response
        webSocketSampler()
            .read()
            .responseTimeout("5000")
            .createNewConnection(false)
            .children(
                responseAssertion()
                    .equalsToStrings("Hello WebSocket!")
            ),
        
        // Close the connection
        webSocketSampler()
            .disconnect()
            .responseTimeout("1000")
            .statusCode("1000")
        )
        ).run();
    }
}
```

##### Manual Connection Configuration

```java
webSocketSampler()
    .connect()
    .server("localhost")
    .port("8080")
    .path("/websocket")
    .tls(false)
    .connectionTimeout("5000")
    .responseTimeout("3000")
```

##### Connection with Assertions

```java
webSocketSampler()
    .read()
    .responseTimeout("5000")
    .createNewConnection(false)
    .children(
        responseAssertion()
            .containsSubstrings("expected response")
    )
```

#### Error Handling

##### Invalid URL Handling

```java
// This will throw IllegalArgumentException
webSocketSampler().connect("http://localhost:80/test");
```

The URL parser validates:
- Protocol must be `ws://` or `wss://`
- Hostname is required
- Valid URI syntax

##### Connection Timeouts

Configure appropriate timeouts to handle network issues:

```java
webSocketSampler()
    .connect("wss://example.com/ws")
    .connectionTimeout("10000")  // 10 seconds
    .responseTimeout("5000")     // 5 seconds
```

#### Best Practices

1. **Connection Reuse**: Set `createNewConnection(false)` for write/read operations to reuse existing connections
2. **Timeout Configuration**: Always set appropriate timeouts to avoid hanging tests
3. **Error Handling**: Use response assertions to validate WebSocket responses
4. **URL Parsing**: Use the `connect(String url)` method for cleaner code when you have complete URLs
5. **Status Codes**: Use standard WebSocket close codes (1000 for normal closure)

#### Integration with Test Plans

WebSocket samplers integrate seamlessly with other JMeter DSL components:

```java
testPlan(
    threadGroup(10, 100,
        // WebSocket operations
        webSocketSampler().connect("wss://api.example.com/ws"),
        webSocketSampler().write().requestData("test data"),
        webSocketSampler().read(),
        webSocketSampler().disconnect(),
    ),
    // Results collection
    jtlWriter("results.jtl"),
    resultsTreeVisualizer()
)
```
