package us.abstracta.jmeter.javadsl.blazemeter;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.blazemeter.api.Location;
import us.abstracta.jmeter.javadsl.blazemeter.api.Project;
import us.abstracta.jmeter.javadsl.blazemeter.api.Test;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestConfig;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestConfig.ExecutionConfig;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRun;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunConfig;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunRequestStats;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunStatus;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunSummaryStats.TestRunLabeledSummary;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.DslJmeterEngine;
import us.abstracta.jmeter.javadsl.engines.BaseRemoteEngine;

/**
 * A {@link DslJmeterEngine} which allows running DslTestPlan in BlazeMeter.
 * <p>
 * <b>Note:</b> the engine always returns 0 as sentBytes statistics, since there is no efficient way
 * to get it from BlazeMeter.
 *
 * @since 0.2
 */
public class BlazeMeterEngine extends BaseRemoteEngine<BlazeMeterClient, BlazeMeterTestPlanStats> {

  private static final Logger LOG = LoggerFactory.getLogger(BlazeMeterEngine.class);
  private static final Duration STATUS_POLL_PERIOD = Duration.ofSeconds(5);

  private final String username;
  private final String password;
  private String testName = "jmeter-java-dsl";
  private Long projectId;
  private Duration testTimeout = Duration.ofHours(1);
  private Duration availableDataTimeout = Duration.ofSeconds(30);
  private Integer totalUsers;
  private Duration rampUp;
  private Integer iterations;
  private Duration holdFor;
  private Integer threadsPerEngine;
  private final List<File> assets = new ArrayList<>();
  private final List<WeightedLocation> locations = new ArrayList<>();
  private boolean useDebugRun;

  /**
   * @param authToken is the authentication token to be used to access BlazeMeter API.
   *                  <p>
   *                  It follows the following format: &lt;Key ID&gt;:&lt;Key Secret&gt;.
   *                  <p>
   *                  Check <a
   *                  href="https://guide.blazemeter.com/hc/en-us/articles/115002213289-BlazeMeter-API-keys-">BlazeMeter
   *                  API keys</a> for instructions on how to generate them.
   */
  public BlazeMeterEngine(String authToken) {
    int tokenSeparatorIndex = authToken.indexOf(':');
    if (tokenSeparatorIndex < 0) {
      throw new IllegalArgumentException(
          "BlazeMeter token does not match with expected format: <apikey>:<secretKey>");
    }
    username = authToken.substring(0, tokenSeparatorIndex);
    password = authToken.substring(tokenSeparatorIndex + 1);
  }

  /**
   * Sets the name of the BlazeMeter test to use.
   * <p>
   * BlazeMeterEngine will search for a test with the given name in the given project (Check
   * {@link #projectId(long)}) and if one exists, it will update it and use it to run the provided
   * test plan. If a test with the given name does not exist, then it will create a new one to run
   * the given test plan.
   * <p>
   * When not specified, the test name defaults to "jmeter-java-dsl".
   *
   * @param testName specifies the name of the test to update or create in BlazeMeter.
   * @return the engine for further configuration or usage.
   */
  public BlazeMeterEngine testName(String testName) {
    this.testName = testName;
    return this;
  }

  /**
   * Specifies the ID of the BlazeMeter project where to run the test.
   * <p>
   * You can get the ID of the project by selecting a given project in BlazeMeter and getting the
   * number right after "/projects" in the URL.
   * <p>
   * When no project ID is specified, then the default one for the user (associated to the given
   * authentication token) is used.
   *
   * @param projectId is the ID of the project to be used to run the test.
   * @return the engine for further configuration or usage.
   */
  public BlazeMeterEngine projectId(long projectId) {
    this.projectId = projectId;
    return this;
  }

  /**
   * Specifies a timeout for the entire test execution.
   * <p>
   * If the timeout is reached then the test run will throw a TimeoutException.
   * <p>
   * It is strongly advised to set this timeout properly in each run, according to the expected test
   * execution time plus some additional margin (to consider for additional delays in BlazeMeter
   * test setup and teardown).
   * <p>
   * This timeout exists to avoid any potential problem with BlazeMeter execution not detected by
   * the client, and avoid keeping the test indefinitely running until is interrupted by a user.
   * This is specially annoying when running tests in automated fashion, for example in CI/CD.
   * <p>
   * When not specified, the default timeout will is set to 1 hour.
   *
   * @param testTimeout to be used as time limit for test execution. If execution takes more than
   *                    this, then a TimeoutException will be thrown by the engine.
   * @return the engine for further configuration or usage.
   */
  public BlazeMeterEngine testTimeout(Duration testTimeout) {
    this.testTimeout = testTimeout;
    return this;
  }

  /**
   * Specifies a timeout for waiting for test data (metrics) to be available in BlazeMeter.
   * <p>
   * After a test is marked as ENDED in BlazeMeter, it may take a few seconds for the associated
   * final metrics to be available. In some cases, the test is marked as ENDED by BlazeMeter, but
   * the data is never available. This usually happens when there is some problem running the test
   * (for example some internal problem with BlazeMeter engine, some missing jmeter plugin, or some
   * other jmeter error). This timeout makes sure that tests properly fail (throwing a
   * TimeoutException) when they are marked as ENDED and no data is available after the given
   * timeout, and avoids unnecessary wait for test execution timeout.
   * <p>
   * Usually this timeout should not be necessary to change, but the API provides such method in
   * case you need to tune such setting.
   * <p>
   * When not specified, this value will default to 30 seconds.
   *
   * @param availableDataTimeout to wait for available data after a test ends, before throwing a
   *                             TimeoutException.
   * @return the engine for further configuration or usage.
   */
  public BlazeMeterEngine availableDataTimeout(Duration availableDataTimeout) {
    this.availableDataTimeout = availableDataTimeout;
    return this;
  }

  /**
   * Specifies the number of virtual users to use when running the test.
   * <p>
   * This value overwrites any value specified in JMeter test plans thread groups.
   * <p>
   * When no configuration is given for totalUsers, rampUpFor, iterations or holdFor, then
   * configuration will be taken from the first default thread group found in the test plan.
   * Otherwise, when no totalUsers is specified, 1 total user for will be used.
   *
   * @param totalUsers number of virtual users to run the test with.
   * @return the engine for further configuration or usage.
   */
  public BlazeMeterEngine totalUsers(int totalUsers) {
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
   * Take into consideration that BlazeMeter does not support specifying this value in units more
   * granular than minutes, so, if you use a finer grain duration, it will be rounded up to minutes
   * (eg: if you specify 61 seconds, this will be translated into 2 minutes).
   * <p>
   * When no configuration is given for totalUsers, rampUpFor, iterations or holdFor, then
   * configuration will be taken from the first default thread group found in the test plan.
   * Otherwise, when no ramp up is specified, 0 ramp up will be used.
   *
   * @param rampUp duration that BlazeMeter will take to spin up all the virtual users.
   * @return the engine for further configuration or usage.
   */
  public BlazeMeterEngine rampUpFor(Duration rampUp) {
    this.rampUp = rampUp;
    return this;
  }

  /**
   * Specifies the number of iterations each virtual user will execute.
   * <p>
   * If both iterations and holdFor are specified, then iterations are ignored and only holdFor is
   * taken into consideration.
   * <p>
   * When neither iterations and holdFor are specified, then the last test run configuration is
   * used, or the criteria specified in the JMeter test plan if no previous test run exists.
   * <p>
   * When no configuration is given for totalUsers, rampUpFor, iterations or holdFor, then
   * configuration will be taken from the first default thread group found in the test plan.
   * Otherwise, when no iterations are specified, infinite iterations will be used.
   *
   * @param iterations for each virtual users to execute.
   * @return the engine for further configuration or usage.
   */
  public BlazeMeterEngine iterations(int iterations) {
    this.iterations = iterations;
    return this;
  }

  /**
   * Specifies the duration of time to keep the virtual users running, after the rampUp period.
   * <p>
   * If both iterations and holdFor are specified, then iterations are ignored and only holdFor is
   * taken into consideration.
   * <p>
   * When neither iterations and holdFor are specified, then the last test run configuration is
   * used, or the criteria specified in the JMeter test plan if no previous test run exists.
   * <p>
   * Take into consideration that BlazeMeter does not support specifying this value in units more
   * granular than minutes, so, if you use a finer grain duration, it will be rounded up to minutes
   * (eg: if you specify 61 seconds, this will be translated into 2 minutes).
   * <p>
   * When no configuration is given for totalUsers, rampUpFor, iterations or holdFor, then
   * configuration will be taken from the first default thread group found in the test plan.
   * Otherwise, when no hold for or iterations are specified, 10 seconds hold for will be used.
   *
   * @param holdFor duration to keep virtual users running after the rampUp period.
   * @return the engine for further configuration or usage.
   */
  public BlazeMeterEngine holdFor(Duration holdFor) {
    this.holdFor = holdFor;
    return this;
  }

  /**
   * Specifies the number of threads/virtual users to use per BlazeMeter engine (host or
   * container).
   * <p>
   * It is always important to use as less resources (which reduces costs) as possible to generate
   * the required load for the test. Too few resources might lead to misguiding results, since the
   * instances/engines running might be saturating and not properly imposing the expected load upon
   * the system under test. Too many resources might lead to unnecessary expenses (wasted money).
   * <p>
   * This setting, in conjunction with totalUsers, determines the number of engines BlazeMeter will
   * use to run the test. For example, if you specify totalUsers to 500 and 100 threadsPerEngine,
   * then 5 engines will be used to run the test.
   * <p>
   * It is important to set this value appropriately, since different test plans may impose
   * different load in BlazeMeter engines. This in turns ends up defining different limit of number
   * of virtual users per engine that a test run requires to properly measure the performance of the
   * system under test. This process is usually referred as "calibration" and you can read more
   * about it <a
   * href="https://guide.blazemeter.com/hc/en-us/articles/360001456978-Calibrating-a-JMeter-Test">here</a>.
   * <p>
   * When not specified, the value of the last test run will be used, or the default one for your
   * BlazeMeter billing plan if no previous test run exists.
   *
   * @param threadsPerEngine the number of threads/virtual users to execute per BlazeMeter engine.
   * @return the engine for further configuration or usage.
   */
  public BlazeMeterEngine threadsPerEngine(int threadsPerEngine) {
    this.threadsPerEngine = threadsPerEngine;
    return this;
  }

  /**
   * Allows specifying asset files that need to be uploaded to BlazeMeter for proper test plan
   * execution.
   * <p>
   * Take into consideration that JMeter DSL will automatically upload files used by
   * {@link us.abstracta.jmeter.javadsl.core.configs.DslCsvDataSet},
   * {@link us.abstracta.jmeter.javadsl.http.DslHttpSampler#bodyFile(String)} and
   * {@link us.abstracta.jmeter.javadsl.http.DslHttpSampler#bodyFilePart(String, String,
   * ContentType)}. So, use this method for any additional files that your test plan requires and is
   * not one used in previously mentioned scenarios.
   *
   * @param files specifies files to upload to BlazeMeter.
   * @return the engine for further configuration or usage.
   * @since 1.11
   */
  public BlazeMeterEngine assets(File... files) {
    assets.addAll(Arrays.asList(files));
    return this;
  }

  /**
   * Allows specifying BlazeMeter locations where the test plan will run.
   * <p>
   * This method allows to use one of the known public locations (from {@link BlazeMeterLocation}}.
   * If you need to use a private location use {@link #location(String, int)}.
   *
   * <p>
   * Use this method multiple times to specify several locations to generate load from multiple
   * locations in parallel.
   * <p>
   * E.g:
   * <pre>{@code
   *  testPlan(
   *    ...
   *  ).runIn(new BlazeMeterEngine(bzToken)
   *  // this scenario will run 50% of the users in GCP us-east1 and the rest in us-west1
   *      .location(BlazeMeterLocation.GCP_US_EAST_1, 0.5)
   *      .location(BlazeMeterLocation.GCP_US_WEST_1, 0.5)
   *  );
   * }</pre>
   * <p>
   * When no location is specified, then the default one will be used.
   *
   * @param location specifies a location where to run test plans.
   * @param weight   specifies the weight of this location over others. For instance, if you have
   *                 two locations one with weight 1 and the other 2, then the first one will get
   *                 1/(2+1)=33% of totalUsers and the other will get the rest of the users. In
   *                 general is easier to think in terms of percentages, for example for the same
   *                 sample set 33 and 67 as weights.
   * @return the engine for further configuration or usage.
   * @since 1.11
   */
  public BlazeMeterEngine location(BlazeMeterLocation location, int weight) {
    return location(location.getName(), weight);
  }

  /**
   * Same as {@link #location(BlazeMeterLocation, int)} but allowing to use any location id (not
   * just public ones).
   *
   * @param location specifies the location id or name (eg:
   *                 <pre>harbor-5b0323b3c648be3b4c7b23c8</pre> or <pre>My Location</pre>).
   * @param weight   specifies the weight of this location over others. For instance, if you have
   *                 two locations one with weight 1 and the other 2, then the first one will get
   *                 1/(2+1)=33% of totalUsers and the other will get the rest of the users. In
   *                 general is easier to think in terms of percentages, for example for the same
   *                 sample set 33 and 67 as weights.
   * @return the engine for further configuration or usage.
   * @see #location(BlazeMeterLocation, int)
   * @since 1.11
   */
  public BlazeMeterEngine location(String location, int weight) {
    locations.add(new WeightedLocation(location, weight));
    return this;
  }

  /**
   * Specifies that the test run will use BlazeMeter debug run feature, not consuming credits but
   * limited up to 10 threads and 5 minutes or 100 iterations.
   *
   * @return the engine for further configuration or usage.
   */
  public BlazeMeterEngine useDebugRun() {
    return useDebugRun(true);
  }

  /**
   * Same as {@link #useDebugRun()} but allowing to enable or disable the settign.
   * <p>
   * This is helpful when the resolution is taken at runtime.
   *
   * @param enable specifies to enable or disable the setting. By default, it is set to false.
   * @return the engine for further configuration or usage.
   * @see #useDebugRun()
   * @since 1.0
   */
  public BlazeMeterEngine useDebugRun(boolean enable) {
    this.useDebugRun = enable;
    return this;
  }

  @Override
  protected BlazeMeterTestPlanStats run(File jmxFile, HashTree tree, BuildTreeContext context)
      throws IOException, InterruptedException, TimeoutException {
    Project project = findProject();
    Test test = apiClient.findTestByName(testName, project).orElse(null);
    TestConfig testConfig = buildTestConfig(project, jmxFile, tree);
    if (test != null) {
      for (String f : apiClient.findTestFiles(test)) {
        apiClient.deleteTestFile(f, test);
      }
      apiClient.updateTest(test, testConfig);
      LOG.info("Updated test {}", test.getUrl());
    } else {
      test = apiClient.createTest(testConfig, project);
      LOG.info("Created test {}", test.getUrl());
    }
    LOG.info("Uploading test script and asset files");
    context.processAssetFile(jmxFile.getPath());
    for (File f : assets) {
      context.processAssetFile(f.getPath());
    }
    for (Map.Entry<String, File> asset : context.getAssetFiles().entrySet()) {
      apiClient.uploadTestFile(asset.getValue(), asset.getKey(), test);
    }
    TestRun testRun = apiClient.startTest(test, buildTestRunConfig());
    LOG.info("Started test run {}", testRun.getUrl());
    awaitTestEnd(testRun);
    return findTestPlanStats(testRun);
  }

  @Override
  protected BlazeMeterClient buildClient() {
    return new BlazeMeterClient(username, password);
  }

  private Project findProject() throws IOException {
    return projectId == null ? apiClient.findDefaultProject()
        : apiClient.findProjectById(this.projectId);
  }

  private TestConfig buildTestConfig(Project project, File jmxFile, HashTree tree)
      throws IOException {
    ExecutionConfig execConfig =
        (totalUsers == null && rampUp == null && iterations == null && holdFor == null)
            ? ExecutionConfig.fromThreadGroup(extractFirstThreadGroup(tree))
            : new ExecutionConfig(totalUsers != null ? totalUsers : 1,
                rampUp != null ? rampUp : Duration.ZERO,
                iterations != null ? iterations : -1,
                holdFor != null ? holdFor : (iterations != null ? null : Duration.ofSeconds(10)));
    execConfig.setLocationsPercents(buildLocationsPercents(project));
    return new TestConfig()
        .name(testName)
        .projectId(project.getId())
        .jmxFile(jmxFile)
        .execConfig(execConfig)
        .threadsPerEngine(threadsPerEngine);
  }

  private Map<String, Integer> buildLocationsPercents(Project project) throws IOException {
    double totalWeight = locations.stream()
        .mapToDouble(l -> l.weight)
        .sum();
    Map<String, Integer> ret = new HashMap<>();
    for (WeightedLocation l : locations) {
      ret.put(solveLocationId(l.location, project),
          (int) Math.round(((double) l.weight / totalWeight) * 100));
    }
    return ret;
  }

  private String solveLocationId(String location, Project project) throws IOException {
    if (location.startsWith("harbor-")
        || Arrays.stream(BlazeMeterLocation.values()).anyMatch(l -> l.getName().equals(location))) {
      return location;
    }
    Location found = apiClient.findPrivateLocationByName(location, project);
    return found != null ? "harbor-" + found.getId() : location;
  }

  private TestRunConfig buildTestRunConfig() {
    TestRunConfig ret = new TestRunConfig();
    if (useDebugRun) {
      ret.debugRun();
    }
    return ret;
  }

  private void awaitTestEnd(TestRun testRun)
      throws InterruptedException, IOException, TimeoutException {
    TestRunStatus status = TestRunStatus.CREATED;
    Instant testStart = Instant.now();
    do {
      Thread.sleep(STATUS_POLL_PERIOD.toMillis());
      TestRunStatus newStatus = apiClient.findTestRunStatus(testRun);
      if (!status.equals(newStatus)) {
        LOG.debug("Test run {} status changed to: {}", testRun.getUrl(), newStatus);
        status = newStatus;
      }
    } while (!TestRunStatus.ENDED.equals(status) && !hasTimedOut(testStart, testTimeout));
    if (!TestRunStatus.ENDED.equals(status)) {
      LOG.warn("Test execution timed out after {}. Stopping test run ...",
          prettyDuration(testTimeout));
      apiClient.stopTestRun(testRun);
      LOG.info("Test run stopped.");
      throw buildTestTimeoutException(testRun);
    } else if (!status.isDataAvailable()) {
      testRun = apiClient.findTestRunById(testRun.getId());
      if (testRun.isErrorStatus()) {
        throw new BlazeMeterException(400, String.join("\n", testRun.getErrorMessages()));
      }
      awaitAvailableData(testRun, testStart);
    }
  }

  private boolean hasTimedOut(Instant start, Duration timeout) {
    return Duration.between(start, Instant.now()).compareTo(timeout) >= 0;
  }

  private TimeoutException buildTestTimeoutException(TestRun testRun) {
    return new TimeoutException(String.format(
        "Test %s didn't end after %s. "
            + "If the timeout is too short, you can change it with testTimeout() method.",
        testRun.getUrl(), prettyDuration(testTimeout)));
  }

  private void awaitAvailableData(TestRun testRun, Instant testStart)
      throws InterruptedException, IOException, TimeoutException {
    TestRunStatus status;
    Instant dataPollStart = Instant.now();
    do {
      Thread.sleep(STATUS_POLL_PERIOD.toMillis());
      status = apiClient.findTestRunStatus(testRun);
    } while (!status.isDataAvailable() && !hasTimedOut(testStart, testTimeout) && !hasTimedOut(
        dataPollStart, availableDataTimeout));
    if (hasTimedOut(testStart, testTimeout)) {
      throw buildTestTimeoutException(testRun);
    } else if (!status.isDataAvailable()) {
      throw new TimeoutException(String.format(
          "Test %s ended, but no data is available after %s. "
              + "This is usually caused by some failure in BlazeMeter. "
              + "Check bzt.log and jmeter.out, and if everything looks good you might try "
              + "increasing this timeout with availableDataTimeout() method.", testRun.getUrl(),
          prettyDuration(availableDataTimeout)));
    }
  }

  private BlazeMeterTestPlanStats findTestPlanStats(TestRun testRun) throws IOException {
    TestRunLabeledSummary summary = apiClient.findTestRunSummaryStats(testRun).getSummary().get(0);
    List<TestRunRequestStats> labeledStats = apiClient.findTestRunRequestStats(testRun);
    return new BlazeMeterTestPlanStats(summary, labeledStats);
  }

  private static class WeightedLocation {

    private final String location;
    private final int weight;

    private WeightedLocation(String location, int weight) {
      this.location = location;
      this.weight = weight;
    }

  }

}
