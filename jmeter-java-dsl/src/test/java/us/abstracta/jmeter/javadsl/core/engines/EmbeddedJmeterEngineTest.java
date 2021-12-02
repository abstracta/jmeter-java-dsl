package us.abstracta.jmeter.javadsl.core.engines;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class EmbeddedJmeterEngineTest extends JmeterDslTest {

  @Test
  public void shouldProperlySolvePropGroovyReferenceWhenEngineWithSpecifiedProperty()
      throws Exception {
    String propName = "MY_PROP";
    String propVal = "MY_VAL";
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri + "/${__groovy(props['" + propName + "'])}")
        )
    ).runIn(new EmbeddedJmeterEngine()
        .prop(propName, propVal));
    wiremockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/" + propVal)));
  }

}
