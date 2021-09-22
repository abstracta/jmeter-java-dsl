package us.abstracta.jmeter.javadsl;

import com.google.common.io.Resources;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class TestResource {

  private final URL resource;

  public TestResource(String resourcePath) {
    resource = Resources.getResource(getClass(), resourcePath);
  }

  public File getFile() {
    try {
      return new File(resource.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
