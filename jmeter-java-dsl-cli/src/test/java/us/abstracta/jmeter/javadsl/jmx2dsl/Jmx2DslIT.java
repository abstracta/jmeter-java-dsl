package us.abstracta.jmeter.javadsl.jmx2dsl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.codegeneration.TestClassTemplate;
import us.abstracta.jmeter.javadsl.util.TestResource;

public class Jmx2DslIT {

  @Test
  public void shouldGetConvertedFileWhenConvert() throws Exception {
    Process p = startCommand("jmx2dsl", new TestResource("test-plan.jmx").filePath());
    assertThat(getProcessOutput(p))
        .isEqualTo(buildConvertedTestClass());
  }

  private Process startCommand(String command, String... args) throws IOException {
    ProcessBuilder ret = new ProcessBuilder()
        .command("java", "-jar", "target/jmdsl.jar", command);
    ret.command().addAll(Arrays.asList(args));
    return ret.start();
  }

  private String getProcessOutput(Process p) throws IOException, InterruptedException {
    String ret = inputStream2String(p.getInputStream()) + inputStream2String(p.getErrorStream());
    p.waitFor();
    return ret;
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

  private static String buildConvertedTestClass()
      throws IOException {
    return new TestClassTemplate()
        .dependencies(Collections.singleton(
                "us.abstracta.jmeter:jmeter-java-dsl:" + System.getProperty("project.version")))
            .imports(Collections.singleton(ContentType.class.getName()))
            .testPlan(new TestResource("TestPlan.java").rawContents())
            .solve() + "\n";
  }

}
