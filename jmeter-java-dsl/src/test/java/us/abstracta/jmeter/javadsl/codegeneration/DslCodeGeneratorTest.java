package us.abstracta.jmeter.javadsl.codegeneration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.core.util.TestResource;
import us.abstracta.jmeter.javadsl.core.StringTemplate;

public class DslCodeGeneratorTest {

  @Test
  public void shouldWriteExpectedTestPlanDslWhenSimpleJmxIsProvided(@TempDir Path tempDir)
      throws Exception {
    File solvedTemplate = solveTemplateResource("/test-plan.template.jmx", tempDir);
    assertThat(new DslCodeGenerator()
        .generateCodeFromJmx(solvedTemplate))
        .isEqualTo(new TestResource("/simple-sample.jsh").getContents());
  }

  private File solveTemplateResource(String resourcePath, Path tempDir) throws IOException {
    String templateContents = new StringTemplate(new TestResource(resourcePath).getContents())
        .solve();
    Path solvedTemplate = tempDir.resolve("test-plan.jmx");
    Files.write(solvedTemplate, templateContents.getBytes(StandardCharsets.UTF_8));
    return solvedTemplate.toFile();
  }

  @Test
  public void shouldWriteExpectedTestPlanDslWhenRecordedJmxIsProvided() throws Exception {
    assertThat(new DslCodeGenerator()
        .generateCodeFromJmx(new TestResource("/recorded.jmx").getFile()))
        .isEqualTo(new TestResource("/recorded.jsh").getContents());
  }

}
