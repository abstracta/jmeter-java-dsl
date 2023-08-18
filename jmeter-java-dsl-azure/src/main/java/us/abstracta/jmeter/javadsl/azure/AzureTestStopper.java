package us.abstracta.jmeter.javadsl.azure;

import static us.abstracta.jmeter.javadsl.JmeterDsl.vars;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.azure.api.Secret;
import us.abstracta.jmeter.javadsl.azure.api.Secret.SecretType;
import us.abstracta.jmeter.javadsl.azure.api.TestRun;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.engines.AutoStoppedTestException;
import us.abstracta.jmeter.javadsl.core.engines.BaseTestStopper;
import us.abstracta.jmeter.javadsl.core.util.JmeterFunction;

public class AzureTestStopper extends BaseTestStopper {

  private static final String VAR_PREFIX = "AZURE_JMETERDSL_";
  private static final String TENANT_ID_VAR = VAR_PREFIX + "TENANT_ID";
  private static final String CLIENT_ID_VAR = VAR_PREFIX + "CLIENT_ID";
  private static final String CLIENT_SECRET_VAR = VAR_PREFIX + "CLIENT_SECRET";
  private static final String TEST_RUN_URL_VAR = VAR_PREFIX + "TEST_RUN_URL";
  private static final String TEST_ID_VAR = VAR_PREFIX + "TEST_ID";
  private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile(
      "\"access_token\":\"([^\"]+)\"");
  private static final String STOP_MESSAGE_VAR = VAR_PREFIX + "STOP_MESSAGE";

  public static void addClientSecretVariableToTree(HashTree tree, BuildTreeContext context) {
    HashTree testPlanTree = tree.values().iterator().next();
    context.buildChild(
        vars().set(CLIENT_SECRET_VAR, JmeterFunction.from("__GetSecret", CLIENT_SECRET_VAR)),
        testPlanTree);
  }

  public static void setupTestRun(TestRun testRun, String tenantId, String clientId,
      String clientSecret, String baseUrl) {
    Map<String, String> envVars = new HashMap<>();
    envVars.put(TENANT_ID_VAR, tenantId);
    envVars.put(CLIENT_ID_VAR, clientId);
    envVars.put(TEST_RUN_URL_VAR, String.format("%s/test-runs/%s", baseUrl, testRun.getId()));
    envVars.put(TEST_ID_VAR, testRun.getTestId());
    testRun.setEnvironmentVariables(envVars);
    testRun.setSecrets(Collections.singletonMap(CLIENT_SECRET_VAR,
        new Secret(SecretType.SECRET_VALUE, clientSecret)));
  }

  @Override
  protected void stopTestExecution() {
    StandardJMeterEngine.stopEngine();
    try {
      Map<String, String> env = System.getenv();
      String tenantId = env.get(TENANT_ID_VAR);
      String clientId = env.get(CLIENT_ID_VAR);
      String testRunUrl = env.get(TEST_RUN_URL_VAR);
      String testId = env.get(TEST_ID_VAR);
      String clientSecret = urlEncode(
          JMeterContextService.getContext().getVariables().get(CLIENT_SECRET_VAR));
      try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
        String auth = "Bearer " + getAccessToken(tenantId, clientId, clientSecret, httpClient);
        try {
          setTestRunStopMessage(getStopMessage(), testRunUrl, testId, tenantId, auth, httpClient);
          stopTestRun(testRunUrl, auth, httpClient);
        } catch (AzureApiException e) {
          if (e.statusCode != 400 || !"TestRunAlreadyFinished".equals(e.getErrorCode())) {
            throw e;
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String urlEncode(String val) throws UnsupportedEncodingException {
    return URLEncoder.encode(val, StandardCharsets.UTF_8.name());
  }

  private String getAccessToken(String tenantId, String clientId, String clientSecret,
      CloseableHttpClient httpClient) throws IOException {
    String resp = httpRequest(new HttpPost(
            String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token", tenantId)),
        HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED, null,
        String.format("scope=https://cnt-prod.loadtesting.azure.com/.default"
            + "&client_id=%s&client_secret=%s"
            + "&grant_type=client_credentials", clientId, clientSecret), httpClient);
    Matcher m = ACCESS_TOKEN_PATTERN.matcher(resp);
    if (!m.find()) {
      throw new IOException("Could not find token in login response: " + resp);
    }
    return m.group(1);
  }

  private String httpRequest(HttpUriRequest req, String contentType, String auth,
      String body, CloseableHttpClient httpClient) throws IOException {
    if (auth != null) {
      req.setHeader(HTTPConstants.HEADER_AUTHORIZATION, auth);
    }
    if (body != null) {
      ((HttpEntityEnclosingRequest) req).setEntity(
          new StringEntity(body, ContentType.create(contentType)));
    }
    try (CloseableHttpResponse response = httpClient.execute(req)) {
      String ret = EntityUtils.toString(response.getEntity());
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode < 200 || statusCode >= 300) {
        throw new AzureApiException(statusCode, ret);
      }
      return ret;
    }
  }

  private void stopTestRun(String testRunUrl, String auth, CloseableHttpClient httpClient)
      throws IOException {
    httpRequest(new HttpPost(String.format("%s:stop?api-version=2022-11-01", testRunUrl)),
        null, auth, null, httpClient);
  }

  private void setTestRunStopMessage(String stopMessage, String testRunUrl, String testId,
      String tenantId, String auth, CloseableHttpClient httpClient) throws IOException {
    httpRequest(
        new HttpPatch(String.format("%s?api-version=2022-11-01&tenantId=%s", testRunUrl, tenantId)),
        "application/merge-patch+json", auth,
        String.format("{\"testId\": \"%s\",\"environmentVariables\": {\"%s\": \"%s\"}}", testId,
            STOP_MESSAGE_VAR,
            stopMessage), httpClient);
  }

  private static class AzureApiException extends IOException {

    private static final Pattern ERROR_CODE_PATTERN = Pattern.compile("\"code\":\"([^\"]+)\"");
    private final int statusCode;
    private final String body;

    private AzureApiException(int statusCode, String body) {
      super("Azure API error status code: " + statusCode + "\n" + body);
      this.statusCode = statusCode;
      this.body = body;
    }

    public String getErrorCode() {
      Matcher m = ERROR_CODE_PATTERN.matcher(body);
      return m.find() ? m.group(1) : null;
    }

  }

  public static void handleTestEnd(TestRun testRun) {
    Map<String, String> envVars = testRun.getEnvironmentVariables();
    if (envVars == null) {
      return;
    }
    String stopMessage = envVars.get(AzureTestStopper.STOP_MESSAGE_VAR);
    if (stopMessage != null) {
      throw new AutoStoppedTestException(stopMessage);
    }
  }

}
