package us.abstracta.jmeter.javadsl.octoperf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import us.abstracta.jmeter.javadsl.engines.BaseRemoteEngineApiClient;
import us.abstracta.jmeter.javadsl.engines.RemoteEngineException;
import us.abstracta.jmeter.javadsl.octoperf.api.BenchReport;
import us.abstracta.jmeter.javadsl.octoperf.api.BenchReport.StatisticTableReportItem;
import us.abstracta.jmeter.javadsl.octoperf.api.BenchReport.SummaryReportItem;
import us.abstracta.jmeter.javadsl.octoperf.api.BenchResult;
import us.abstracta.jmeter.javadsl.octoperf.api.Project;
import us.abstracta.jmeter.javadsl.octoperf.api.Provider;
import us.abstracta.jmeter.javadsl.octoperf.api.Scenario;
import us.abstracta.jmeter.javadsl.octoperf.api.TableEntry;
import us.abstracta.jmeter.javadsl.octoperf.api.User;
import us.abstracta.jmeter.javadsl.octoperf.api.VirtualUser;
import us.abstracta.jmeter.javadsl.octoperf.api.Workspace;

public class OctoPerfClient extends BaseRemoteEngineApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(OctoPerfClient.class);
  private static final String BASE_URL = "https://api.octoperf.com";
  private static final String BASE_APP_URL = BASE_URL + "/app/#/app";

  private final OctoPerfApi api;
  private final String apiKey;

  public OctoPerfClient(String apiKey) {
    super(LOG);
    this.apiKey = apiKey;
    api = buildApiFor(BASE_URL, OctoPerfApi.class);
  }

  @Override
  protected String buildAuthorizationHeaderValue(Request request) {
    return "Bearer " + apiKey;
  }

  @Override
  protected ObjectMapper buildConverterMapper() {
    return super.buildConverterMapper()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @Override
  protected RemoteEngineException buildRemoteEngineException(int code, String message) {
    return new OctoPerfException(code, message);
  }

  private interface OctoPerfApi {

    @GET("users/current")
    Call<User> findCurrentUser();

    @GET("workspaces/member-of")
    Call<List<Workspace>> findWorkspaces();

    @GET("design/projects/by-workspace/{workspaceId}/DESIGN")
    Call<List<Project>> findProjectsByWorkspace(@Path("workspaceId") String workspaceId);

    @POST("design/projects")
    Call<Project> createProject(@Body Project project);

    @POST("design/imports/jmx/{projectId}")
    @Multipart
    Call<List<VirtualUser>> importJmx(@Path("projectId") String projectId,
        @Part MultipartBody.Part file);

    @GET("design/virtual-users/by-project/{projectId}")
    Call<List<VirtualUser>> findVirtualUsersByProject(@Path("projectId") String projectId);

    @DELETE("design/virtual-users/{virtualUserId}")
    Call<Void> deleteVirtualUser(@Path("virtualUserId") String virtualUserId);

    @PUT("design/virtual-users/{virtualUserId}")
    Call<Void> updateVirtualUser(@Path("virtualUserId") String virtualUserId, @Body VirtualUser vu);

    @GET("workspaces/docker-providers/public")
    Call<List<Provider>> findProvidersByWorkspace(@Query("workspaceId") String workspaceId);

    @POST("runtime/scenarios")
    Call<Scenario> createScenario(@Body Scenario scenario);

    @GET("runtime/scenarios/by-project/{projectId}")
    Call<List<Scenario>> findScenariosByProject(@Path("projectId") String projectId);

    @POST("runtime/scenarios/run/{scenarioId}")
    Call<BenchReport> runScenario(@Path("scenarioId") String scenarioId);

    @DELETE("runtime/scenarios/{scenarioId}")
    Call<Void> deleteScenario(@Path("scenarioId") String scenarioId);

    @GET("runtime/bench-results/{benchResultId}")
    Call<BenchResult> findBenchResult(@Path("benchResultId") String benchResultId);

    @POST("analysis/metrics/summary")
    Call<double[]> findSummaryStats(@Body SummaryReportItem report);

    @POST("analysis/metrics/table")
    Call<List<TableEntry>> findTableStats(@Body StatisticTableReportItem report);

  }

  public User findCurrentUser() throws IOException {
    return execApiCall(api.findCurrentUser());
  }

  public Workspace findDefaultWorkspace() throws IOException {
    Workspace ret = execApiCall(api.findWorkspaces()).get(0);
    ret.setBaseAppUrl(BASE_APP_URL);
    return ret;
  }

  public Optional<Project> findProjectByWorkspaceAndName(Workspace workspace, String name)
      throws IOException {
    Optional<Project> ret = execApiCall(api.findProjectsByWorkspace(workspace.getId()))
        .stream()
        .filter(p -> name.equalsIgnoreCase(p.getName()))
        .findAny();
    ret.ifPresent(p -> p.setWorkspace(workspace));
    return ret;
  }

  public Project createProject(Project project) throws IOException {
    Project ret = execApiCall(api.createProject(project));
    ret.setWorkspace(project.getWorkspace());
    return ret;
  }

  public List<VirtualUser> findVirtualUsersByProject(Project project) throws IOException {
    List<VirtualUser> ret = execApiCall(api.findVirtualUsersByProject(project.getId()));
    ret.forEach(v -> v.setProject(project));
    return ret;
  }

  public void deleteVirtualUser(VirtualUser virtualUser) throws IOException {
    execApiCall(api.deleteVirtualUser(virtualUser.getId()));
  }

  public List<Scenario> findScenariosByProject(Project project) throws IOException {
    List<Scenario> ret = execApiCall(api.findScenariosByProject(project.getId()));
    ret.forEach(s -> s.setProject(project));
    return ret;
  }

  public void deleteScenario(Scenario scenario) throws IOException {
    execApiCall(api.deleteScenario(scenario.getId()));
  }

  public List<VirtualUser> importJmx(Project project, File jmxFile) throws IOException {
    RequestBody requestBody = RequestBody
        .create(MediaType.get("application/octet-stream"), jmxFile);
    MultipartBody.Part part = MultipartBody.Part
        .createFormData("file", jmxFile.getName(), requestBody);
    List<VirtualUser> ret = execApiCall(api.importJmx(project.getId(), part));
    ret.forEach(vu -> vu.setProject(project));
    return ret;
  }

  public void updateVirtualUser(VirtualUser vu) throws IOException {
    execApiCall(api.updateVirtualUser(vu.getId(), vu));
  }

  public Provider findProviderByWorkspace(Workspace workspace) throws IOException {
    return execApiCall(api.findProvidersByWorkspace(workspace.getId())).get(0);
  }

  public Scenario createScenario(Scenario scenario) throws IOException {
    Scenario ret = execApiCall(api.createScenario(scenario));
    ret.setProject(scenario.getProject());
    return ret;
  }

  public BenchReport runScenario(Scenario scenario) throws IOException {
    BenchReport ret = execApiCall(api.runScenario(scenario.getId()));
    ret.setProject(scenario.getProject());
    return ret;
  }

  public BenchResult findBenchResult(String resultId) throws IOException {
    return execApiCall(api.findBenchResult(resultId));
  }

  public double[] findSummaryStats(SummaryReportItem summaryReport) throws IOException {
    return execApiCall(api.findSummaryStats(summaryReport));
  }

  public List<TableEntry> findTableStats(StatisticTableReportItem tableReport) throws IOException {
    return execApiCall(api.findTableStats(tableReport));
  }

}
