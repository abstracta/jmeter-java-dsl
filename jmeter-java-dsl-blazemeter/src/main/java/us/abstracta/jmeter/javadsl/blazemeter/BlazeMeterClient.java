package us.abstracta.jmeter.javadsl.blazemeter;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import devcsrj.okhttp3.logging.HttpLoggingInterceptor;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import us.abstracta.jmeter.javadsl.blazemeter.api.ApiResponse;
import us.abstracta.jmeter.javadsl.blazemeter.api.Project;
import us.abstracta.jmeter.javadsl.blazemeter.api.Test;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestConfig;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRun;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunRequestStats;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunStatus;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunSummaryStats;
import us.abstracta.jmeter.javadsl.blazemeter.api.User;
import us.abstracta.jmeter.javadsl.blazemeter.api.Workspace;

public class BlazeMeterClient {

  private static final Logger LOG = LoggerFactory.getLogger(BlazeMeterClient.class);
  private static final int WARNING_EVENT_LEVEL = 300;

  private final BlazeMeterApi api;

  public BlazeMeterClient(String apiUrl, String authToken) {
    api = new Retrofit.Builder()
        .baseUrl(apiUrl)
        .addConverterFactory(buildConverterFactory())
        .client(buildHttpClient(authToken))
        .build()
        .create(BlazeMeterApi.class);
  }

  private JacksonConverterFactory buildConverterFactory() {
    return JacksonConverterFactory.create(new ObjectMapper()
        .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JavaTimeModule()));
  }

  private OkHttpClient buildHttpClient(String token) {
    int tokenSeparatorIndex = token.indexOf(":");
    if (tokenSeparatorIndex < 0) {
      throw new IllegalArgumentException(
          "BlazeMeter token does not match with expected format: <apikey>:<secretKey>");
    }
    OkHttpClient.Builder builder = new OkHttpClient.Builder()
        .addInterceptor(chain -> {
          Request request = chain.request()
              .newBuilder()
              .header("Authorization", Credentials.basic(token.substring(0, tokenSeparatorIndex),
                  token.substring(tokenSeparatorIndex + 1)))
              .build();
          return chain.proceed(request);
        });
    if (LOG.isDebugEnabled()) {
      builder.addInterceptor(new HttpLoggingInterceptor());
    }
    return builder.build();
  }

  private interface BlazeMeterApi {

    @GET("user")
    Call<ApiResponse<User>> findUser();

    @GET("projects/{projectId}")
    Call<ApiResponse<Project>> findProject(@Path("projectId") long projectId);

    @GET("workspaces/{workspaceId}")
    Call<ApiResponse<Workspace>> findWorkspace(@Path("workspaceId") long workspaceId);

    @GET("tests")
    Call<ApiResponse<List<Test>>> findTests(@Query("projectId") long projectId,
        @Query("name") String name);

    @PATCH("tests/{testId}")
    Call<ApiResponse<Void>> updateTest(@Path("testId") long testId, @Body TestConfig test);

    @POST("tests")
    Call<ApiResponse<Test>> createTest(@Body TestConfig test);

    @POST("tests/{testId}/files")
    @Multipart
    Call<ApiResponse<Void>> uploadTestFile(@Path("testId") long testId,
        @Part MultipartBody.Part testFile);

    @POST("tests/{testId}/start")
    Call<ApiResponse<TestRun>> startTest(@Path("testId") long testId);

    @GET("masters/{testRunId}/status")
    Call<ApiResponse<TestRunStatus>> findTestRunStatus(@Path("testRunId") long testRunId,
        @Query("level") int level);

    @GET("masters/{testRunId}/reports/default/summary")
    Call<ApiResponse<TestRunSummaryStats>> findTestRunSummaryStats(
        @Path("testRunId") long testRunId);

    @GET("masters/{testRunId}/reports/aggregatereport/data")
    Call<ApiResponse<List<TestRunRequestStats>>> findTestRunRequestStats(
        @Path("testRunId") long testRunId);

  }

  public Project findDefaultProject(String baseUrl) throws IOException {
    Project ret = execApiCall(api.findUser()).getDefaultProject();
    ret.setBaseUrl(baseUrl);
    return ret;
  }

  public Project findProjectById(Long projectId, String baseUrl) throws IOException {
    Project ret = execApiCall(api.findProject(projectId));
    ret.setAccountId(execApiCall(api.findWorkspace(ret.getWorkspaceId())).getAccountId());
    ret.setBaseUrl(baseUrl);
    return ret;
  }

  private <T> T execApiCall(Call<ApiResponse<T>> call) throws IOException {
    Response<ApiResponse<T>> response = call.execute();
    if (!response.isSuccessful()) {
      throw new BlazeMeterException(response.code(), response.errorBody().string());
    }
    return response.body().getResult();
  }

  public Optional<Test> findTestByName(String testName, Project project) throws IOException {
    List<Test> tests = execApiCall(api.findTests(project.getId(), testName));
    if (tests.isEmpty()) {
      return Optional.empty();
    }
    Test test = tests.get(0);
    test.setProject(project);
    return Optional.of(test);
  }

  public Test createTest(TestConfig testConfig, Project project)
      throws IOException {
    Test ret = execApiCall(api.createTest(testConfig));
    ret.setProject(project);
    return ret;
  }

  public void updateTest(Test test, TestConfig testConfig) throws IOException {
    execApiCall(api.updateTest(test.getId(), testConfig));
  }

  public void uploadTestFile(Test test, File jmxFile) throws IOException {
    RequestBody requestBody = RequestBody
        .create(MediaType.get("application/octet-stream"), jmxFile);
    MultipartBody.Part part = MultipartBody.Part
        .createFormData("file", jmxFile.getName(), requestBody);
    execApiCall(api.uploadTestFile(test.getId(), part));
  }

  public TestRun startTest(Test test) throws IOException {
    TestRun ret = execApiCall(api.startTest(test.getId()));
    ret.setTest(test);
    return ret;
  }

  public TestRunStatus findTestRunStatus(TestRun testRun)
      throws IOException {
    return execApiCall(api.findTestRunStatus(testRun.getId(), WARNING_EVENT_LEVEL));
  }

  public TestRunSummaryStats findTestRunSummaryStats(TestRun testRun) throws IOException {
    return execApiCall(api.findTestRunSummaryStats(testRun.getId()));
  }

  public List<TestRunRequestStats> findTestRunRequestStats(TestRun testRun) throws IOException {
    return execApiCall(api.findTestRunRequestStats(testRun.getId()));
  }

}
