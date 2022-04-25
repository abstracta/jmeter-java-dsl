package us.abstracta.jmeter.javadsl.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Eases usage of test resources files (e.g.: files in {@code src/test/resources}).
 * <p>
 * This internally uses java {@link ClassLoader#getResource(String)}. For instance, if you want to
 * reference a file in {@code "src/test/resources/csvs/myCsv.csv"} in a maven project, you will need
 * to use new {@code TestResource("csvs/myCsv.csv")}.
 *
 * @since 0.54
 */
public class TestResource {

  private final URL resource;

  public TestResource(String resourcePath) {
    ClassLoader loader = Optional.ofNullable(Thread.currentThread().getContextClassLoader())
        .orElse(TestResource.class.getClassLoader());
    resource = loader.getResource(resourcePath);
    if (resource == null) {
      throw new IllegalArgumentException(
          "Could not access file associated to resource " + resourcePath);
    }
  }

  /**
   * Gets the file associated to the resource.
   * <p>
   * This is handy when you need to take some file operations or need further information about the
   * file.
   *
   * @return the associated file.
   */
  public File file() {
    try {
      return new File(resource.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets the associated file path.
   * <p>
   * This is the same as {@code file().getPath()}.
   * <p>
   * This handy when you need the file path, and avoid previously mentioned boilerplate code.
   *
   * @return the associated file path.
   */
  public String filePath() {
    return file().getPath();
  }

  /**
   * Gets the contents of the given resources as string.
   * <p>
   * Take into consideration that this uses UTF_8 encoding for reading the file and that file should
   * be in general be a textual one (otherwise decoding it may fail).
   *
   * @return text decoded contents of the file.
   * @throws IOException if there is some problem reading associated resource contents.
   */
  public String contents() throws IOException {
    return String.join("\n", Files.readAllLines(file().toPath(), StandardCharsets.UTF_8));
  }

}
