package us.abstracta.jmeter.javadsl.http;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class ProxyServer {

  private final Server server = new Server(0);
  private String credentials;
  private volatile boolean proxiedRequest;

  public ProxyServer auth(String username, String password) {
    this.credentials = Base64.getEncoder()
        .encodeToString((username + ":" + password).getBytes(StandardCharsets.ISO_8859_1));
    return this;
  }

  public void start() throws Exception {
    AbstractHandler proxyHandler = new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request,
          HttpServletResponse response) {
        baseRequest.setHandled(true);
        // if the request is direct to the server and not a proxied request
        if (baseRequest.getHttpURI().getPort() == server.getURI().getPort()) {
          response.setStatus(HttpServletResponse.SC_USE_PROXY);
          return;
        }

        if (credentials == null) {
          handleProxiedRequest(response);
          return;
        }

        String authorization = request.getHeader(HttpHeader.PROXY_AUTHORIZATION.asString());
        if (authorization == null) {
          response.setStatus(HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED);
          response.setHeader(HttpHeader.PROXY_AUTHENTICATE.asString(), "Basic realm=\"test\"");
          return;
        }

        String method = "Basic ";
        if (authorization.startsWith(method)) {
          String requestCredentials = authorization.substring(method.length());
          if (credentials.equals(requestCredentials)) {
            handleProxiedRequest(response);
            return;
          }
        }
        response.setStatus(HttpStatus.SC_FORBIDDEN);
      }

      private void handleProxiedRequest(HttpServletResponse response) {
        proxiedRequest = true;
        response.setStatus(HttpStatus.SC_NO_CONTENT);
      }

    };
    server.setHandler(proxyHandler);
    server.start();
  }

  public String url() {
    return server.getURI().toString();
  }

  public boolean proxiedRequest() {
    return proxiedRequest;
  }

  public void stop() throws Exception {
    server.stop();
  }

}