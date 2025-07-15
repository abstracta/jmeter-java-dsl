#### Keep alive

By default, **JMeter Java DSL** uses a persistent HTTP connection (sending the `Connection: keep-alive` header) to reuse the same socket for multiple requests to the same server.

To force server to close the connection after each request, you can disable keep-alive with `.useKeepAlive(false)` either on an individual `httpSampler` or globally on `httpDefaults`.

This can be useful when you’re load-testing an API that enforces a strict limit on the number of simultaneous sockets per client. If you leave keep-alive enabled, each thread will hold its socket open for the duration of the test, consuming the server's connection resources.

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import org.junit.jupiter.api.Test;
public class PerformanceTest {

  @Test
  public void test1() throws Exception {
    testPlan(
        threadGroup(1, 10,
            //Disable keep-alive for this sampler: sends Connection: close header and close the connection after this request
            httpSampler("https://myservice1.com")
                .useKeepAlive(false),
            // Explicitly enable keep-alive for this sampler: sends Connection: keep-alive header and keeps the connection open
            httpSampler("https://myservice2.com")
                .useKeepAlive(true),
            //No keep-alive configuration, so it will use the default keep-alive behavior (keep-alive enabled)
            httpSampler("https://myservice3.com")
        )
    ).run();
  }

  @Test
  public void test2() throws Exception {      
    testPlan(
        //Disable keep-alive globally for all samplers in this test plan
        httpDefaults()
            .useKeepAlive(false), 
        threadGroup(1, 10,
            // These samplers inherit keep-alive disabled from httpDefaults()
            httpSampler("https://myservice1.com"),
            httpSampler("https://myservice2.com"),
            // Explicitly enable keep-alive for this sampler overriding the global setting
            httpSampler("https://myservice3.com")
                .useKeepAlive(true)
       )
    ).run();
  }

}
```