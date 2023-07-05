import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.bridge.BridgeService;
import us.abstracta.jmeter.javadsl.core.StringTemplateAssert;
import us.abstracta.jmeter.javadsl.util.TestResource;

public class BridgeServiceIT {

  @Test
  public void shouldGetExpectedStatsWhenRunSimpleTemplate(@TempDir Path tmpDir) throws Exception {
    TestResource testPlanYml = new TestResource("simpleTestPlan.yml");
    Path statsPath = tmpDir.resolve(UUID.randomUUID() + "-stats.yml");
    Process p = startCommand("run", testPlanYml, statsPath);
    int exitCode = p.waitFor();
    assertThat(exitCode).isEqualTo(0);
    StringTemplateAssert.assertThat(
            new String(Files.readAllBytes(statsPath), StandardCharsets.UTF_8))
        .matches(new TestResource("simpleTestPlan-out.template.yml").rawContents());
  }

  private Process startCommand(String command, TestResource testPlan, Path statsPath) throws IOException {
    String classPath = buildClassPath();
    return new ProcessBuilder()
        .command("java", "-cp", classPath, BridgeService.class.getName(), command, statsPath.toString())
        .redirectOutput(Redirect.INHERIT)
        .redirectError(Redirect.INHERIT)
        .redirectInput(testPlan.file())
        .start();
  }

  private String buildClassPath() {
    File dependenciesDir = new File("target/dependency");
    return "target/jmeter-java-dsl-bridge-" + System.getProperty("project.version") + ".jar:"
        + Arrays.stream(dependenciesDir.listFiles())
        .map(File::getAbsolutePath)
        .collect(Collectors.joining(":"));
  }

}
