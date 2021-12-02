package us.abstracta.jmeter.javadsl;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class TestResource {

  private final URL resource;

  public TestResource(String resourcePath) {
    resource = getClass().getResource(resourcePath);
  }

  public File getFile() {
    try {
      return new File(resource.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
