package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.core.controllers.DslTestFragmentControllerTest.DEFAULT_FRAGMENT_NAME;
import static us.abstracta.jmeter.javadsl.core.controllers.DslTestFragmentControllerTest.buildFragmentJmx;
import static us.abstracta.jmeter.javadsl.core.controllers.DslTestFragmentControllerTest.buildFragmentMethod;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;

public class DslIncludeControllerTest {

  @Nested
  public class CodeBuilderTest extends MethodCallFragmentBuilderTest {

    private static final String FRAGMENT_METHOD_CALL = "myfragment()";

    @Test
    public void shouldGenerateDslWithFragmentWhenConvertingTestPlanWithIncludeController(
        @TempDir Path tmp) throws Exception {
      File includedJmx = buildIncludedTestPlanJmx(tmp);
      String jmx = buildTestPlanJmx(
          buildThreadGroupJmx(
              buildIncludeControllerJmx(includedJmx)
          ));
      assertThat(jmx2dsl(jmx, tmp))
          .isEqualTo(buildTestPlanDsl(FRAGMENT_METHOD_CALL));
    }

    private File buildIncludedTestPlanJmx(Path tmp) throws IOException {
      String jmx = buildTestPlanJmx(buildFragmentJmx());
      Path ret = tmp.resolve("myFragment.jmx");
      Files.write(ret, jmx.getBytes(StandardCharsets.UTF_8));
      return ret.toFile();
    }

    private String buildIncludeControllerJmx(File includedJmx) {
      return new StringTemplate(testResourceContents("fragments/include-controller.template.jmx"))
          .bind("jmxPath", includedJmx)
          .solve();
    }

    public String buildTestPlanDsl(String... threadGroupChildren) {
      return buildTestPlanDslTemplate(
          Arrays.asList("httpCookies()", "httpCache()", buildThreadGroupDsl(threadGroupChildren)))
          .staticImports(Collections.singleton(DslTestFragmentController.class.getName()))
          .imports(Collections.singleton(DslTestFragmentController.class.getName()))
          .methodDefinitions(
              Collections.singletonList(buildFragmentMethod("myfragment", DEFAULT_FRAGMENT_NAME)))
          .solve();
    }

    @Test
    public void shouldReuseFragmentWhenTestPlanWithTwoIncludesWithSameFile(@TempDir Path tmp)
        throws Exception {
      File includedJmx = buildIncludedTestPlanJmx(tmp);
      String jmx = buildTestPlanJmx(
          buildThreadGroupJmx(
              buildIncludeControllerJmx(includedJmx),
              buildIncludeControllerJmx(includedJmx)
          ));
      assertThat(jmx2dsl(jmx, tmp))
          .isEqualTo(buildTestPlanDsl(FRAGMENT_METHOD_CALL, FRAGMENT_METHOD_CALL));
    }

  }

}
