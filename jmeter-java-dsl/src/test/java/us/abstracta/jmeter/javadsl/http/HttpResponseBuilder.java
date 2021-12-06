package us.abstracta.jmeter.javadsl.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes.Type;

public class HttpResponseBuilder {

  private HttpResponseBuilder() {
  }

  public static ResponseDefinitionBuilder buildEmbeddedResourcesResponse(String... resourcesUrls) {
    return aResponse()
        .withHeader(HttpHeader.CONTENT_TYPE.toString(), Type.TEXT_HTML.toString())
        .withBody("<html>" +
            Arrays.stream(resourcesUrls)
                .map(r -> "<img src=\"" + r + "\"/>")
                .collect(Collectors.joining())
            + "</html>");
  }

}
