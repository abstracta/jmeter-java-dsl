package us.abstracta.jmeter.javadsl.core.listeners;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.visualizers.backend.graphite.GraphiteBackendListenerClient;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;

/**
 * Test element which publishes all test run metrics to a Graphite instance.
 *
 * @since 1.25
 */
public class GraphiteBackendListener extends DslBackendListener<GraphiteBackendListener> {

  private static final int PICKLE_PORT = 2004;
  private static final String HOST_ARG = "graphiteHost";
  private static final String PORT_ARG = "graphitePort";
  private static final String PREFIX_ARG = "rootMetricsPrefix";
  private String prefix;

  public GraphiteBackendListener(String url) {
    super(GraphiteBackendListenerClient.class, url);
  }

  /**
   * Allows specifying the prefix used to store the metrics in Graphite.
   * <p>
   * This is useful to group the metrics of a single run, test plan, project, etc.
   *
   * @param prefix specifies the prefix to be used to store the metrics. When not specified
   *               "jmeter." is used.
   * @return the Graphite listener for further configuration or usage.
   */
  public GraphiteBackendListener metricsPrefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  @Override
  protected Arguments buildListenerArguments() {
    Arguments ret = new Arguments();
    solveHostAndPort(url, ret);
    if (prefix != null) {
      ret.addArgument(PREFIX_ARG, prefix);
    }
    ret.addArgument("graphiteMetricsSender",
        "org.apache.jmeter.visualizers.backend.graphite.PickleGraphiteMetricsSender");
    ret.addArgument("summaryOnly", "false");
    ret.addArgument("useRegexpForSamplersList", "true");
    ret.addArgument("samplersList", ".*");
    return ret;
  }

  private void solveHostAndPort(String url, Arguments args) {
    String host;
    String port;
    int portPos = url.indexOf(":");
    if (portPos >= 0) {
      host = url.substring(0, portPos);
      port = url.substring(portPos + 1);
    } else {
      host = url;
      port = String.valueOf(PICKLE_PORT);
    }
    args.addArgument(HOST_ARG, host);
    args.addArgument(PORT_ARG, port);
  }

  public static class CodeBuilder extends BackendListenerCodeBuilder {

    public CodeBuilder(List<Method> builderMethods) {
      super(GraphiteBackendListenerClient.class, builderMethods);
    }

    @Override
    protected MethodCall buildBackendListenerCall(Map<String, String> args,
        Map<String, String> defaultValues) {
      String port = args.get(PORT_ARG);
      return buildMethodCall(
          new StringParam(
              args.get(HOST_ARG) + (!String.valueOf(PICKLE_PORT).equals(port) ? ":" + port : "")))
          .chain("metricsPrefix", buildArgParam(PREFIX_ARG, args, defaultValues));
    }

  }

}
