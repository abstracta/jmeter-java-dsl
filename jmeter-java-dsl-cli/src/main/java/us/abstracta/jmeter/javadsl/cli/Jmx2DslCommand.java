package us.abstracta.jmeter.javadsl.cli;

import java.io.File;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.codegeneration.DslCodeGenerator;
import us.abstracta.jmeter.javadsl.datadog.DatadogBackendListener;
import us.abstracta.jmeter.javadsl.elasticsearch.listener.ElasticsearchBackendListener;
import us.abstracta.jmeter.javadsl.graphql.DslGraphqlSampler;
import us.abstracta.jmeter.javadsl.jdbc.JdbcJmeterDsl;
import us.abstracta.jmeter.javadsl.parallel.ParallelController;
import us.abstracta.jmeter.javadsl.wrapper.WrapperJmeterDsl;

@Command(name = "jmx2dsl", header = "Converts a JMX file to DSL code",
    description = "Conversion is a constantly improving process, so, if you find something that "
        + "is not properly converted, or you have ideas for improvement, please create an issue at "
        + "https://github.com/abstracta/jmeter-java-dsl/issues to help us improving it.",
    usageHelpAutoWidth = true)
public class Jmx2DslCommand implements Callable<Integer> {

  private static final String VERSION = getVersion();

  @Parameters(paramLabel = "JMX_FILE", description = "path to .jmx file to generate DSL from")
  private File jmxFile;

  private static String getVersion() {
    try {
      return new Cli.ManifestVersionProvider().getVersion()[0];
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Integer call() throws Exception {
    DslCodeGenerator codeGenerator = new DslCodeGenerator();
    /*
    even though code generator already includes this dependency, is necessary to re add it to
    properly solve the dependency version.
     */
    addBuildersFrom(JmeterDsl.class, "jmeter-java-dsl", codeGenerator);
    addBuildersFrom(JdbcJmeterDsl.class, "jmeter-java-dsl-jdbc", codeGenerator);
    addBuildersFrom(DslGraphqlSampler.class, "jmeter-java-dsl-graphql", codeGenerator);
    addBuildersFrom(ParallelController.class, "jmeter-java-dsl-parallel", codeGenerator);
    addBuildersFrom(WrapperJmeterDsl.class, "jmeter-java-dsl-wrapper", codeGenerator);
    addBuildersFrom(ElasticsearchBackendListener.class, "jmeter-java-dsl-elasticsearch-listener",
        codeGenerator);
    addBuildersFrom(DatadogBackendListener.class, "jmeter-java-dsl-datadog", codeGenerator);
    System.out.println(codeGenerator.generateCodeFromJmx(jmxFile));
    return 0;
  }

  private void addBuildersFrom(Class<?> jmeterDslClass, String moduleName,
      DslCodeGenerator codeGenerator) {
    codeGenerator.addBuildersFrom(jmeterDslClass);
    codeGenerator.addDependency(jmeterDslClass,
        "us.abstracta.jmeter:" + moduleName + (VERSION != null ? ":" + VERSION : ""));
  }

}
