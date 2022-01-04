package us.abstracta.jmeter.javadsl.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Values;

public class JmeterUrlTest {

  @CartesianTest
  public void shouldGetExpectedParameterizedUrlWhenBuildFromString(
      @Values(strings = {"https", "${protocol}"}) String protocol,
      @Values(strings = {"myservice.com", "${domain}", "${domain}.myservice.com"}) String host,
      @Values(strings = {"", "443", "${port}"}) String port,
      @Values(strings = {"", "/my/path", "/my/${path}", "/${path}"}) String path) {
    String urlString = buildUrlString(protocol, host, port, path);
    JmeterUrl url = JmeterUrl.valueOf(urlString);
    assertThat(url).isEqualTo(new JmeterUrl(protocol, host, port, path.isEmpty() ? "/" : path));
  }

  private String buildUrlString(String protocol, String host, String port, String path) {
    return protocol + "://" + host + (port.isEmpty() ? "" : ":" + port) + path;
  }

  @Test
  public void shouldGetExpectedParameterizedUrlWhenBuildFromStringWithUniqueExpression() {
    String expression = "${path}";
    assertThat(JmeterUrl.valueOf(expression)).isEqualTo(
        new JmeterUrl(null, null, null, expression));
  }

}
