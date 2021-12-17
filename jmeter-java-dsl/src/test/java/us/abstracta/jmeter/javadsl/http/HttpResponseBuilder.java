package us.abstracta.jmeter.javadsl.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.util.HTTPConstants;

public class HttpResponseBuilder {

  private HttpResponseBuilder() {
  }

  public static ResponseDefinitionBuilder buildEmbeddedResourcesResponse(String... resourcesUrls) {
    return aResponse()
        .withHeader(HTTPConstants.HEADER_CONTENT_TYPE, ContentType.TEXT_XML.toString())
        .withBody("<html>" +
            Arrays.stream(resourcesUrls)
                .map(r -> "<img src=\"" + r + "\"/>")
                .collect(Collectors.joining())
            + "</html>");
  }

}
