package us.abstracta.jmeter.javadsl.http;

import java.time.Duration;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler.HttpClientImpl;

/**
 * Contains common logic used by HTTP samplers and config elements.
 */
/*
This class is implemented just as a collection of static methods because:
- Using as a super and abstract class (containing common fields) would require HTTP sampler to re
implement base sampler logic due to java single inheritance restriction.
- Using it as a helper class to be instantiated and containing common fields would require to
remove existing fields from HTTP sampler and config elements which would break compatibility
with existing users that might be extending one of these classes. In a future major release we may
explore doing this refactoring to reduce duplication of fileds definition and handling.
*/
public class HttpElementHelper {

  public static void modifyTestElementUrl(TestElement elem, String protocol, String host,
      String port, String path) {
    if (protocol != null) {
      elem.setProperty(HTTPSamplerBase.PROTOCOL, protocol);
    }
    if (host != null) {
      elem.setProperty(HTTPSamplerBase.DOMAIN, host);
    }
    if (port != null) {
      elem.setProperty(HTTPSamplerBase.PORT, port);
    }
    if (path != null) {
      elem.setProperty(HTTPSamplerBase.PATH, path);
    }
  }

  public static void modifyTestElementEmbeddedResources(TestElement ret, boolean enabled,
      String matchRegex, String notMatchRegex) {
    if (enabled) {
      ret.setProperty(HTTPSamplerBase.IMAGE_PARSER, true);
      ret.setProperty(HTTPSamplerBase.CONCURRENT_DWN, true);
      if (matchRegex != null) {
        ret.setProperty(HTTPSamplerBase.EMBEDDED_URL_RE, matchRegex);
      }
      if (notMatchRegex != null) {
        ret.setProperty(HTTPSamplerBase.EMBEDDED_URL_EXCLUDE_RE, notMatchRegex);
      }
    }
  }

  public static void modifyTestElementTimeouts(TestElement ret, Duration connectionTimeout,
      Duration responseTimeout) {
    if (connectionTimeout != null) {
      ret.setProperty(HTTPSamplerBase.CONNECT_TIMEOUT, connectionTimeout.toMillis());
    }
    if (responseTimeout != null) {
      ret.setProperty(HTTPSamplerBase.RESPONSE_TIMEOUT, responseTimeout.toMillis());
    }
  }

  public static MethodParam buildUrlParam(MethodParam protocol, MethodParam domain,
      MethodParam port, MethodParam path) {
    if (!domain.isDefault()) {
      return new StringParam(
          new JmeterUrl(protocol.getExpression(), domain.getExpression(), port.getExpression(),
              path.isDefault() ? "" : path.getExpression()).toString());
    } else {
      return path;
    }
  }

  public static void modifyTestElementProxy(TestElement ret, String url, String user,
      String password) {
    if (url != null) {
      JmeterUrl parsedUrl = JmeterUrl.valueOf(url);
      ret.setProperty(HTTPSamplerBase.PROXYSCHEME, parsedUrl.protocol());
      ret.setProperty(HTTPSamplerBase.PROXYHOST, parsedUrl.host());
      ret.setProperty(HTTPSamplerBase.PROXYPORT, parsedUrl.port());
      if (user != null) {
        ret.setProperty(HTTPSamplerBase.PROXYUSER, user);
      }
      if (password != null) {
        ret.setProperty(HTTPSamplerBase.PROXYPASS, password);
      }
    }
  }

  public static void chainConnectionOptionsToMethodCall(MethodCall ret,
      TestElementParamBuilder paramBuilder) {
    chainTimeoutOption(HTTPSamplerBase.CONNECT_TIMEOUT, "connectionTimeout", ret, paramBuilder);
    chainTimeoutOption(HTTPSamplerBase.RESPONSE_TIMEOUT, "responseTimeout", ret, paramBuilder);
    chainProxyOptions(ret, paramBuilder);
  }

  private static void chainTimeoutOption(String propertyName, String methodName, MethodCall ret,
      TestElementParamBuilder paramBuilder) {
    MethodParam connectionTimeout = paramBuilder.durationParamMillis(
        propertyName, null);
    if (!connectionTimeout.isDefault()) {
      ret.chain(methodName, connectionTimeout);
    }
  }

  private static void chainProxyOptions(MethodCall ret, TestElementParamBuilder paramBuilder) {
    MethodParam protocol = paramBuilder.stringParam(HTTPSamplerBase.PROXYSCHEME);
    MethodParam host = paramBuilder.stringParam(HTTPSamplerBase.PROXYHOST);
    MethodParam port = paramBuilder.intParam(HTTPSamplerBase.PROXYPORT);
    MethodParam user = paramBuilder.stringParam(HTTPSamplerBase.PROXYUSER);
    MethodParam password = paramBuilder.stringParam(HTTPSamplerBase.PROXYPASS);
    if (host.isDefault()) {
      return;
    }
    MethodParam proxyUrl = HttpElementHelper.buildUrlParam(protocol, host,
        port.isDefault() ? new StringParam("") : port, new StringParam(""));
    if (user.isDefault()) {
      ret.chain("proxy", proxyUrl);
    } else {
      ret.chain("proxy", proxyUrl, user, password);
    }
  }

  public static void chainEncodingToMethodCall(MethodCall ret,
      TestElementParamBuilder paramBuilder) {
    ret.chain("encoding", paramBuilder.encodingParam(HTTPSamplerBase.CONTENT_ENCODING, null));
  }

  public static void chainEmbeddedResourcesOptionsToMethodCall(MethodCall ret,
      TestElementParamBuilder paramBuilder) {
    MethodParam enabled = paramBuilder.boolParam(HTTPSamplerBase.IMAGE_PARSER, false);
    if (!enabled.isDefault()) {
      boolean chained = false;
      MethodParam matching = paramBuilder.stringParam(HTTPSamplerBase.EMBEDDED_URL_RE);
      if (!matching.isDefault()) {
        ret.chain("downloadEmbeddedResourcesMatching", matching);
        chained = true;
      }
      MethodParam notMatching = paramBuilder.stringParam(HTTPSamplerBase.EMBEDDED_URL_EXCLUDE_RE);
      if (!notMatching.isDefault()) {
        ret.chain("downloadEmbeddedResourcesNotMatching", notMatching);
        chained = true;
      }
      if (!chained) {
        ret.chain("downloadEmbeddedResources", enabled);
      }
    }
  }

  public static void chainClientImplToMethodCall(MethodCall ret,
      TestElementParamBuilder paramBuilder) {
    ret.chain("clientImpl",
        paramBuilder.enumParam(HTTPSamplerBase.IMPLEMENTATION, HttpClientImpl.HTTP_CLIENT));
  }

}
