package us.abstracta.jmeter.javadsl.elasticsearch.listener;

import io.github.delirius325.jmeter.backendlistener.elasticsearch.ElasticsearchBackendClient;
import java.net.URI;
import org.apache.jmeter.config.Arguments;
import us.abstracta.jmeter.javadsl.core.listeners.DslBackendListener;

/**
 * Test element which publishes all test run metrics to an Elasticsearch instance.
 *
 * @since 0.20
 */
public class ElasticsearchBackendListener extends DslBackendListener {

  private String username;
  private String password;

  public ElasticsearchBackendListener(String url) {
    super(ElasticsearchBackendClient.class, url);
  }

  /**
   * Creates a new Elasticsearch Backend listener posting sample result metrics to a given
   * Elasticsearch index.
   *
   * @param url specifies scheme, host, port and index to post sample results metrics to. E.g.:
   * http://localhost:9200/jmeter
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
   * @return this instance for fluent API usage.
   */
  public ElasticsearchBackendListener credentials(String username, String password) {
    this.username = username;
    this.password = password;
    return this;
  }

  /**
   * Specifies the length of sample results queue used to asynchronously send the information to
   * Elasticsearch.
   *
   * @param queueSize the size of the queue to use
   * @return this instance for fluent API usage.
   * @see #setQueueSize(int)
   */
  public ElasticsearchBackendListener queueSize(int queueSize) {
    setQueueSize(queueSize);
    return this;
  }

  @Override
  protected Arguments buildListenerArguments() {
    Arguments ret = new Arguments();
    URI uri = URI.create(url);
    ret.addArgument("es.scheme", uri.getScheme());
    ret.addArgument("es.host", uri.getHost());
    ret.addArgument("es.port", String.valueOf(uri.getPort()));
    ret.addArgument("es.index", uri.getPath().substring(1));
    ret.addArgument("es.xpack.user", username);
    ret.addArgument("es.xpack.password", password);
    return ret;
  }

}
