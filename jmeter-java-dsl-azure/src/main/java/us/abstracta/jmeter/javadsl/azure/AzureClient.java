package us.abstracta.jmeter.javadsl.azure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import okhttp3.Interceptor.Chain;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Tag;
import us.abstracta.jmeter.javadsl.azure.api.AppComponents;
import us.abstracta.jmeter.javadsl.azure.api.FileInfo;
import us.abstracta.jmeter.javadsl.azure.api.LoadTest;
import us.abstracta.jmeter.javadsl.azure.api.LoadTestResource;
import us.abstracta.jmeter.javadsl.azure.api.Location;
import us.abstracta.jmeter.javadsl.azure.api.ResourceGroup;
import us.abstracta.jmeter.javadsl.azure.api.ResponseList;
import us.abstracta.jmeter.javadsl.azure.api.Subscription;
import us.abstracta.jmeter.javadsl.azure.api.TestRun;
import us.abstracta.jmeter.javadsl.azure.api.Token;
import us.abstracta.jmeter.javadsl.engines.BaseRemoteEngineApiClient;
import us.abstracta.jmeter.javadsl.util.TestResource;

public class AzureClient extends BaseRemoteEngineApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(AzureClient.class);
  private static final String USER_AGENT = getUserAgent();

  private final String tenantId;
  private final String clientId;
  private final String clientSecret;
  private final LoginApi loginApi;
  private final ManagementApi managementApi;
  private String managementToken;
  private String loadTestToken;
  private LoadTestApi loadTestApi;
  private String dataPlaneUrl;

  public AzureClient(String tenantId, String clientId, String clientSecret) {
    super(LOG);
    this.tenantId = tenantId;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    loginApi = buildApiFor(
        String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/", tenantId),
        LoginApi.class);
    managementApi = buildApiFor("https://management.azure.com/", ManagementApi.class);
  }

  private static String getUserAgent() {
    try {
      String userAgent = System.getProperty("us.abstracta.jmeterdsl.userAgent");
      if (userAgent != null) {
        return userAgent;
      }
      return "jmeter-java-dsl/" + new TestResource(
          "us/abstracta/jmeter/javadsl/version.txt").rawContents();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void configureHttpClient(OkHttpClient.Builder builder) {
    builder.addInterceptor(this::addAgentHeader);
    super.configureHttpClient(builder);
  }

  private Response addAgentHeader(Chain chain) throws IOException {
    Request request = chain.request()
        .newBuilder()
        .header("User-Agent", USER_AGENT)
        .build();
    return chain.proceed(request);
  }

  @Override
  protected String buildAuthorizationHeaderValue(Request request) throws IOException {
    if (request.tag(RequestOrigin.class) == RequestOrigin.LOGIN) {
      return null;
    }
    return "Bearer " + (request.tag(RequestOrigin.class) == RequestOrigin.MANAGEMENT
        ? getFreshManagementToken() : getFreshLoadTestToken());
  }

  private enum RequestOrigin {
    LOGIN, MANAGEMENT
  }

  private String getFreshManagementToken() throws IOException {
    if (managementToken == null || isExpiredToken(managementToken)) {
      managementToken = getNewToken("management");
    }
    return managementToken;
  }

  private String getFreshLoadTestToken() throws IOException {
    if (loadTestToken == null || isExpiredToken(loadTestToken)) {
      loadTestToken = getNewToken("cnt-prod.loadtesting");
    }
    return loadTestToken;
  }

  private boolean isExpiredToken(String token) {
    try {
      String[] chunks = token.split("\\.");
      Decoder decoder = Base64.getUrlDecoder();
      String payload = new String(decoder.decode(chunks[1]), StandardCharsets.UTF_8);
      long expirationEpochSeconds = new ObjectMapper().readTree(payload).get("exp").asLong();
      return Instant.ofEpochSecond(expirationEpochSeconds).isBefore(Instant.now());
    } catch (JsonProcessingException e) {
      LOG.warn("Could not check validity of token", e);
      return false;
    }
  }

  private String getNewToken(String scope) throws IOException {
    String scopeUri = String.format("https://%s.azure.com/.default", scope);
    Token token = execApiCall(
        loginApi.getToken(scopeUri, clientId, clientSecret, "client_credentials",
            RequestOrigin.LOGIN));
    return token.getAccessToken();
  }

  private interface LoginApi {

    @POST("token")
    @FormUrlEncoded
    Call<Token> getToken(@Field("scope") String scope, @Field("client_id") String clientId,
        @Field("client_secret") String clientSecret, @Field("grant_type") String grantType,
        @Tag RequestOrigin tag);

  }

  private interface ManagementApi {

    String DEFAULT_API_VERSION = "?api-version=2020-01-01";
    String RESOURCE_GROUP_API_VERSION = "?api-version=2021-04-01";
    String LOAD_TESTS_PROVIDER = "Microsoft.LoadTestService";
    String LOAD_TESTS_API_VERSION = "?api-version=2022-12-01";

    @GET("subscriptions" + DEFAULT_API_VERSION)
    Call<ResponseList<Subscription>> findSubscriptions(@Tag RequestOrigin tag);

    @GET("subscriptions/{subscriptionId}/locations" + DEFAULT_API_VERSION)
    Call<ResponseList<Location>> findLocations(@Path("subscriptionId") String subscriptionId,
        @Tag RequestOrigin tag);

    @GET("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}"
        + RESOURCE_GROUP_API_VERSION)
    Call<ResourceGroup> findResourceGroup(@Path("subscriptionId") String subscriptionId,
        @Path("resourceGroupName") String name, @Tag RequestOrigin tag);

    @PUT("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}"
        + RESOURCE_GROUP_API_VERSION)
    Call<ResourceGroup> createResourceGroup(@Path("subscriptionId") String subscriptionId,
        @Path("resourceGroupName") String name, @Body ResourceGroup resourceGroup,
        @Tag RequestOrigin tag);

    @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/"
        + LOAD_TESTS_PROVIDER + "/loadTests/{loadTestName}" + LOAD_TESTS_API_VERSION)
    Call<LoadTestResource> findTestResource(
        @Path("subscriptionId") String subscriptionId,
        @Path("resourceGroupName") String resourceGroupName,
        @Path("loadTestName") String loadTestName, @Tag RequestOrigin tag);

    @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/"
        + LOAD_TESTS_PROVIDER + "/loadTests/{loadTestName}" + LOAD_TESTS_API_VERSION)
    Call<LoadTestResource> createTestResource(@Path("subscriptionId") String subscriptionId,
        @Path("resourceGroupName") String resourceGroupName,
        @Path("loadTestName") String loadTestName, @Body LoadTestResource testResource,
        @Tag RequestOrigin tag);

  }

  private interface LoadTestApi {

    String API_VERSION = "?api-version=2022-11-01";
    String MERGE_PATCH_CONTENT_TYPE_HEADER = "content-type: application/merge-patch+json";

    @GET("tests" + API_VERSION)
    Call<ResponseList<LoadTest>> findTestByName(@Query("search") String search);

    @PATCH("tests/{testId}" + API_VERSION)
    @Headers({MERGE_PATCH_CONTENT_TYPE_HEADER})
    Call<Void> updateTest(@Path("testId") String testId, @Body LoadTest loadTest);

    @PATCH("tests/{testId}/app-components" + API_VERSION)
    @Headers({MERGE_PATCH_CONTENT_TYPE_HEADER})
    Call<Void> updateAppComponents(@Path("testId") String testId,
        @Body AppComponents appComponents);

    @GET("tests/{testId}/app-components" + API_VERSION)
    Call<AppComponents> findTestAppComponents(@Path("testId") String testId);

    @GET("tests/{testId}/files" + API_VERSION)
    Call<ResponseList<FileInfo>> findTestFiles(@Path("testId") String testId);

    @DELETE("tests/{testId}/files/{fileName}" + API_VERSION)
    Call<Void> deleteTestFile(@Path("testId") String testId, @Path("fileName") String fileName);

    @PUT("tests/{testId}/files/{fileName}" + API_VERSION)
    Call<FileInfo> uploadTestFile(@Path("testId") String testId, @Path("fileName") String fileName,
        @Body RequestBody testFile);

    @GET("tests/{testId}/files/{fileName}" + API_VERSION)
    Call<FileInfo> findTestFile(@Path("testId") String testId, @Path("fileName") String fileName);

    @PATCH("test-runs/{testRunId}" + API_VERSION)
    @Headers(MERGE_PATCH_CONTENT_TYPE_HEADER)
    Call<TestRun> createTestRun(@Path("testRunId") String testRunId,
        @Query("tenantId") String tenantId, @Body TestRun testRun);

    @GET("test-runs/{testRunId}" + API_VERSION)
    Call<TestRun> findTestRunById(@Path("testRunId") String id);

    @POST("test-runs/{testRunId}:stop" + API_VERSION)
    Call<Void> stopTestRun(@Path("testRunId") String id);

  }

  public Subscription findSubscription() throws IOException {
    return execApiCall(managementApi.findSubscriptions(RequestOrigin.MANAGEMENT))
        .getFirstElement().orElse(null);
  }

  public ResourceGroup findResourceGroup(String name, Subscription subscription)
      throws IOException {
    return execOptionalApiCall(
        managementApi.findResourceGroup(subscription.getId(), name, RequestOrigin.MANAGEMENT))
        .map(g -> {
          g.setName(name);
          g.setSubscription(subscription);
          return g;
        })
        .orElse(null);
  }

  private <T> Optional<T> execOptionalApiCall(Call<T> call) throws IOException {
    retrofit2.Response<T> response = call.execute();
    if (!response.isSuccessful()) {
      if (response.code() == 404) {
        return Optional.empty();
      }
      try (ResponseBody errorBody = response.errorBody()) {
        throw buildRemoteEngineException(response.code(), errorBody.string());
      }
    }
    return Optional.ofNullable(response.body());
  }

  public Location findLocation(Subscription subscription) throws IOException {
    return execApiCall(
        managementApi.findLocations(subscription.getId(), RequestOrigin.MANAGEMENT))
        .getFirstElement().orElse(null);
  }

  public void createResourceGroup(ResourceGroup resourceGroup) throws IOException {
    ResourceGroup created = execApiCall(
        managementApi.createResourceGroup(resourceGroup.getSubscription().getId(),
            resourceGroup.getName(), resourceGroup, RequestOrigin.MANAGEMENT));
    resourceGroup.setProvisioningState(created.getProvisioningState());
  }

  public LoadTestResource findTestResource(String name, ResourceGroup resourceGroup)
      throws IOException {
    return execOptionalApiCall(
        managementApi.findTestResource(resourceGroup.getSubscription().getId(),
            resourceGroup.getName(), name, RequestOrigin.MANAGEMENT))
        .map(r -> {
          r.setResourceGroup(resourceGroup);
          setLoadTestResource(r);
          return r;
        })
        .orElse(null);
  }

  private void setLoadTestResource(LoadTestResource r) {
    dataPlaneUrl = String.format("https://%s", r.getDataPlaneUri());
    loadTestApi = buildApiFor(dataPlaneUrl + "/", LoadTestApi.class);
  }

  public String getDataPlaneUrl() {
    return dataPlaneUrl;
  }

  public void createTestResource(LoadTestResource testResource) throws IOException {
    LoadTestResource created = execApiCall(managementApi.createTestResource(
        testResource.getResourceGroup().getSubscription().getId(),
        testResource.getResourceGroup().getName(), testResource.getName(), testResource,
        RequestOrigin.MANAGEMENT));
    testResource.setId(created.getId());
    testResource.setProvisioningState(created.getProvisioningState());
    setLoadTestResource(created);
  }

  public LoadTest findTestByName(String testName, LoadTestResource testResource)
      throws IOException {
    return execApiCall(loadTestApi.findTestByName(testName)).stream()
        /*
         this is necessary because API returns all tests elements with name starting with the given
         string
         */
        .filter(t -> testName.equals(t.getDisplayName()))
        .peek(t -> t.setTestResource(testResource))
        .findAny()
        .orElse(null);
  }

  public void updateTest(LoadTest loadTest) throws IOException {
    execApiCall(loadTestApi.updateTest(loadTest.getTestId(), loadTest));
  }

  public void updateAppComponents(String testId, AppComponents appComponents)
      throws IOException {
    execApiCall(loadTestApi.updateAppComponents(testId, appComponents));
  }

  public AppComponents findTestAppComponents(String testId) throws IOException {
    return execOptionalApiCall(loadTestApi.findTestAppComponents(testId))
        .orElse(new AppComponents(Collections.emptyList()));
  }

  public List<String> findTestFiles(String testId) throws IOException {
    return execApiCall(loadTestApi.findTestFiles(testId)).stream()
        .map(FileInfo::getFileName)
        .collect(Collectors.toList());
  }

  public void deleteTestFile(String fileName, String testId) throws IOException {
    execApiCall(loadTestApi.deleteTestFile(testId, fileName));
  }

  public FileInfo uploadTestFile(File file, String fileName, String testId) throws IOException {
    return execApiCall(loadTestApi.uploadTestFile(testId, fileName,
        RequestBody.create(MediaType.get("application/octet-stream"), file)));
  }

  public FileInfo findTestFile(String fileName, String testId) throws IOException {
    return execApiCall(loadTestApi.findTestFile(testId, fileName));
  }

  public TestRun createTestRun(TestRun testRun) throws IOException {
    return execApiCall(loadTestApi.createTestRun(testRun.getId(), tenantId, testRun));
  }

  public TestRun findTestRunById(String id) throws IOException {
    return execApiCall(loadTestApi.findTestRunById(id));
  }

  public void stopTestRun(String id) throws IOException {
    execApiCall(loadTestApi.stopTestRun(id));
  }

}
