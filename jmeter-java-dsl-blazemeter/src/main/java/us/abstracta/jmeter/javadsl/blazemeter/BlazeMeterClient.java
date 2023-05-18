package us.abstracta.jmeter.javadsl.blazemeter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import us.abstracta.jmeter.javadsl.blazemeter.api.ApiResponse;
import us.abstracta.jmeter.javadsl.blazemeter.api.Location;
import us.abstracta.jmeter.javadsl.blazemeter.api.Project;
import us.abstracta.jmeter.javadsl.blazemeter.api.Test;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestConfig;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestFile;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestFileDeleteRequest;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRun;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunConfig;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunRequestStats;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunStatus;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunSummaryStats;
import us.abstracta.jmeter.javadsl.blazemeter.api.User;
import us.abstracta.jmeter.javadsl.blazemeter.api.Workspace;
import us.abstracta.jmeter.javadsl.engines.BaseRemoteEngineApiClient;
import us.abstracta.jmeter.javadsl.engines.RemoteEngineException;

public class BlazeMeterClient extends BaseRemoteEngineApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(BlazeMeterClient.class);
  private static final String BASE_URL = "https://a.blazemeter.com";
  public static final String BASE_APP_URL = BASE_URL + "/app/#";
  private static final int WARNING_EVENT_LEVEL = 300;

  private final BlazeMeterApi api;
  private final String username;
  private final String password;
  private List<Location> privateLocationsCache;

  public BlazeMeterClient(String username, String password) {
    super(LOG);
    this.username = username;
    this.password = password;
    api = buildApiFor(BASE_URL + "/api/v4/", BlazeMeterApi.class);
  }

  @Override
  protected String buildAuthorizationHeaderValue(Request request) {
    return Credentials.basic(username, password);
  }

  @Override
  protected RemoteEngineException buildRemoteEngineException(int code, String message) {
    return new BlazeMeterException(code, message);
  }

  private interface BlazeMeterApi {

    @GET("user")
    Call<ApiResponse<User>> findUser();

    @GET("projects/{projectId}")
    Call<ApiResponse<Project>> findProject(@Path("projectId") long projectId);

    @GET("workspaces/{workspaceId}")
    Call<ApiResponse<Workspace>> findWorkspace(@Path("workspaceId") long workspaceId);

    @GET("private-locations")
    Call<ApiResponse<List<Location>>> findPrivateLocations(@Query("accountId") long accountId,
        @Query("workspaceId") long workspaceId);

    @GET("tests")
    Call<ApiResponse<List<Test>>> findTests(@Query("projectId") long projectId,
        @Query("name") String name);

    @PATCH("tests/{testId}")
    Call<ApiResponse<Void>> updateTest(@Path("testId") long testId, @Body TestConfig test);

    @POST("tests")
    Call<ApiResponse<Test>> createTest(@Body TestConfig test);

    @GET("tests/{testId}/files")
    Call<ApiResponse<List<TestFile>>> findTestFiles(@Path("testId") long testId);

    @POST("tests/{testId}/delete-file")
    Call<ApiResponse<Void>> deleteTestFile(@Path("testId") long testId,
        @Body TestFileDeleteRequest req);

    @POST("tests/{testId}/files")
    @Multipart
    Call<ApiResponse<Void>> uploadTestFile(@Path("testId") long testId,
        @Part MultipartBody.Part testFile);

    @POST("tests/{testId}/start")
    Call<ApiResponse<TestRun>> startTest(@Path("testId") long testId,
        @Body TestRunConfig testRunConfig);

    @GET("masters/{testRunId}/status")
    Call<ApiResponse<TestRunStatus>> findTestRunStatus(@Path("testRunId") long testRunId,
        @Query("level") int level);

    @GET("masters/{testRunId}?withMessages=true")
    Call<ApiResponse<TestRun>> findTestRunById(@Path("testRunId") long testRunId);

    @GET("masters/{testRunId}/reports/default/summary")
    Call<ApiResponse<TestRunSummaryStats>> findTestRunSummaryStats(
        @Path("testRunId") long testRunId);

    @GET("masters/{testRunId}/reports/aggregatereport/data")
    Call<ApiResponse<List<TestRunRequestStats>>> findTestRunRequestStats(
        @Path("testRunId") long testRunId);

    @POST("masters/{testRunId}/stop")
    Call<ApiResponse<Void>> stopTestRun(@Path("testRunId") long id);

  }

  public Project findDefaultProject() throws IOException {
    Project ret = execBmApiCall(api.findUser()).getDefaultProject();
    ret.setBaseUrl(BASE_APP_URL);
    return ret;
  }

  public Project findProjectById(Long projectId) throws IOException {
    Project ret = execBmApiCall(api.findProject(projectId));
    ret.setAccountId(execBmApiCall(api.findWorkspace(ret.getWorkspaceId())).getAccountId());
    ret.setBaseUrl(BASE_APP_URL);
    return ret;
  }

  private <T> T execBmApiCall(Call<ApiResponse<T>> call) throws IOException {
    return execApiCall(call).getResult();
  }

  public Location findPrivateLocationByName(String name, Project project) throws IOException {
    if (privateLocationsCache == null) {
      privateLocationsCache = execBmApiCall(
          api.findPrivateLocations(project.getAccountId(), project.getWorkspaceId()));
    }
    return privateLocationsCache.stream()
        .filter(l -> l.getName().equals(name))
        .findAny()
        .orElse(null);
  }

  public Optional<Test> findTestByName(String testName, Project project) throws IOException {
    List<Test> tests = execBmApiCall(api.findTests(project.getId(), testName));
    if (tests.isEmpty()) {
      return Optional.empty();
    }
    Test test = tests.get(0);
    test.setProject(project);
    return Optional.of(test);
  }

  public List<String> findTestFiles(Test test) throws IOException {
    return execBmApiCall(api.findTestFiles(test.getId())).stream()
        .map(TestFile::getName)
        .collect(Collectors.toList());
  }

  public void deleteTestFile(String name, Test test) throws IOException {
    execApiCall(api.deleteTestFile(test.getId(), new TestFileDeleteRequest(name)));
  }

  public Test createTest(TestConfig testConfig, Project project)
      throws IOException {
    Test ret = execBmApiCall(api.createTest(testConfig));
    ret.setProject(project);
    return ret;
  }

  public void updateTest(Test test, TestConfig testConfig) throws IOException {
    execApiCall(api.updateTest(test.getId(), testConfig));
  }

  public void uploadTestFile(File file, String fileName, Test test) throws IOException {
    RequestBody requestBody = RequestBody
        .create(MediaType.get("application/octet-stream"), file);
    MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileName, requestBody);
    execApiCall(api.uploadTestFile(test.getId(), part));
  }

  public TestRun startTest(Test test, TestRunConfig runConfig) throws IOException {
    TestRun ret = execBmApiCall(api.startTest(test.getId(), runConfig));
    ret.setTest(test);
    return ret;
  }

  public TestRunStatus findTestRunStatus(TestRun testRun)
      throws IOException {
    return execBmApiCall(api.findTestRunStatus(testRun.getId(), WARNING_EVENT_LEVEL));
  }

  public TestRun findTestRunById(long testRunId) throws IOException {
    return execBmApiCall(api.findTestRunById(testRunId));
  }

  public TestRunSummaryStats findTestRunSummaryStats(TestRun testRun) throws IOException {
    return execBmApiCall(api.findTestRunSummaryStats(testRun.getId()));
  }

  public List<TestRunRequestStats> findTestRunRequestStats(TestRun testRun) throws IOException {
    return execBmApiCall(api.findTestRunRequestStats(testRun.getId()));
  }

  public void stopTestRun(TestRun testRun) throws IOException {
    execApiCall(api.stopTestRun(testRun.getId()));
  }

}
