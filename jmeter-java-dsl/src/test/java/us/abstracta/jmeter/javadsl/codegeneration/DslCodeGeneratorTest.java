package us.abstracta.jmeter.javadsl.codegeneration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.core.StringTemplate;
import us.abstracta.jmeter.javadsl.util.TestResource;

public class DslCodeGeneratorTest {

  @Test
  public void shouldGenerateExpectedTestPlanDslWhenSimpleJmxIsProvided(@TempDir Path tempDir)
      throws Exception {
    File solvedTemplate = solveTemplateResource("test-plan.template.jmx", tempDir);
    assertThat(new DslCodeGenerator()
        .generateTestPlanCodeFromJmx(solvedTemplate))
        .isEqualTo(new TestResource("simple-sample.jsh").contents());
  }

  private File solveTemplateResource(String resourcePath, Path tempDir) throws IOException {
    String templateContents = new StringTemplate(new TestResource(resourcePath).contents())
        .solve();
    Path solvedTemplate = tempDir.resolve("test-plan.jmx");
    Files.write(solvedTemplate, templateContents.getBytes(StandardCharsets.UTF_8));
    return solvedTemplate.toFile();
  }

  @Test
  public void shouldGenerateExpectedTestPlanDslWhenRecordedJmxIsProvided() throws Exception {
    assertThat(new DslCodeGenerator()
        .generateTestClassCodeFromJmx(new TestResource("recorded.jmx").file()))
        .isEqualTo(new TestResource("recorded.jsh").contents());
  }

  @Test
  public void shouldGenerateExpectedTestClassWhenSimpleJmxIsProvided(@TempDir Path tempDir)
      throws Exception {
    File solvedTemplate = solveTemplateResource("test-plan.template.jmx", tempDir);
    assertThat(new DslCodeGenerator()
        .generateTestClassCodeFromJmx(solvedTemplate))
        .isEqualTo(new TestResource("SimpleTestClass.java").contents());
  }

}
