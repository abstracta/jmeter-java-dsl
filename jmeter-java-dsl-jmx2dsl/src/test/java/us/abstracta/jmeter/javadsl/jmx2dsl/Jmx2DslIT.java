package us.abstracta.jmeter.javadsl.jmx2dsl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.util.TestResource;

public class Jmx2DslIT {

  @Test
  public void shouldGetConvertedFileWhenConvert() throws Exception {
    Process p = new ProcessBuilder()
        .command("java", "-jar", "target/jmx2dsl.jar",
            new TestResource("/test-plan.jmx").getFile().getPath())
        .start();
    String output = processOutput2String(p);
    p.waitFor();
    assertThat(output).isEqualTo(new TestResource("/testPlan.jsh").getContents());
  }

  private String processOutput2String(Process p) throws IOException {
    return inputStream2String(p.getInputStream()) + inputStream2String(p.getErrorStream());
  }

  private String inputStream2String(InputStream inputStream) throws IOException {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream))) {
      StringBuilder builder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
        builder.append(System.getProperty("line.separator"));
      }
      return builder.toString();
    }
  }

}
