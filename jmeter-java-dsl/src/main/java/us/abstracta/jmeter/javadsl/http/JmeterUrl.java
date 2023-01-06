package us.abstracta.jmeter.javadsl.http;

import java.util.Objects;

/**
 * Allows parsing url components without failing when JMeter expressions are used on them.
 * <p>
 * We need to use this class instead of URI, since URI.create fails when a jmeter expression (like
 * {$MY_VAR}) is contained in URL.
 *
 * @since 0.42
 */
public class JmeterUrl {

  public static final String SCHEME_DELIMITER = "://";
  private final String protocol;
  private final String host;
  private final String port;
  private final String path;

  public JmeterUrl(String protocol, String host, String port, String path) {
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.path = path;
  }

  public static JmeterUrl valueOf(String url) {
    int pos = url.indexOf(SCHEME_DELIMITER);
    if (pos < 0) {
      return new JmeterUrl(null, null, null, url);
    }
    String protocol = url.substring(0, pos);
    url = url.substring(pos + SCHEME_DELIMITER.length());
    String authority = url;
    String path = "/";
    pos = url.indexOf("/");
    if (pos >= 0) {
      authority = url.substring(0, pos);
      path = url.substring(pos);
    }
    pos = authority.indexOf(":");
    String host = authority;
    String port = "";
    if (pos >= 0) {
      host = authority.substring(0, pos);
      port = authority.substring(pos + 1);
    }
    return new JmeterUrl(protocol, host, port, path);
  }

  public String protocol() {
    return protocol;
  }

  public String host() {
    return host;
  }

  public String port() {
    return port;
  }

  public String path() {
    return path;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JmeterUrl that = (JmeterUrl) o;
    return Objects.equals(protocol, that.protocol) && Objects.equals(host,
        that.host) && Objects.equals(port, that.port) && Objects.equals(path,
        that.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(protocol, host, port, path);
  }

  @Override
  public String toString() {
    if (isNullOrEmpty(host)) {
      return path;
    } else {
      return (isNullOrEmpty(protocol) ? "http" : protocol) + SCHEME_DELIMITER + host
          + (isNullOrEmpty(port) || "0".equals(port) ? "" : ":" + port)
          + (isNullOrEmpty(path) ? "" : path);
    }
  }

  boolean isNullOrEmpty(String str) {
    return str == null || str.isEmpty();
  }

}
