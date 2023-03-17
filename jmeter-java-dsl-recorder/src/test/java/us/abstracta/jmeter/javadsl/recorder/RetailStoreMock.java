package us.abstracta.jmeter.javadsl.recorder;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import java.net.MalformedURLException;
import java.net.URL;

public class RetailStoreMock implements AutoCloseable {

  private final WireMockServer server;

  public RetailStoreMock(int port) {
    server = new WireMockServer(wireMockConfig()
        .withRootDirectory("src/test/resources/retailstore-mock")
        .extensions(new ResponseTemplateTransformer(false))
        .port(port));
    server.start();
  }

  public URL getUrl() {
    try {
      return new URL(server.baseUrl());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() throws Exception {
    server.stop();
  }

}
