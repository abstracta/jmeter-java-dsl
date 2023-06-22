package us.abstracta.jmeter.javadsl.http;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class DslAuthManagerTest extends JmeterDslTest {

  private static final String USERNAME = "user";
  private static final String PASSWORD = "pass";

  @Test
  public void shouldRequestIncludeAuthWhenBasicAuthWithMatchingUrl() throws Exception {
    testPlan(
        httpAuth()
            .basicAuth(wiremockUri, USERNAME, PASSWORD),
        threadGroup(1, 1,
            httpSampler(wiremockUri)
        )
    ).run();
    WireMock.verify(WireMock.getRequestedFor(WireMock.anyUrl())
        .withBasicAuth(new BasicCredentials(USERNAME, PASSWORD)));
  }

  @Test
  public void shouldRequestNotIncludeAuthWhenBasicAuthWithNonMatchingUrl() throws Exception {
    testPlan(
        httpAuth()
            .basicAuth(wiremockUri + "/test", USERNAME, PASSWORD),
        threadGroup(1, 1,
            httpSampler(wiremockUri)
        )
    ).run();
    WireMock.verify(WireMock.getRequestedFor(WireMock.anyUrl())
        .withoutHeader("Authorization"));
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithBasicAuth() {
      return testPlan(
          threadGroup(1, 1,
              httpAuth()
                  // TODO including passwords in code repositories may lead to security leaks. Review generated code and consider externalizing any credentials. Eg: System.getenv("AUTH_PASSWORD")
                  .basicAuth("http://myservice", "user", "password"),
              httpSampler("http://myservice")
          )
      );
    }

  }

}
