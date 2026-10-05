# jmeter-java-dsl-http2

Module for [BlazeMeter HTTP plugin](https://github.com/Blazemeter/jmeter-http2-plugin) support (HTTP/1.1, HTTP/2, HTTP/3).

## Requirements

- Java 17+ at runtime (required by the BlazeMeter HTTP plugin)
- BlazeMeter HTTP plugin JAR on the classpath

## Plugin dependency setup

Version `3.2.0` of `com.blazemeter:jmeter-bzm-http2` is not published on Maven Central. Install it locally before building:

```bash
curl -L -o jmeter-bzm-http2-3.2.0.jar \
  https://github.com/Blazemeter/jmeter-http2-plugin/releases/download/v3.2.0/jmeter-bzm-http2-3.2.0.jar

mvn install:install-file \
  -Dfile=jmeter-bzm-http2-3.2.0.jar \
  -DgroupId=com.blazemeter \
  -DartifactId=jmeter-bzm-http2 \
  -Dversion=3.2.0 \
  -Dpackaging=jar
```

## Usage

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.http2.Http2JmeterDsl.*;

testPlan(
    threadGroup(1, 1,
        http2Sampler("https://example.com")
            .http2Only(),
        httpAsyncController(
            http2Sampler("https://api.example.com/a"),
            http2Sampler("https://api.example.com/b")
        )
    )
).run();
```

## Maven dependency

```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-http2</artifactId>
  <version>2.3-SNAPSHOT</version>
</dependency>
```
