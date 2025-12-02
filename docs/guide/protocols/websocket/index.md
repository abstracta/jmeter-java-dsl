# WebSocket Sampler Documentation

## Overview

The `DslWebsocketSampler` class provides a Java DSL for creating WebSocket performance tests using JMeter. It supports the full WebSocket lifecycle including connection, data transmission, and disconnection operations.

## Features

- **Connection Management**: Establish and close WebSocket connections
- **Data Transmission**: Send and receive WebSocket messages
- **URL Parsing**: Automatic parsing of WebSocket URLs (ws:// and wss://)
- **Timeout Configuration**: Configurable connection and response timeouts
- **TLS Support**: Secure WebSocket connections with WSS protocol
- **Fluent API**: Method chaining for easy configuration

## Main Components

### 1. DslWebsocketSampler (Main Class)

The main class that provides static factory methods for creating different types of WebSocket samplers.

#### Static Methods

- `webSocketSampler()` - Creates a basic WebSocket sampler
- `connect()` - Creates a WebSocket connection sampler
- `connect(String url)` - Creates a WebSocket connection sampler with URL parsing
- `disconnect()` - Creates a WebSocket disconnection sampler
- `write()` - Creates a WebSocket write sampler
- `read()` - Creates a WebSocket read sampler

### 2. DslConnectSampler (Connection Operations)

Handles WebSocket connection establishment.

#### Configuration Methods

- `connectionTimeout(String timeout)` - Sets connection timeout in milliseconds
- `responseTimeout(String timeout)` - Sets response timeout in milliseconds
- `server(String server)` - Sets the WebSocket server hostname
- `port(String port)` - Sets the WebSocket server port
- `path(String path)` - Sets the WebSocket path
- `tls(boolean tls)` - Enables/disables TLS encryption

#### URL Parsing

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

### 3. DslDisconnectSampler (Disconnection Operations)

Handles WebSocket connection closure.

#### Configuration Methods

- `responseTimeout(String timeout)` - Sets response timeout in milliseconds
- `statusCode(String statusCode)` - Sets the close status code (e.g., "1000" for normal closure)

### 4. DslWriteSampler (Write Operations)

Handles sending data through WebSocket connections.

#### Configuration Methods

- `connectionTimeout(String timeout)` - Sets connection timeout in milliseconds
- `requestData(String requestData)` - Sets the data to send
- `createNewConnection(boolean createNewConnection)` - Whether to create a new connection
- `loadDataFromFile(boolean loadDataFromFile)` - Whether to load data from a file

### 5. DslReadSampler (Read Operations)

Handles receiving data from WebSocket connections.

#### Configuration Methods

- `connectionTimeout(String timeout)` - Sets connection timeout in milliseconds
- `responseTimeout(String timeout)` - Sets response timeout in milliseconds
- `createNewConnection(boolean createNewConnection)` - Whether to create a new connection

## Usage Examples

### Basic WebSocket Test

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import us.abstracta.jmeter.javadsl.websocket.DslWebsocketSampler;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class Test {
    public static void main(String[] args) throws Exception {
        TestPlanStats stats = testPlan(
    threadGroup(1, 1,
        // Connect to WebSocket server
        DslWebsocketSampler
            .connect("wss://ws.postman-echo.com/raw")
            .connectionTimeout("10000")
            .responseTimeout("5000"),
        
        // Send a message
        DslWebsocketSampler
            .write()
            .requestData("Hello WebSocket!")
            .createNewConnection(false),
        
        // Read the response
        DslWebsocketSampler
            .read()
            .responseTimeout("5000")
            .createNewConnection(false)
            .children(
                responseAssertion()
                    .equalsToStrings("Hello WebSocket!")
            ),
        
        // Close the connection
        DslWebsocketSampler
            .disconnect()
            .responseTimeout("1000")
            .statusCode("1000")
        )
        ).run();
    }
}
```

### Manual Connection Configuration

```java
DslWebsocketSampler
    .connect()
    .server("localhost")
    .port("8080")
    .path("/websocket")
    .tls(false)
    .connectionTimeout("5000")
    .responseTimeout("3000")
```

### Connection with Assertions

```java
DslWebsocketSampler
    .read()
    .responseTimeout("5000")
    .createNewConnection(false)
    .children(
        responseAssertion()
            .containsSubstrings("expected response")
    )
```

## Error Handling

### Invalid URL Handling

```java
// This will throw IllegalArgumentException
DslWebsocketSampler.connect("http://localhost:80/test");
```

The URL parser validates:
- Protocol must be `ws://` or `wss://`
- Hostname is required
- Valid URI syntax

### Connection Timeouts

Configure appropriate timeouts to handle network issues:

```java
DslWebsocketSampler
    .connect("wss://example.com/ws")
    .connectionTimeout("10000")  // 10 seconds
    .responseTimeout("5000")     // 5 seconds
```

## Best Practices

1. **Connection Reuse**: Set `createNewConnection(false)` for write/read operations to reuse existing connections
2. **Timeout Configuration**: Always set appropriate timeouts to avoid hanging tests
3. **Error Handling**: Use response assertions to validate WebSocket responses
4. **URL Parsing**: Use the `connect(String url)` method for cleaner code when you have complete URLs
5. **Status Codes**: Use standard WebSocket close codes (1000 for normal closure)

## Integration with Test Plans

WebSocket samplers integrate seamlessly with other JMeter DSL components:

```java
testPlan(
    threadGroup(10, 100,
        // WebSocket operations
        DslWebsocketSampler.connect("wss://api.example.com/ws"),
        DslWebsocketSampler.write().requestData("test data"),
        DslWebsocketSampler.read(),
        DslWebsocketSampler.disconnect(),
    ),
    // Results collection
    jtlWriter("results.jtl"),
    resultsTreeVisualizer()
)
```
