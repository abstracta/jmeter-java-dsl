package us.abstracta.jmeter.javadsl.http;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Nested;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class HttpHeadersTest {

  @SuppressWarnings("unused")
  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithHeaders() {
      return testPlan(
          threadGroup(1, 1,
              httpHeaders()
                  .header("Accept", "application/json")
                  .header("X-My-Header", "my-value")
                  .header("X-Other-Header", "")
                  .contentType(ContentType.APPLICATION_JSON),
              httpSampler("http://localhost")
          )
      );
    }

  }

}
