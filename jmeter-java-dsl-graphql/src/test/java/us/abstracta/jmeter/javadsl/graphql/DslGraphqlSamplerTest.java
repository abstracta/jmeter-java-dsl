package us.abstracta.jmeter.javadsl.graphql;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.vars;
import static us.abstracta.jmeter.javadsl.graphql.DslGraphqlSampler.graphqlSampler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class DslGraphqlSamplerTest extends JmeterDslTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String QUERY = "{ user(id: 1) { name } }";

  @Test
  public void shouldSendGraphqlQueryToServerWhenGraphqlSampler() throws Exception {
    testPlan(
        threadGroup(1, 1,
            graphqlSampler(wiremockUri, QUERY)
        )
    ).run();
    verify(postRequestedFor(anyUrl())
        .withHeader(HTTPConstants.HEADER_CONTENT_TYPE,
            equalTo(ContentType.APPLICATION_JSON.toString()))
        .withRequestBody(equalToJson(buildRequestBody(null, null))));
  }

  @NotNull
  private String buildRequestBody(String operationName, Map<String, Object> variables)
      throws JsonProcessingException {
    Map<String, Object> ret = new LinkedHashMap<>();
    ret.put("operationName", operationName);
    ret.put("query", QUERY);
    if (variables != null) {
      ret.put("variables", variables);
    }
    return OBJECT_MAPPER.writeValueAsString(ret);
  }

  @Test
  public void shouldSendGraphqlQueryWithOperationWhenGraphqlSamplerWithOperation()
      throws Exception {
    String operationName = "myOp";
    testPlan(
        threadGroup(1, 1,
            graphqlSampler(wiremockUri, QUERY)
                .operationName(operationName)
        )
    ).run();
    verify(postRequestedFor(anyUrl())
        .withRequestBody(equalToJson(buildRequestBody(operationName, null))));
  }

  @Test
  public void shouldSendGraphqlQueryWithVariablesWhenGraphqlSamplerWithSimpleVariables()
      throws Exception {
    LinkedHashMap<String, Object> vars = new LinkedHashMap<>();
    vars.put("var1", "val1");
    vars.put("var2", 2);
    vars.put("var3", 2.5);
    vars.put("var4", false);
    vars.put("var5", null);
    DslGraphqlSampler sampler = graphqlSampler(wiremockUri, QUERY);
    vars.forEach(sampler::variable);
    testPlan(
        threadGroup(1, 1,
            sampler
        )
    ).run();
    verify(postRequestedFor(anyUrl())
        .withRequestBody(equalToJson(buildRequestBody(null, vars))));
  }

  @Test
  public void shouldSendGraphqlQueryWithVariablesWhenGraphqlSamplerWithVariablesJson()
      throws Exception {
    LinkedHashMap<String, Object> vars = new LinkedHashMap<>();
    vars.put("var1", 1);
    vars.put("var2", "test");
    vars.put("var3", Collections.singletonMap("prop", 3));
    vars.put("var4", Collections.singletonList(1));
    testPlan(
        threadGroup(1, 1,
            graphqlSampler(wiremockUri, QUERY)
                .variablesJson(OBJECT_MAPPER.writeValueAsString(vars))
        )
    ).run();
    verify(postRequestedFor(anyUrl())
        .withRequestBody(equalToJson(buildRequestBody(null, vars))));
  }

  @Test
  public void shouldSendGraphqlQueryWithVariablesWhenGraphqlSamplerWithRawVariable()
      throws Exception {
    String jmeterVarName = "MY_VAR";
    int varValue = 1;
    String graphqlVarName = "var1";
    testPlan(
        vars()
            .set(jmeterVarName, String.valueOf(varValue)),
        threadGroup(1, 1,
            graphqlSampler(wiremockUri, QUERY)
                .rawVariable(graphqlVarName, "${" + jmeterVarName + "}")
        )
    ).run();
    verify(postRequestedFor(anyUrl())
        .withRequestBody(equalToJson(
            buildRequestBody(null, Collections.singletonMap(graphqlVarName, varValue)))));
  }

  @Test
  public void shouldSendGraphqlQueryToServerWhenGraphqlSamplerWithHttpGet()
      throws Exception {
    testPlan(
        threadGroup(1, 1,
            graphqlSampler(wiremockUri, QUERY)
                .httpGet()
        )
    ).run();
    verify(getRequestedFor(anyUrl()).withQueryParam("query", equalTo(QUERY)));
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public CodeBuilderTest() {
      codeGenerator.addBuildersFrom(DslGraphqlSampler.class);
    }

    public DslTestPlan testPlanWithSimpleGraphqlQuery() {
      return testPlan(
          threadGroup(1, 1,
              graphqlSampler("http://localhost", "{ user(id: 1){ name } }")
          )
      );
    }

    public DslTestPlan testPlanWithGraphqlAndOperationAndSimpleVariables() {
      return testPlan(
          threadGroup(1, 1,
              graphqlSampler("http://localhost", "{ user(id: 1){ name } }")
                  .operationName("myOp")
                  .variable("var1", 1)
                  .variable("var2", 2.5)
                  .variable("var3", "test")
                  .variable("var4", false)
                  .variable("var5", null)
          )
      );
    }

    public DslTestPlan testPlanWithGraphqlAndVariablesJson() {
      return testPlan(
          vars()
              .set("MY_VARS", "{\"var\": 1}"),
          threadGroup(1, 1,
              graphqlSampler("http://localhost", "{ user(id: 1){ name } }")
                  .variablesJson("${MY_VARS}")
          )
      );
    }

    public DslTestPlan testPlanWithGraphqlAndRawVariable() {
      return testPlan(
          threadGroup(1, 1,
              graphqlSampler("http://localhost", "{ user(id: 1){ name } }")
                  .rawVariable("var", "[1,2,3]")
          )
      );
    }

  }

}
