package us.abstracta.jmeter.javadsl.core.preprocessors;

import org.apache.jmeter.threads.JMeterVariables;
import org.eclipse.jetty.http.MimeTypes;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class DslJsr223PreProcessorTest extends JmeterDslTest {

    @Test
    public void shouldBuildBodyFromPreProcessor() throws Exception {
        MimeTypes.Type contentType = MimeTypes.Type.TEXT_PLAIN;
        testPlan(
                threadGroup(1, 1,
                        JmeterDsl.
                                httpSampler(wiremockUri).post("${REQUEST_BODY}", contentType)
                                .children(
                                        jsr223PreProcessor("us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessorTest.staticFunctionToCall(vars, 'put this in the body')")
                                )
                )
        ).run();
        wiremockServer.verify(postRequestedFor(anyUrl())
                .withRequestBody(equalTo("put this in the body")));
    }

    public static void staticFunctionToCall(JMeterVariables vars, String bodyText) {
        vars.put("REQUEST_BODY", bodyText);
    }

}
