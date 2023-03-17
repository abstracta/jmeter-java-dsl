package us.abstracta.jmeter.javadsl.codegeneration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;
import us.abstracta.jmeter.javadsl.util.TestResource;

public class DslCodeGeneratorTest {

  private static final String RESOURCES_FOLDER = "codegeneration";

  @Test
  public void shouldGenerateExpectedCodeWhenSimpleJmxIsProvided(@TempDir Path tempDir)
      throws Exception {
    File solvedTemplate = solveTemplateResource("test-plan.template.jmx", tempDir);
    assertThat(new DslCodeGenerator().generateCodeFromJmx(solvedTemplate))
        .isEqualTo(
            solveTestClassTemplate(Collections.singleton(ContentType.class.getName()),
                "SimpleTest.java"));
  }

  private File solveTemplateResource(String resourcePath, Path tempDir) throws IOException {
    String templateContents = new StringTemplate(new TestResource(resourcePath).rawContents())
        .solve();
    Path solvedTemplate = tempDir.resolve("test-plan.jmx");
    Files.write(solvedTemplate, templateContents.getBytes(StandardCharsets.UTF_8));
    return solvedTemplate.toFile();
  }

  private String solveTestClassTemplate(Set<String> imports, String testPlanCodeResource)
      throws IOException {
    return new TestClassTemplate()
        .dependencies(Collections.singleton("us.abstracta.jmeter:jmeter-java-dsl"))
        .imports(imports)
        .testPlan(new TestResource(RESOURCES_FOLDER + "/" + testPlanCodeResource).rawContents())
        .solve();
  }

  @Test
  public void shouldGenerateExpectedCodeWhenRecordedJmxIsProvided() throws Exception {
    assertThat(new DslCodeGenerator()
        .generateCodeFromJmx(new TestResource(RESOURCES_FOLDER + "/recorded.jmx").file()))
        .isEqualTo(solveTestClassTemplate(Collections.singleton(StandardCharsets.class.getName()),
            "RecordedTest.java"));
  }

  @Test
  public void shouldGenerateCommentedElementsCodeWhenDisabledElementsInJmx() throws Exception {
    assertThat(new DslCodeGenerator()
        .generateCodeFromJmx(new TestResource(RESOURCES_FOLDER + "/disabled-elements.jmx").file()))
        .isEqualTo(solveTestClassTemplate(Collections.emptySet(), "DisabledElements.java"));
  }

}
