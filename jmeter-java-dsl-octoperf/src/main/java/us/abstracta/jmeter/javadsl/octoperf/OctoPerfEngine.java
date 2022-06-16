package us.abstracta.jmeter.javadsl.octoperf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.DslJmeterEngine;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.engines.JmeterEnvironment;
import us.abstracta.jmeter.javadsl.octoperf.api.BenchReport;
import us.abstracta.jmeter.javadsl.octoperf.api.BenchReport.BenchReportItem;
import us.abstracta.jmeter.javadsl.octoperf.api.BenchReport.ReportItemMetric;
import us.abstracta.jmeter.javadsl.octoperf.api.BenchReport.ReportMetricId;
import us.abstracta.jmeter.javadsl.octoperf.api.BenchReport.StatisticTableReportItem;
import us.abstracta.jmeter.javadsl.octoperf.api.BenchReport.SummaryReportItem;
import us.abstracta.jmeter.javadsl.octoperf.api.BenchResult;
import us.abstracta.jmeter.javadsl.octoperf.api.BenchResult.State;
import us.abstracta.jmeter.javadsl.octoperf.api.Project;
import us.abstracta.jmeter.javadsl.octoperf.api.Provider;
import us.abstracta.jmeter.javadsl.octoperf.api.Scenario;
import us.abstracta.jmeter.javadsl.octoperf.api.TableEntry;
import us.abstracta.jmeter.javadsl.octoperf.api.User;
import us.abstracta.jmeter.javadsl.octoperf.api.UserLoad;
import us.abstracta.jmeter.javadsl.octoperf.api.UserLoad.UserLoadRampUp;
import us.abstracta.jmeter.javadsl.octoperf.api.VirtualUser;
import us.abstracta.jmeter.javadsl.octoperf.api.Workspace;

/**
 * A {@link DslJmeterEngine} which allows running DslTestPlan in OctoPerf.
 *
 * @since 0.58
 */
public class OctoPerfEngine implements DslJmeterEngine {

  private static final Logger LOG = LoggerFactory.getLogger(OctoPerfEngine.class);
  private static final String TAG = "jmeter-java-dsl";
  private static final Set<String> TAGS = Collections.singleton(TAG);
  private static final Duration STATUS_POLL_PERIOD = Duration.ofSeconds(5);
  private static final Duration STATISTICS_POLL_PERIOD = Duration.ofSeconds(30);

  private final String apiKey;
  private String projectName = "jmeter-java-dsl";
  private int totalUsers = 1;
  private Duration rampUp = Duration.ZERO;
  private Duration holdFor = Duration.ofSeconds(10);
  private Duration testTimeout = Duration.ofHours(1);
  private boolean projectCleanUp = true;

  /**
   * @param apiKey is the authentication token to be used to access OctoPerf API.
   *               <p>
   *               Check <a href="https://doc.octoperf.com/account/profile/#apikey">OctoPerf API
   *               keys</a> for instructions on how to get it.
   */
  public OctoPerfEngine(String apiKey) {
    this.apiKey = apiKey;
  }

  /**
   * Sets the name of the OctoPerf project to use.
   * <p>
   * OctoPerfEngine will search for a project with the given name in the default workspace and if
   * one exists, it will use it. Otherwise, a new project will be created with given name.
   * <p>
   * To keep project clean, and avoid piling up virtual users and scenarios, OctoPerfEngine will
   * remove any existing virtual users or scenarios created by jmeter-java-dsl (checking for
   * jmeter-java-dsl tag). If you want to disable this logic check
   * {@link #projectCleanUp(boolean)}.
   * <p>
   * <b>It is important that you use a unique project name for each project</b>, to avoid one
   * project interfering with other projects entities.
   * <p>
   * When not specified, the project name defaults to "jmeter-java-dsl".
   *
   * @param projectName specifies the name of the project to use in OctoPerf.
   * @return the modified instance for fluent API usage.
   */
  public OctoPerfEngine projectName(String projectName) {
    this.projectName = projectName;
    return this;
  }

  /**
   * Specifies the number of virtual users to use when running the test.
   * <p>
   * This value overwrites any value specified in JMeter test plans thread groups.
   * <p>
   * When not specified, then 1 will be used.
   *
   * @param totalUsers number of virtual users to run the test with.
   * @return the modified instance for fluent API usage.
   */
  public OctoPerfEngine totalUsers(int totalUsers) {
    this.totalUsers = totalUsers;
    return this;
  }

  /**
   * Sets the duration of time taken to start the specified total users.
   * <p>
   * For example if totalUsers is set to 10, rampUp is 1 minute and holdFor is 10 minutes, it means
   * that it will take 1 minute to start the 10 users (starting them in a linear fashion: 1 user
   * every 6 seconds), and then continue executing the test with the 10 users for 10 additional
   * minutes.
   * <p>
   * This value overwrites any value specified in JMeter test plans thread groups.
   * <p>
   * When not specified, 0 will be used.
   *
   * @param rampUp duration that OctoPerf will take to spin up all the virtual users.
   * @return the modified instance for fluent API usage.
   */
  public OctoPerfEngine rampUpFor(Duration rampUp) {
    this.rampUp = rampUp;
    return this;
  }

  /**
   * Specifies the duration of time to keep the virtual users running, after the rampUp period.
   * <p>
   * When specified, this value overwrites any value specified in JMeter test plans thread groups.
   * When not specified it will use 10 seconds by default.
   *
   * @param holdFor duration to keep virtual users running after the rampUp period.
   * @return the modified instance for fluent API usage.
   */
  public OctoPerfEngine holdFor(Duration holdFor) {
    this.holdFor = holdFor;
    return this;
  }

  /**
   * Specifies a timeout for the entire test execution.
   * <p>
   * If the timeout is reached then the test run will throw a TimeoutException.
   * <p>
   * It is strongly advised to set this timeout properly in each run, according to the expected test
   * execution time plus some additional margin (to consider for additional delays in OctoPerf test
   * setup and teardown).
   * <p>
   * This timeout exists to avoid any potential problem with OctoPerf execution not detected by the
   * client, and avoid keeping the test indefinitely running until is interrupted by a user. This is
   * specially annoying when running tests in automated fashion, for example in CI/CD.
   * <p>
   * When not specified, the default timeout will is set to 1 hour.
   *
   * @param testTimeout to be used as time limit for test execution. If execution takes more than
   *                    this, then a TimeoutException will be thrown by the engine.
   * @return the modified instance for fluent API usage.
   */
  public OctoPerfEngine testTimeout(Duration testTimeout) {
    this.testTimeout = testTimeout;
    return this;
  }

  /**
   * Allows enabling or disabling de automatic deletion of virtual users and scenarios from
   * project.
   * <p>
   * To avoid piling up virtual users and scenarios in the project, the OctoPerfEngine deletes,
   * before creating new ones, all virtual users and scenarios previously created by jmeter-java-dsl
   * (the ones that contain jmeter-java-dsl tag). This method allows to disable such logic.
   *
   * @param enabled specifies to enable the automatic clean up or disable it when false.
   * @return the modified instance for fluent API usage.
   */
  public OctoPerfEngine projectCleanUp(boolean enabled) {
    this.projectCleanUp = enabled;
    return this;
  }

  @Override
  public TestPlanStats run(DslTestPlan testPlan)
      throws IOException, InterruptedException, TimeoutException {
    File jmxFile = Files.createTempFile("jmeter-dsl", "test.jmx").toFile();
    try (OctoPerfClient client = new OctoPerfClient(apiKey)) {
      User user = client.findCurrentUser();
      Project project = findProject(user, client);
      if (projectCleanUp) {
        cleanUpProject(project, client);
      }
      saveTestPlanTo(testPlan, jmxFile);
      LOG.info("Importing JMX file into project...");
      List<VirtualUser> vus = client.importJmx(project, jmxFile);
      vus.forEach(vu -> LOG.info("Created virtual user {}", vu.getUrl()));
      tagVirtualUsers(vus, client);
      Scenario scenario = buildScenario(user, project, vus, client);
      BenchReport report = client.runScenario(scenario);
      LOG.info("Running scenario in {}", report.getUrl());
      Instant testStart = Instant.now();
      BenchResult result = awaitTestEnd(report, testStart, client);
      return findTestPlanStats(report, testStart, vus, result, client);
    } finally {
      jmxFile.delete();
    }
  }

  private Project findProject(User user, OctoPerfClient client) throws IOException {
    LOG.info("Looking up project with name '{}'...", projectName);
    Workspace workspace = client.findDefaultWorkspace();
    Optional<Project> foundProject = client.findProjectByWorkspaceAndName(workspace, projectName);
    if (foundProject.isPresent()) {
      LOG.info("Found project {}", foundProject.get().getUrl());
      return foundProject.get();
    } else {
      Project ret = client.createProject(new Project(user, workspace, projectName, TAGS));
      LOG.info("Created project {}", ret.getUrl());
      return ret;
    }
  }

  private void cleanUpProject(Project project, OctoPerfClient client) throws IOException {
    LOG.info("Deleting previously generated virtual users and scenarios from project to avoid "
        + "piling them up...");
    List<VirtualUser> vus = client.findVirtualUsersByProject(project);
    for (VirtualUser vu : vus) {
      if (vu.getTags().contains(TAG)) {
        try {
          client.deleteVirtualUser(vu);
        } catch (IOException e) {
          LOG.warn("Problem deleting virtual user {}" + vu.getUrl(), e);
        }
      }
    }
    List<Scenario> scenarios = client.findScenariosByProject(project);
    for (Scenario scenario : scenarios) {
      if (scenario.getTags().contains(TAG)) {
        try {
          client.deleteScenario(scenario);
        } catch (IOException e) {
          LOG.warn("Problem deleting scenario {}" + scenario.getUrl(), e);
        }
      }
    }
  }

  private void saveTestPlanTo(DslTestPlan testPlan, File jmxFile) throws IOException {
    JmeterEnvironment env = new JmeterEnvironment();
    try (FileOutputStream output = new FileOutputStream(jmxFile.getPath())) {
      HashTree tree = new ListedHashTree();
      BuildTreeContext context = new BuildTreeContext();
      context.buildTreeFor(testPlan, tree);
      env.saveTree(tree, output);
      context.getVisualizers().forEach((v, e) ->
          LOG.warn(
              "OctoPerfEngine does not currently support displaying visualizers. Ignoring {}.",
              v.getClass().getSimpleName())
      );
    }
  }

  private void tagVirtualUsers(List<VirtualUser> vus, OctoPerfClient client) throws IOException {
    for (VirtualUser vu : vus) {
      vu.getTags().add(TAG);
      client.updateVirtualUser(vu);
    }
  }

  private Scenario buildScenario(User user, Project project, List<VirtualUser> vus,
      OctoPerfClient client)
      throws IOException {
    Provider provider = client.findProviderByWorkspace(project.getWorkspace());
    String defaultRegion = provider.getRegions().keySet().iterator().next();
    List<UserLoad> userLoads = vus.stream()
        .map(vu -> new UserLoad(vu.getId(), provider.getId(), defaultRegion,
            new UserLoadRampUp(totalUsers, rampUp.toMillis(), holdFor.toMillis())))
        .collect(Collectors.toList());
    Scenario ret = client.createScenario(
        new Scenario(user, project, projectName, userLoads, TAGS));
    LOG.info("Created scenario {}", ret.getUrl());
    return ret;
  }

  private BenchResult awaitTestEnd(BenchReport report, Instant testStart, OctoPerfClient client)
      throws InterruptedException, IOException, TimeoutException {
    String resultId = report.getBenchResultIds().get(0);
    BenchResult result;
    State status = State.CREATED;
    do {
      Thread.sleep(STATUS_POLL_PERIOD.toMillis());
      result = client.findBenchResult(resultId);
      if (!status.equals(result.getState())) {
        LOG.debug("Test run {} status changed to: {}", report.getUrl(), result.getState());
        status = result.getState();
      }
    } while (!status.isFinalState() && !hasTimedOut(testStart, testTimeout));
    if (!status.isFinalState()) {
      throw buildTestTimeoutException(report);
    } else if (status == State.ERROR) {
      throw new IllegalStateException("Execution of test failed, please check OctoPerf logs for "
          + "more details: " + report.getUrl());
    } else if (status == State.ABORTED) {
      throw new IllegalStateException("Execution of the test was aborted, probably to some users "
          + "stopping it from OctoPerf  site. Check OctoPerf test execution for more details: "
          + report.getUrl());
    }
    return result;
  }

  private boolean hasTimedOut(Instant start, Duration timeout) {
    return Duration.between(start, Instant.now()).compareTo(timeout) >= 0;
  }

  private TimeoutException buildTestTimeoutException(BenchReport report) {
    return new TimeoutException(String.format(
        "Test %s didn't end after %s. "
            + "If the timeout is too short, you can change it with testTimeout() method.",
        report.getUrl(), testTimeout));
  }

  private TestPlanStats findTestPlanStats(BenchReport report, Instant testStart,
      List<VirtualUser> vus, BenchResult result, OctoPerfClient client)
      throws IOException, TimeoutException, InterruptedException {
    List<ReportMetricId> metrics = Arrays.asList(ReportMetricId.HITS_TOTAL,
        ReportMetricId.HITS_RATE, ReportMetricId.ERRORS_TOTAL, ReportMetricId.ERRORS_RATE,
        ReportMetricId.RESPONSE_TIME_AVG, ReportMetricId.RESPONSE_TIME_MIN,
        ReportMetricId.RESPONSE_TIME_MAX, ReportMetricId.RESPONSE_TIME_MEDIAN,
        ReportMetricId.RESPONSE_TIME_PERCENTILE_90, ReportMetricId.RESPONSE_TIME_PERCENTILE_95,
        ReportMetricId.RESPONSE_TIME_PERCENTILE_99, ReportMetricId.THROUGHPUT_TOTAL,
        ReportMetricId.THROUGHPUT_RATE, ReportMetricId.SENT_BYTES_TOTAL,
        ReportMetricId.SENT_BYTES_RATE);
    SummaryReportItem summaryReport = findReportItemWithType(SummaryReportItem.class,
        report.getItems());
    setReportMetrics(summaryReport, metrics);
    double[] summaryStats = client.findSummaryStats(summaryReport);
    double[] prevStats;
    /*
     since OctoPerf statistics are processed asynchronously, they may not be yet fully updated
     when tests result is marked as finished and since there is no indicator either when statistics
     are completely updated, we poll until we don't see further changes in statistics which is an
     indicator that statistics are highly probably complete. Explored alternatives included using
     jtl files and jmeter logs, but there is a limit of 1GB disk space in OctoPerf load generators
     and jtl and jmeter logs may as well be incomplete, with the additional performance penalty of
     handling such files.
     */
    do {
      Thread.sleep(STATISTICS_POLL_PERIOD.toMillis());
      prevStats = summaryStats;
      summaryStats = client.findSummaryStats(summaryReport);
    } while (!Arrays.equals(summaryStats, prevStats) && !hasTimedOut(testStart, testTimeout));
    if (hasTimedOut(testStart, testTimeout)) {
      throw buildTestTimeoutException(report);
    }
    StatisticTableReportItem tableReport = findReportItemWithType(StatisticTableReportItem.class,
        report.getItems());
    setReportMetrics(tableReport, metrics);
    List<TableEntry> tableStats = client.findTableStats(tableReport);
    return new OctoPerfTestPlanStats(summaryStats, tableStats, vus, result);
  }

  private static <T extends BenchReportItem> T findReportItemWithType(Class<T> reportItemClass,
      List<BenchReportItem> items) {
    return items.stream()
        .filter(reportItemClass::isInstance)
        .map(reportItemClass::cast)
        .findAny()
        .get();
  }

  private static void setReportMetrics(BenchReportItem summaryReport,
      List<ReportMetricId> metricIds) {
    List<ReportItemMetric> metrics = summaryReport.getMetrics();
    String benchResultId = metrics.get(0).getBenchResultId();
    metrics.clear();
    metricIds.forEach(m -> metrics.add(new ReportItemMetric(m, benchResultId)));
  }

}
