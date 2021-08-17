package us.abstracta.jmeter.javadsl;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock;
import ru.lanwen.wiremock.ext.WiremockUriResolver;
import ru.lanwen.wiremock.ext.WiremockUriResolver.WiremockUri;

@ExtendWith({
    WiremockResolver.class,
    WiremockUriResolver.class
})
public abstract class JmeterDslTest {

  protected static final int TEST_ITERATIONS = 3;
  protected static final String SAMPLE_1_LABEL = "sample1";
  protected static final String SAMPLE_2_LABEL = "sample2";
  protected static final String OVERALL_STATS_LABEL = "overall";
  protected static final String JSON_BODY = "{\"var\":\"val\"}";

  protected String wiremockUri;
  protected WireMockServer wiremockServer;

  @BeforeEach
  public void setup(@Wiremock WireMockServer server, @WiremockUri String uri) {
    server.stubFor(any(anyUrl()));
    wiremockServer = server;
    wiremockUri = uri;
  }

  protected Map<String, Long> buildExpectedTotalCounts() {
    Map<String, Long> expectedStats = new HashMap<>();
    expectedStats.put(OVERALL_STATS_LABEL, (long) 2 * TEST_ITERATIONS);
    expectedStats.put(SAMPLE_1_LABEL, (long) TEST_ITERATIONS);
    expectedStats.put(SAMPLE_2_LABEL, (long) TEST_ITERATIONS);
    return expectedStats;
  }

  protected String getFileContents(Path filePath) throws IOException {
    return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
  }

  protected String getResourceContents(String resourcePath) throws IOException {
    try {
      return new String(Files.readAllBytes(Paths.get(getClass().getResource(resourcePath).toURI())),
          StandardCharsets.UTF_8);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
