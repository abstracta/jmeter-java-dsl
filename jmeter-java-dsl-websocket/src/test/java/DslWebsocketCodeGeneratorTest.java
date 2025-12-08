import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.codegeneration.DslCodeGenerator;
import us.abstracta.jmeter.javadsl.codegeneration.TestClassTemplate;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;
import us.abstracta.jmeter.javadsl.util.TestResource;
import us.abstracta.jmeter.javadsl.websocket.DslWebsocketSampler;

public class DslWebsocketCodeGeneratorTest {

  private static final String RESOURCES_FOLDER = "websocket-codegeneration";

  @Test
  public void shouldGenerateExpectedCodeWhenSimpleWebSocketJmxIsProvided(@TempDir Path tempDir)
      throws Exception {
    File solvedTemplate = solveTemplateResource("websocket-test-plan.template.jmx", tempDir);
    assertThat(new DslCodeGenerator().addBuildersFrom(DslWebsocketSampler.class).generateCodeFromJmx(solvedTemplate))
        .isEqualToNormalizingNewlines(
            solveTestClassTemplate(Collections.emptySet(),
                "SimpleWebSocketTest.java"));
  }

  @Test
  public void shouldGenerateExpectedCodeWhenComplexWebSocketJmxIsProvided(@TempDir Path tempDir) throws Exception {
    File solvedTemplate = solveTemplateResource("/complex-websocket.jmx", tempDir);
    assertThat(new DslCodeGenerator().addBuildersFrom(DslWebsocketSampler.class).generateCodeFromJmx(solvedTemplate))
        .isEqualToNormalizingNewlines(solveTestClassTemplate(Collections.emptySet(),
            "ComplexWebSocketTest.java"));
  }

  @Test
  public void shouldGenerateExpectedCodeWhenWebSocketWithAssertionsJmxIsProvided(@TempDir Path tempDir) throws Exception {
    File solvedTemplate = solveTemplateResource("/websocket-with-assertions.jmx", tempDir);
    assertThat(new DslCodeGenerator().addBuildersFrom(DslWebsocketSampler.class).generateCodeFromJmx(solvedTemplate))
        .isEqualToNormalizingNewlines(solveTestClassTemplate(Collections.emptySet(),
            "WebSocketWithAssertionsTest.java"));
  }

  @Test
  public void shouldGenerateExpectedCodeWhenWebSocketWithVariablesJmxIsProvided(@TempDir Path tempDir) throws Exception {
    assertThat(new DslCodeGenerator()
        .addBuildersFrom(DslWebsocketSampler.class)
        .addDependency(DslWebsocketSampler.class, "us.abstracta.jmeter:jmeter-java-dsl-websocket")
        .generateCodeFromJmx(new TestResource(RESOURCES_FOLDER + "/websocket-with-variables.jmx").file()))
        .isEqualToNormalizingNewlines(solveTestClassTemplate(Collections.emptySet(),
            "WebSocketWithVariablesTest.java"));
  }

  private File solveTemplateResource(String resourcePath, Path tempDir) throws IOException {
    String templateContents = new StringTemplate(new TestResource(RESOURCES_FOLDER + "/" + resourcePath).rawContents())
        .solve();
    Path solvedTemplate = tempDir.resolve("websocket-test-plan.jmx");
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

}
