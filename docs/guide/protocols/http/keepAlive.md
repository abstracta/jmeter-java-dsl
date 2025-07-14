#### Keep alive

Jmeter-java-dsl maintains a persistent http connection for subsequent requests to the same server.
It is done by sending `Connection: keep-alive` header.
If you want to disable keep-alive logic and force a server to close connection after each request then use`.useKeepAlive(false)` in a given `httpSampler` or `httpDefaults`.


```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import org.junit.jupiter.api.Test;
public class PerformanceTest {

  @Test
  public void test1() throws Exception {
    testPlan(
        threadGroup(1, 10,
            httpSampler("https://myservice1.com")
                .useKeepAlive(false), 
            httpSampler("https://myservice2.com")
                .useKeepAlive(true),
            httpSampler("https://myservice3.com")
        )
    ).run();
  }

  @Test
  public void test2() throws Exception {      
    testPlan(
        httpDefaults()
            .useKeepAlive(false), 
        threadGroup(1, 10,
            httpSampler("https://myservice1.com"),
            httpSampler("https://myservice2.com")
       )
    ).run();
  }

}
```