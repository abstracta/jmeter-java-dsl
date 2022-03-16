package us.abstracta.jmeter.javadsl.core.configs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class DslVariablesTest extends JmeterDslTest {

  @Test
  public void shouldInitVariablesWhenVarsAsTestPlanChild() throws Exception {
    String varName = "var1";
    String varValue = "value1";
    testPlan(
        vars()
            .set(varName, varValue),
        threadGroup(1, 1,
            httpSampler(wiremockUri + "/${" + varName + "}")
        )
    ).run();
    verify(getRequestedFor(urlEqualTo("/" + varValue)));
  }

  @Test
  public void shouldUpdateVariableWhenVarsAsThreadGroupChild() throws Exception {
    String varName = "var1";
    String varValue1 = "value1";
    String varValue2 = "value2";
    testPlan(
        vars()
            .set(varName, varValue1),
        threadGroup(1, 1,
            vars()
                .set(varName, varValue2),
            httpSampler(wiremockUri + "/${" + varName + "}")
        )
    ).run();
    verify(getRequestedFor(urlEqualTo("/" + varValue2)));
  }

  @Test
  public void shouldResetVariableBetweenIterationsWhenVarsInitAtInitOfThreadGroup()
      throws Exception {
    String varName = "var1";
    String varValue1 = "value1";
    String varValue2 = "value2";
    testPlan(
        threadGroup(1, 1,
            vars()
                .set(varName, varValue1),
            httpSampler(wiremockUri + "/${" + varName + "}"),
            vars()
                .set(varName, varValue2)
        )
    ).run();
    verify(getRequestedFor(urlEqualTo("/" + varValue1)));
  }

  @Test
  public void shouldAllowCrossVariableReferenceWhenVarsAsThreadGroupChild() throws Exception {
    String var1Name = "var1";
    String var1Value = "value1";
    String var2Name = "var2";
    testPlan(
        threadGroup(1, 1,
            vars()
                .set(var1Name, var1Value)
                .set(var2Name, "${" + var1Name + "}"),
            httpSampler(wiremockUri + "/${" + var2Name + "}")
        )
    ).run();
    verify(getRequestedFor(urlEqualTo("/" + var1Value)));
  }

  @Test
  public void shouldProperlyEscapeCharactersWhenVarsWithGroovyAndJMeterSpecialChars()
      throws Exception {
    String var1Name = "var1";
    String var1Value = "value1";
    String var2Name = "var2";
    String immutablePart = " $#{\" + var1Name+ \"} #{\" + var1Name + \"} ##{\" + var1Name + \"} \n"
        + "\t'\r\\";
    String var2Value = "${" + var1Name + "}" + immutablePart;
    testPlan(
        threadGroup(1, 1,
            vars()
                .set(var1Name, var1Value)
                .set(var2Name, var2Value),
            httpSampler(wiremockUri).post("${" + var2Name + "}", ContentType.TEXT_PLAIN)
        )
    ).run();
    verify(postRequestedFor(anyUrl()).withRequestBody(equalTo(var1Value + immutablePart)));
  }

}
