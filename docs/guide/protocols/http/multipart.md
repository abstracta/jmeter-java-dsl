#### Multipart requests

When you need to upload files to an HTTP server or need to send a complex request body, you will in many cases require sending multipart requests. To send a multipart request just use `bodyPart` and `bodyFilePart` methods like in the following example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void test() throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpSampler("https://myservice.com/report")
                .method(HTTPConstants.POST)
                .bodyPart("myText", "Hello World", ContentType.TEXT_PLAIN)
                .bodyFilePart("myFile", "myReport.xml", ContentType.TEXT_XML)
        )
    ).run();
  }

}
```
