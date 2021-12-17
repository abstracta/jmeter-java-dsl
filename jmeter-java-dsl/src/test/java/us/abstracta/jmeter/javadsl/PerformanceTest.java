package us.abstracta.jmeter.javadsl;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

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
