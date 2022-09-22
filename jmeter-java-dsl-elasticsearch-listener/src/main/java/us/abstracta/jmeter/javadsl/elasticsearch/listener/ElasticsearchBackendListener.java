package us.abstracta.jmeter.javadsl.elasticsearch.listener;

import io.github.delirius325.jmeter.backendlistener.elasticsearch.ElasticsearchBackendClient;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.apache.jmeter.config.Arguments;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;
import us.abstracta.jmeter.javadsl.core.listeners.DslBackendListener;
import us.abstracta.jmeter.javadsl.http.JmeterUrl;

/**
 * Test element which publishes all test run metrics to an Elasticsearch instance.
 *
 * @since 0.20
 */
public class ElasticsearchBackendListener extends DslBackendListener<ElasticsearchBackendListener> {

  private static final String SCHEME_ARG = "es.scheme";
  private static final String HOST_ARG = "es.host";
  private static final String PORT_ARG = "es.port";
  private static final String INDEX_ARG = "es.index";
  private static final String USER_ARG = "es.xpack.user";
  private static final String PASSWORD_ARG = "es.xpack.password";

  protected String username;
  protected String password;

  public ElasticsearchBackendListener(String url) {
    super(ElasticsearchBackendClient.class, url);
  }

  /**
   * Creates a new Elasticsearch Backend listener posting sample result metrics to a given
   * Elasticsearch index.
   *
   * @param url specifies scheme, host, port and index to post sample results metrics to. E.g.:
   *            http://localhost:9200/jmeter
   * @return the ElasticsearchBackendListener instance for fluent API usage.
   */
  public static ElasticsearchBackendListener elasticsearchListener(String url) {
    return new ElasticsearchBackendListener(url);
  }

  /**
   * Allows specifying username and password to authenticate against Elasticsearch.
   *
   * @param username to use to authenticate to InfluxDB
   * @param password to use to authenticate to InfluxDB
   * @return the listener for further configuration or usage.
   */
  public ElasticsearchBackendListener credentials(String username, String password) {
    this.username = username;
    this.password = password;
    return this;
  }

  @Override
  protected Arguments buildListenerArguments() {
    Arguments ret = new Arguments();
    URI uri = URI.create(url);
    String scheme = uri.getScheme();
    int port = uri.getPort();
    if (port == -1) {
      port = "https".equals(scheme) ? 443 : 80;
    }
    ret.addArgument(SCHEME_ARG, uri.getScheme());
    ret.addArgument(HOST_ARG, uri.getHost());
    ret.addArgument(PORT_ARG, String.valueOf(port));
    ret.addArgument(INDEX_ARG, uri.getPath().substring(1));
    ret.addArgument(USER_ARG, username);
    ret.addArgument(PASSWORD_ARG, password);
    return ret;
  }

  public static class CodeBuilder extends BackendListenerCodeBuilder {

    public CodeBuilder(List<Method> builderMethods) {
      super(ElasticsearchBackendClient.class, builderMethods);
    }

    @Override
    protected MethodCall buildBackendListenerCall(Map<String, String> args,
        Map<String, String> defaultValues) {
      String scheme = args.get(SCHEME_ARG);
      String port = args.get(PORT_ARG);
      if ("80".equals(port) && "http".equals(scheme) || "443".equals(port) && "https".equals(
          scheme)) {
        port = "";
      }
      String url = new JmeterUrl(args.get(SCHEME_ARG), args.get(HOST_ARG), port,
          "/" + args.get(INDEX_ARG)).toString();
      return buildMethodCall(new StringParam(url))
          .chain("credentials", new StringParam(args.get(USER_ARG)),
              new StringParam(args.get(PASSWORD_ARG)));
    }

  }

}
