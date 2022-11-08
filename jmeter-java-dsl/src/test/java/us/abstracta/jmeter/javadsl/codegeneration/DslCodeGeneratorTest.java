package us.abstracta.jmeter.javadsl.codegeneration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;
import us.abstracta.jmeter.javadsl.util.TestResource;

public class DslCodeGeneratorTest {

  @Test
  public void shouldGenerateExpectedCodeWhenSimpleJmxIsProvided(@TempDir Path tempDir)
      throws Exception {
    File solvedTemplate = solveTemplateResource("test-plan.template.jmx", tempDir);
    assertThat(new DslCodeGenerator()
        .generateCodeFromJmx(solvedTemplate))
        .isEqualTo(new TestClassTemplate()
            .dependencies(Collections.singleton("us.abstracta.jmeter:jmeter-java-dsl"))
            .imports(Collections.singleton(ContentType.class.getName()))
            .testPlan(new TestResource("codegeneration/SimpleTest.java").contents())
            .solve());
  }

  private File solveTemplateResource(String resourcePath, Path tempDir) throws IOException {
    String templateContents = new StringTemplate(new TestResource(resourcePath).contents())
        .solve();
    Path solvedTemplate = tempDir.resolve("test-plan.jmx");
    Files.write(solvedTemplate, templateContents.getBytes(StandardCharsets.UTF_8));
    return solvedTemplate.toFile();
  }

  @Test
  public void shouldGenerateExpectedCodeWhenRecordedJmxIsProvided() throws Exception {
    assertThat(new DslCodeGenerator()
        .generateCodeFromJmx(new TestResource("codegeneration/recorded.jmx").file()))
        .isEqualTo(new TestClassTemplate()
            .dependencies(Collections.singleton("us.abstracta.jmeter:jmeter-java-dsl"))
            .imports(Collections.singleton(StandardCharsets.class.getName()))
            .testPlan(new TestResource("codegeneration/RecordedTest.java").contents())
            .solve());
  }

}
