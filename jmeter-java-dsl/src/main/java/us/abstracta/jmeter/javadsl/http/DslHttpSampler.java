package us.abstracta.jmeter.javadsl.http;

import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PreProcessor;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPConstantsInterface;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.testelement.property.JMeterProperty;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.BoolParam;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.StringParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorScript;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorVars;
import us.abstracta.jmeter.javadsl.core.util.JmeterFunction;

/**
 * Allows to configure a JMeter HTTP sampler to make HTTP requests in a test plan.
 *
 * @since 0.1
 */
public class DslHttpSampler extends DslBaseHttpSampler<DslHttpSampler> {

  private static final String DEFAULT_NAME = "HTTP Request";
  private String method = HTTPConstants.GET;
  private final List<HTTPArgument> arguments = new ArrayList<>();
  private String body;
  private boolean multiPart;
  private final List<HTTPFileArg> files = new ArrayList<>();
  private Charset encoding;
  private boolean followRedirects = true;
  private boolean downloadEmbeddedResources;
  private HttpClientImpl clientImpl;

  public DslHttpSampler(String name, String url) {
    super(name != null ? name : DEFAULT_NAME, url, HttpTestSampleGui.class);
  }

  public DslHttpSampler(String name, Function<PreProcessorVars, String> urlSupplier) {
    this(name, (String) null);
    String variableName = "PRE_PROCESSOR_URL";
    this.path = JmeterFunction.var(variableName);
    children(
        jsr223PreProcessor(s -> s.vars.put(variableName, urlSupplier.apply(s))
        ));
  }

  /**
   * Specifies that the sampler should send an HTTP POST to defined URL.
   *
   * @param body        to include in HTTP POST request body.
   * @param contentType to be sent as Content-Type header in HTTP POST request.
   * @return the altered sampler to allow for fluent API usage.
   * @since 0.42
   */
  public DslHttpSampler post(String body, ContentType contentType) {
    return method(HTTPConstants.POST)
        .contentType(contentType)
        .body(body);
  }

  /**
   * Same as {@link #post(String, ContentType)} but allowing to use a dynamically calculated body.
   * <p>
   * This method is just an abstraction that uses a JMeter variable as HTTP request body and
   * calculates the variable with a jsr223PreProcessor.
   * <p>
   * <b>WARNING:</b> As this method internally uses
   * {@link JmeterDsl#jsr223PreProcessor(PreProcessorScript)}, same limitations and considerations
   * apply. Check its documentation. To avoid such limitations you may use
   * {@link #post(String, ContentType)} with a JMeter variable instead, and dynamically set the
   * variable with {@link JmeterDsl#jsr223PreProcessor(String)}.
   *
   * @param bodySupplier function to calculate the body on each request.
   * @param contentType  to be sent as Content-Type header in HTTP POST request.
   * @return the altered sampler to allow for fluent API usage.
   * @see #body(Function)
   * @since 0.42
   */
  public DslHttpSampler post(Function<PreProcessorVars, String> bodySupplier,
      ContentType contentType) {
    return method(HTTPConstants.POST)
        .contentType(contentType)
        .body(bodySupplier);
  }

  /**
   * Specifies the HTTP method to be used in the HTTP request generated by the sampler.
   *
   * @param method is the HTTP method to be used by the sampler.
   * @return the altered sampler to allow for fluent API usage.
   * @since 0.42
   */
  public DslHttpSampler method(String method) {
    this.method = method;
    return this;
  }

  /**
   * Specifies the body to be sent in the HTTP request generated by the sampler.
   *
   * @param body to be used as in the body of the HTTP request.
   * @return the altered sampler to allow for fluent API usage.
   */
  public DslHttpSampler body(String body) {
    this.body = body;
    return this;
  }

  /**
   * Same as {@link #body(String)} but allows using dynamically calculated HTTP request body.
   * <p>
   * This method is just an abstraction that uses a JMeter variable as HTTP request body and
   * calculates the variable with a jsr223PreProcessor.
   * <p>
   * <b>WARNING:</b> As this method internally uses
   * {@link JmeterDsl#jsr223PreProcessor(PreProcessorScript)}, same limitations and considerations
   * apply. Check its documentation.  To avoid such limitations you may use {@link #body(String)}
   * with a JMeter variable instead, and dynamically set the variable with
   * {@link JmeterDsl#jsr223PreProcessor(String)}.
   *
   * @param bodySupplier function to calculate the body on each request.
   * @return the altered sampler to allow for fluent API usage.
   * @since 0.10
   */
  public DslHttpSampler body(Function<PreProcessorVars, String> bodySupplier) {
    String variableName = "PRE_PROCESSOR_REQUEST_BODY";
    this.body = JmeterFunction.var(variableName);
    return children(
        jsr223PreProcessor(s -> s.vars.put(variableName, bodySupplier.apply(s))));
  }

  /**
   * Specifies a file to be sent as body of the request.
   * <p>
   * This method is useful to send binary data in request (eg: uploading an image to a server).
   *
   * @param filePath is path to the file to be sent as request body.
   * @return the altered sampler to allow for fluent API usage.
   * @since 0.44
   */
  public DslHttpSampler bodyFile(String filePath) {
    files.add(new HTTPFileArg(filePath, "", ""));
    return this;
  }

  /**
   * Allows specifying a query parameter or url encoded form body parameter.
   * <p>
   * JMeter will automatically URL encode provided parameters names and values. Use
   * {@link #rawParam(String, String)} to send parameters values which are already encoded and
   * should be sent as is by JMeter.
   * <p>
   * JMeter will use provided parameter in query string if method is GET, DELETE or OPTIONS,
   * otherwise it will use them in url encoded form body.
   * <p>
   * If you set a parameter with empty string name, it results in same behavior as using
   * {@link #body(String)} method. In general, you either use body function or parameters functions,
   * but don't use both of them in same sampler.
   *
   * @param name  specifies the name of the parameter.
   * @param value specifies the value of the parameter to be URL encoded to include in URL
   * @return the altered sampler to allow for fluent API usage.
   * @since 0.42
   */
  public DslHttpSampler param(String name, String value) {
    arguments.add(new HTTPArgument(name, value));
    return this;
  }

  /**
   * Same as {@link #param(String, String)} but param name and value will be sent with no additional
   * encoding.
   *
   * @see #param(String, String)
   * @since 0.42
   * @deprecated as of 0.54 use {@link #rawParam(String, String)} instead which avoids some
   * confusion.
   */
  public DslHttpSampler encodedParam(String name, String value) {
    return rawParam(name, value);
  }

  /**
   * Same as {@link #param(String, String)} but param name and value will be sent with no additional
   * encoding.
   *
   * @see #param(String, String)
   * @since 0.54
   */
  public DslHttpSampler rawParam(String name, String value) {
    HTTPArgument arg = new HTTPArgument(name, value);
    arg.setAlwaysEncoded(false);
    arguments.add(arg);
    return this;
  }

  /**
   * Specifies a part of a multipart form body.
   * <p>
   * In general, samplers should not use this method in combination with
   * {@link #param(String, String)} or {@link #rawParam(String, String)}.
   *
   * @param name        specifies the name of the part.
   * @param value       specifies the string to be sent in the part.
   * @param contentType specifies the content-type associated to the part.
   * @return the altered sampler to allow for fluent API usage.
   * @since 0.42
   */
  public DslHttpSampler bodyPart(String name, String value, ContentType contentType) {
    multiPart = true;
    HTTPArgument arg = new HTTPArgument(name, value);
    arg.setContentType(contentType.toString());
    arguments.add(arg);
    return this;
  }

  /**
   * Specifies a file to be sent in a multipart form body.
   *
   * @param name        is the name to be assigned to the file part.
   * @param filePath    is path to the file to be sent in the multipart form body.
   * @param contentType the content type associated to the part.
   * @return the altered sampler to allow for fluent API usage.
   * @since 0.42
   */
  public DslHttpSampler bodyFilePart(String name, String filePath, ContentType contentType) {
    multiPart = true;
    files.add(new HTTPFileArg(filePath, name, contentType.toString()));
    return this;
  }

  /**
   * Specifies the charset to be used to encode URLs and request contents.
   *
   * @param encoding contains the charset to be used.
   * @return the altered sampler to allow for fluent API usage.
   * @since 0.39
   */
  public DslHttpSampler encoding(Charset encoding) {
    this.encoding = encoding;
    return this;
  }

  /**
   * Allows enabling/disabling automatic request for redirects.
   * <p>
   * When a response is a redirection response (3xx status code with a Location header), JMeter
   * automatically generates a new request to the redirected destination registering the redirect
   * request as a sub sample. This method allows enabling/disabling such behavior.
   *
   * @param followRedirects sets either to enable or disable automatic redirects. By default,
   *                        redirects are automatically followed.
   * @return the altered sampler to allow for fluent API usage.
   * @since 0.21
   */
  public DslHttpSampler followRedirects(boolean followRedirects) {
    this.followRedirects = followRedirects;
    return this;
  }

  /**
   * Allows enabling automatic download of HTML embedded resources (images, iframes, etc).
   * <p>
   * When enabled JMeter will automatically parse HTMLs and download any found embedded resources
   * adding their information as sub samples of the original request.
   * <p>
   * Additionally, and in contrast to JMeter, this will download embedded resources in parallel by
   * default (with up to 6 parallel downloads). The DSL enables this behavior by default since it is
   * the most common way to use it to properly emulate browsers behavior.
   * <p>
   * Check <a
   * href="https://jmeter.apache.org/usermanual/component_reference.html#HTTP_Request">JMeter HTTP
   * Request documentation</a> for additional details on embedded resources download.
   *
   * @return the altered sampler to allow for fluent API usage.
   * @since 0.24
   */
  public DslHttpSampler downloadEmbeddedResources() {
    this.downloadEmbeddedResources = true;
    return this;
  }

  /**
   * Allows specifying the HTTP client implementation to use for this particular sampler.
   * <p>
   * Changing the default implementation ({@link DslHttpSampler.HttpClientImpl#HTTP_CLIENT}) to
   * {@link DslHttpSampler.HttpClientImpl#JAVA} may improve performance in some scenarios
   * (connection time, memory, cpu usage). But, Java implementation has its own limitations, check
   * <a href="https://jmeter.apache.org/usermanual/component_reference.html#HTTP_Request">JMeter
   * documentation</a> for more details.
   *
   * @param clientImpl the HTTP client implementation to use. If none is specified, then
   *                   {@link DslHttpSampler.HttpClientImpl#HTTP_CLIENT} is used.
   * @return the altered sampler to allow for fluent API usage.
   * @since 0.39
   */
  public DslHttpSampler clientImpl(HttpClientImpl clientImpl) {
    this.clientImpl = clientImpl;
    return this;
  }

  @Override
  public HTTPSamplerProxy configureHttpTestElement(HTTPSamplerProxy elem) {
    elem.setMethod(method);
    elem.setArguments(buildArguments());
    if (multiPart) {
      elem.setDoMultipart(true);
    }
    elem.setHTTPFiles(files.toArray(new HTTPFileArg[0]));
    if (encoding != null) {
      elem.setContentEncoding(encoding.toString());
    }
    elem.setFollowRedirects(followRedirects);
    elem.setUseKeepAlive(true);
    if (downloadEmbeddedResources) {
      elem.setImageParser(true);
      elem.setConcurrentDwn(true);
    }
    if (clientImpl != null) {
      elem.setImplementation(clientImpl.propertyValue);
    }
    return elem;
  }

  private Arguments buildArguments() {
    Arguments args = new Arguments();
    if (body != null) {
      HTTPArgument arg = new HTTPArgument("", body, false);
      arg.setAlwaysEncoded(false);
      args.addArgument(arg);
    }
    arguments.forEach(args::addArgument);
    return args;
  }

  /**
   * Specifies an HTTP client implementation to be used by HTTP samplers.
   */
  public enum HttpClientImpl {
    /**
     * Specifies to use the Java implementation.
     */
    JAVA("Java"),
    /**
     * Specifies to use the Apache HttpClient implementation. This is the default one and usually
     * the preferred one.
     */
    HTTP_CLIENT("HttpClient4");

    private static final Map<String, HttpClientImpl> IMPLS_BY_PROPERTY_VALUE = Arrays.stream(
        values()).collect(Collectors.toMap(v -> v.propertyValue, v -> v));

    public final String propertyValue;

    HttpClientImpl(String propertyValue) {
      this.propertyValue = propertyValue;
    }

    public static HttpClientImpl fromPropertyValue(String propertyValue) {
      if (propertyValue.isEmpty()) {
        return null;
      }
      HttpClientImpl ret = IMPLS_BY_PROPERTY_VALUE.get(propertyValue);
      if (ret == null) {
        throw new IllegalArgumentException(
            "Unknown " + HttpClientImpl.class.getSimpleName() + " property value: "
                + propertyValue);
      }
      return ret;
    }

  }

  public static class CodeBuilder extends BaseHttpSamplerCodeBuilder {

    public CodeBuilder(List<Method> builderMethods) {
      super(DEFAULT_NAME, HttpTestSampleGui.class, builderMethods);
    }

    @Override
    protected MethodCall buildBaseHttpMethodCall(StringParam name, StringParam url,
        TestElementParamBuilder paramBuilder) {
      return buildMethodCall(name, url);
    }

    @Override
    protected void setAdditionalOptions(MethodCall ret, TestElementParamBuilder paramBuilder) {
      ret.chain("encoding", EncodingParam.from(paramBuilder))
          .chain("followRedirects", buildFollowRedirectsParam(paramBuilder))
          .chain("downloadEmbeddedResources",
              paramBuilder.boolParam(HTTPSamplerBase.IMAGE_PARSER, false))
          .chain("clientImpl", ClientImplParam.from(paramBuilder));
    }

    @Override
    protected void buildRequestCall(MethodCall ret, HTTPSamplerProxy testElem,
        MethodCallContext buildContext) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElem);
      HttpMethodParam method = new HttpMethodParam(paramBuilder);
      MethodCallContext headers = buildContext.removeChild(HeaderManager.class);
      String contentType = removeContentTypeHeader(headers);
      Arguments args = testElem.getArguments();
      if (!method.isDefault() && HTTPConstants.POST.equals(method.getValue().toUpperCase(Locale.US))
          && !testElem.getUseMultipart() && contentType != null && isRawBody(args)) {
        ret.chain("post", buildRawBody(args), new ContentTypeParam(contentType));
        chainHeaders(ret, headers);
        return;
      }
      ret.chain("method", method);
      if (contentType != null) {
        chainContentType(ret, contentType);
      }
      chainHeaders(ret, headers);
      if (isRawBody(args)) {
        ret.chain("body", buildRawBody(args));
      } else if (testElem.getSendFileAsPostBody()) {
        HTTPFileArg file = testElem.getHTTPFiles()[0];
        if (file.getMimeType() != null && !file.getMimeType().isEmpty()) {
          chainContentType(ret, file.getMimeType());
        }
        ret.chain("bodyFile", new StringParam(file.getPath()));
      } else if (testElem.getUseMultipart()) {
        for (JMeterProperty prop : args) {
          HTTPArgument arg = (HTTPArgument) prop.getObjectValue();
          ret.chain("bodyPart", new StringParam(arg.getName()), new StringParam(arg.getValue()),
              new ContentTypeParam(arg.getContentType()));
        }
        for (HTTPFileArg file : testElem.getHTTPFiles()) {
          ret.chain("bodyFilePart", new StringParam(file.getParamName()),
              new StringParam(file.getPath()), new ContentTypeParam(file.getMimeType()));
        }
      } else {
        for (JMeterProperty prop : args) {
          HTTPArgument arg = (HTTPArgument) prop.getObjectValue();
          if (arg.isAlwaysEncoded()) {
            ret.chain("param", new StringParam(arg.getName()), new StringParam(arg.getValue()));
          } else {
            ret.chain("rawParam", new StringParam(arg.getName()),
                new StringParam(arg.getValue()));
          }
        }
      }
    }

    private boolean isRawBody(Arguments args) {
      return args.getArgumentCount() == 1 && args.getArgument(0).getName().isEmpty();
    }

    private StringParam buildRawBody(Arguments args) {
      return new StringParam(args.getArgument(0).getValue());
    }

    private BoolParam buildFollowRedirectsParam(TestElementParamBuilder paramBuilder) {
      BoolParam follow = paramBuilder.boolParam(HTTPSamplerBase.FOLLOW_REDIRECTS, true);
      if (!follow.isDefault()) {
        return follow;
      } else {
        BoolParam auto = paramBuilder.boolParam(HTTPSamplerBase.AUTO_REDIRECTS, false);
        return Boolean.TRUE.equals(auto.getValue())
            ? new BoolParam(true, true)
            : follow;
      }
    }

    private static class HttpMethodParam extends StringParam {

      private static final Map<String, String> CONSTANT_METHODS = findConstantNames(
          HTTPConstantsInterface.class, String.class,
          f -> {
            try {
              String value = (String) f.get(null);
              return !HTTPConstants.HTTP_1_1.equals(value) && value.equals(
                  value.toUpperCase(Locale.US));
            } catch (IllegalAccessException e) {
              /*
               this should never happen since the predicate is only applied to public static fields
               */
              throw new RuntimeException(e);
            }
          });

      private HttpMethodParam(TestElementParamBuilder paramBuilder) {
        super(paramBuilder.prop(HTTPSamplerBase.METHOD).getStringValue(), HTTPConstants.GET);
      }

      @Override
      public boolean isDefault() {
        return value == null || value.isEmpty() || defaultValue != null && defaultValue.equals(
            value.toUpperCase(Locale.US));
      }

      @Override
      public String buildSpecificCode(String indent) {
        String constant = CONSTANT_METHODS.get(value != null ? value.toUpperCase(Locale.US) : null);
        return constant != null ? HTTPConstants.class.getSimpleName() + "." + constant
            : super.buildCode(indent);
      }

    }

  }

}
