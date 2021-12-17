package us.abstracta.jmeter.javadsl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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

  public String getContents() throws IOException {
    return String.join("\n", Files.readAllLines(getFile().toPath(), StandardCharsets.UTF_8));
  }

}
