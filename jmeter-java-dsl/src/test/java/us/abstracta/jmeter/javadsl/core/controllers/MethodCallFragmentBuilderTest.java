package us.abstracta.jmeter.javadsl.core.controllers;

import static us.abstracta.jmeter.javadsl.JmeterDsl.testResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import us.abstracta.jmeter.javadsl.codegeneration.DslCodeGenerator;
import us.abstracta.jmeter.javadsl.codegeneration.Indentation;
import us.abstracta.jmeter.javadsl.codegeneration.TestClassTemplate;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;

public abstract class MethodCallFragmentBuilderTest {

  protected String buildTestPlanJmx(String... childrenJmx) {
    return new StringTemplate(testResourceContents("test-plan.template.jmx"))
        .bind("children", String.join("\n", childrenJmx))
        .solve();
  }

  protected static String testResourceContents(String resourceName) {
    try {
      return testResource(resourceName.startsWith("/") ? resourceName.substring(1)
          : "codegeneration/" + resourceName).rawContents();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected String jmx2dsl(String testPlanJmx, Path tmp) throws IOException {
    Path testPlanPath = tmp.resolve("testplan.jmx");
    Files.write(testPlanPath, testPlanJmx.getBytes(StandardCharsets.UTF_8));
    return new DslCodeGenerator().generateCodeFromJmx(testPlanPath.toFile());
  }

  protected TestClassTemplate buildTestPlanDslTemplate(List<String> children) {
    return new TestClassTemplate()
        .dependencies(Collections.singleton("us.abstracta.jmeter:jmeter-java-dsl"))
        .testPlan(buildTestPlanMethodCode(children));
  }

  protected String buildTestPlanMethodCode(List<String> children) {
    String childIndent = Indentation.indentLevel(4);
    return String.format("testPlan(\n%s\n%s)", children.stream()
        .map(c -> Indentation.indent(c, childIndent))
        .collect(Collectors.joining(",\n")), Indentation.indentLevel(2));
  }

  protected String buildThreadGroupJmx(String... childrenJmx) {
    return new StringTemplate(testResourceContents("fragments/thread-group.template.jmx"))
        .bind("children", String.join("\n", childrenJmx))
        .solve();
  }

  protected String buildThreadGroupDsl(String... children) {
    return String.format("threadGroup(1, 1,\n%s\n)",
        Indentation.indent(String.join(",\n", children), Indentation.INDENT));
  }

}
