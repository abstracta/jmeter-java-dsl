package us.abstracta.jmeter.javadsl.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
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
   * <p>
   * Note that this method is known to have some issues, check {@link #rawContents()}.
   *
   * @return text decoded contents of the file.
   * @throws IOException if there is some problem reading associated resource contents.
   */
  public String contents() throws IOException {
    return String.join("\n", Files.readAllLines(file().toPath(), StandardCharsets.UTF_8));
  }

  /**
   * Gets all the contents of the resource without replacing OS specific new lines characters.
   * <p>
   * Additionally, this method works even when resource is inside jar, when {@link #contents()}
   * doesn't.
   * <p>
   * In version 2.0 we will replace {@link #contents()} by this method, but for the time being
   * keeping both as to avoid breaking existing users code that might be using contents().
   *
   * @return text contents of the resource.
   * @throws IOException if there is some problem reading associated resource contents.
   * @since 1.9
   */
  public String rawContents() throws IOException {
    try (BufferedInputStream bis = new BufferedInputStream(resource.openStream())) {
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      for (int result = bis.read(); result != -1; result = bis.read()) {
        buf.write((byte) result);
      }
      return buf.toString(StandardCharsets.UTF_8.name());
    }
  }

}
