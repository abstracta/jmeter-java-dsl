package us.abstracta.jmeter.javadsl.blazemeter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.blazemeter.api.Project;
import us.abstracta.jmeter.javadsl.blazemeter.api.Test;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestConfig;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRun;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunConfig;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunRequestStats;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunStatus;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestRunSummaryStats.TestRunLabeledSummary;
import us.abstracta.jmeter.javadsl.core.DslJmeterEngine;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.TestPlanStats.StatsSummary;

/**
 * A {@link DslJmeterEngine} which allows running DslTestPlan in BlazeMeter.
 */
public class BlazeMeterEngine implements DslJmeterEngine {

  private static final Logger LOG = LoggerFactory.getLogger(BlazeMeterEngine.class);
  private static final String BASE_URL = "https://a.blazemeter.com";
  private static final Duration STATUS_POLL_PERIOD = Duration.ofSeconds(5);

  private final BlazeMeterClient client;
  private String testName = "jmeter-java-dsl";
  private Long projectId;
  private Duration testTimeout = Duration.ofHours(1);
  private Duration availableDataTimeout = Duration.ofSeconds(30);
  private Integer totalUsers;
  private Duration rampUp;
  private Integer iterations;
  private Duration holdFor;
  private Integer threadsPerEngine;
  private boolean useDebugRun;

  /**
   * @param authToken is the authentication token to be used to access BlazeMeter API.
   * <p>
   * It follows the following format: &lt;Key ID&gt;:&lt;Key Secret&gt;.
   * <p>
   * Check <a href="https://guide.blazemeter.com/hc/en-us/articles/115002213289-BlazeMeter-API-keys-">BlazeMeter
   * API keys</a> for instructions on how to generate them.
   */
  public BlazeMeterEngine(String authToken) {
    client = new BlazeMeterClient(BASE_URL + "/api/v4/", authToken);
  }

  /**
   * Sets the name of the BlazeMeter test to use.
   * <p>
   * BlazeMeterEngine will search for a test with the given name in the given project (Check {@link
   * #projectId(long)}) and if one exists, it will update it and use it to run the provided test
   * plan. If a test with the given name does not exist, then it will create a new one to run the
   * given test plan.
   * <p>
   * When not specified, the test name defaults to "jmeter-java-dsl".
   *
   * @param testName specifies the name of the test to update or create in BlazeMeter.
   * @return the modified instance for fluent API usage.
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
   * @return the modified instance for fluent API usage.
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
   * this, then a TimeoutException will be thrown by the engine.
   * @return the modified instance for fluent API usage.
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
   * TimeoutException.
   * @return the modified instance for fluent API usage.
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
   * When not specified, then the last test run (with same name) value or 1 (if none has ever been
   * specified) will be used.
   *
   * @param totalUsers number of virtual users to run the test with.
   * @return the modified instance for fluent API usage.
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
   * When not specified, the last test run (with same name) value or 0 (if none has ever been
   * specified) will be used.
   *
   * @param rampUp duration that BlazeMeter will take to spin up all the virtual users.
   * @return the modified instance for fluent API usage.
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
   * When specified, this value overwrites any value specified in JMeter test plans thread groups.
   *
   * @param iterations for each virtual users to execute.
   * @return the modified instance for fluent API usage.
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
   * When specified, this value overwrites any value specified in JMeter test plans thread groups.
   *
   * @param holdFor duration to keep virtual users running after the rampUp period.
   * @return the modified instance for fluent API usage.
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
   * the system under test. Too much resources might lead to unnecessary expenses (wasted money).
   * <p>
   * This setting, in conjunction with totalUsers, determine the number of engines BlazeMeter will
   * use to run the test. For example, if you specify totalUsers to 500 and 100 threadsPerEngine,
   * then 5 engines will be used to run the test.
   * <p>
   * It is important to set this value appropriately, since different test plans may impose
   * different load in BlazeMeter engines. This in turns ends up defining different limit of number
   * of virtual users per engine that a test run requires to properly measure the performance of the
   * system under test. This process is usually referred as "calibration" and you can read more
   * about it <a href="https://guide.blazemeter.com/hc/en-us/articles/360001456978-Calibrating-a-JMeter-Test">here</a>.
   * <p>
   * When not specified, the value of the last test run will be used, or the default one for your
   * BlazeMeter billing plan if no previous test run exists.
   *
   * @param threadsPerEngine the number of threads/virtual users to execute per BlazeMeter engine.
   * @return the modified instance for fluent API usage.
   */
  public BlazeMeterEngine threadsPerEngine(int threadsPerEngine) {
    this.threadsPerEngine = threadsPerEngine;
    return this;
  }

  /**
   * Specifies that the test run will use BlazeMeter debug run feature, not consuming credits but
   * limited up to 10 threads and 5 mins or 100 iterations.
   *
   * @return the modified instance for fluent API usage.
   */
  public BlazeMeterEngine useDebugRun() {
    this.useDebugRun = true;
    return this;
  }

  @Override
  public TestPlanStats run(DslTestPlan testPlan)
      throws IOException, InterruptedException, TimeoutException {
    Project project = findProject();
    /*
     Create file within temporary directory instead of just temporary file, to control the name of
     the file, which is later used by BlazeMeter test.
     */
    File jmxFile = Files.createTempDirectory("jmeter-dsl").resolve("test.jmx").toFile();
    try {
      testPlan.saveAsJmx(jmxFile.getPath());
      Test test = client.findTestByName(testName, project).orElse(null);
      TestConfig testConfig = buildTestConfig(project, jmxFile);
      if (test != null) {
        client.updateTest(test, testConfig);
        LOG.info("Updated test {}", test.getUrl());
      } else {
        test = client.createTest(testConfig, project);
        LOG.info("Created test {}", test.getUrl());
      }
      client.uploadTestFile(test, jmxFile);
      TestRun testRun = client.startTest(test, buildTestRunConfig());
      LOG.info("Started test run {}", testRun.getUrl());
      awaitTestEnd(testRun);
      return findTestPlanStats(testRun);
    } finally {
      if (jmxFile.delete()) {
        jmxFile.getParentFile().delete();
      }
    }
  }

  private Project findProject() throws IOException {
    String appBaseUrl = BASE_URL + "/app/#";
    return projectId == null ? client.findDefaultProject(appBaseUrl)
        : client.findProjectById(this.projectId, appBaseUrl);
  }

  private TestConfig buildTestConfig(Project project, File jmxFile) {
    return new TestConfig()
        .name(testName)
        .projectId(project.getId())
        .jmxFile(jmxFile)
        .totalUsers(totalUsers)
        .rampUp(rampUp)
        .iterations(iterations)
        .holdFor(holdFor)
        .threadsPerEngine(threadsPerEngine);
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
      TestRunStatus newStatus = client.findTestRunStatus(testRun);
      if (!status.equals(newStatus)) {
        LOG.debug("Test run {} status changed to: {}", testRun.getUrl(), newStatus);
        status = newStatus;
      }
    } while (!TestRunStatus.ENDED.equals(status) && !hasTimedOut(testStart, testTimeout));
    if (!TestRunStatus.ENDED.equals(status)) {
      throw buildTestTimeoutException(testRun);
    } else if (!status.isDataAvailable()) {
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
        testRun.getUrl(), testTimeout));
  }

  private void awaitAvailableData(TestRun testRun, Instant testStart)
      throws InterruptedException, IOException, TimeoutException {
    TestRunStatus status;
    Instant dataPollStart = Instant.now();
    do {
      Thread.sleep(STATUS_POLL_PERIOD.toMillis());
      status = client.findTestRunStatus(testRun);
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
          availableDataTimeout));
    }
  }

  private TestPlanStats findTestPlanStats(TestRun testRun) throws IOException {
    TestRunLabeledSummary summary = client
        .findTestRunSummaryStats(testRun).getSummary().get(0);
    List<TestRunRequestStats> labeledStats = client
        .findTestRunRequestStats(testRun);
    return buildTestStats(summary, labeledStats);
  }

  private TestPlanStats buildTestStats(TestRunLabeledSummary summary,
      List<TestRunRequestStats> labeledStats) {
    TestPlanStats stats = new TestPlanStats();
    for (TestRunRequestStats labeledStat : labeledStats) {
      StatsSummary labelStatsSummary = new BlazemeterStatsSummary(labeledStat, summary);
      if ("ALL".equals(labeledStat.getLabelName())) {
        stats.setOverallStats(labelStatsSummary);
      } else {
        stats.setLabeledStats(labeledStat.getLabelName(), labelStatsSummary);
      }
    }
    return stats;
  }

  private static class BlazemeterStatsSummary implements StatsSummary {

    private final Instant firstTime;
    private final Instant endTime;
    private final Duration elapsedTime;
    private final long samplesCount;
    private final double samplesPerSecond;
    private final long errorsCount;
    private final Duration minElapsedTime;
    private final Duration maxElapsedTime;
    private final Duration meanElapsedTime;
    private final Duration elapsedTimePercentile90;
    private final Duration elapsedTimePercentile95;
    private final Duration elapsedTimePercentile99;
    private final long receivedBytes;
    private final double receivedBytesPerSecond;

    private BlazemeterStatsSummary(TestRunRequestStats labeledStat,
        TestRunLabeledSummary summary) {
      /*
       These two values are approximations, since BZ api does not provide such information per label
       and calculating it from result logs would incur in significant additional time and resources
       usage.
       */
      firstTime = summary.getFirst();
      endTime = summary.getLast();
      elapsedTime = Duration.ofMillis(labeledStat.getDuration());
      samplesCount = labeledStat.getSamples();
      samplesPerSecond = labeledStat.getAvgThroughput();
      errorsCount = labeledStat.getErrorsCount();
      minElapsedTime = Duration.ofMillis(labeledStat.getMinResponseTime());
      maxElapsedTime = Duration.ofMillis(labeledStat.getMaxResponseTime());
      meanElapsedTime = Duration.ofMillis(Math.round(labeledStat.getAvgResponseTime()));
      elapsedTimePercentile90 = Duration.ofMillis(labeledStat.getPerc90());
      elapsedTimePercentile95 = Duration.ofMillis(labeledStat.getPerc95());
      elapsedTimePercentile99 = Duration.ofMillis(labeledStat.getPerc99());
      // Similar comment as with firstTime and endTime: this is just an approximation.
      receivedBytes = Math.round(labeledStat.getAvgBytes() / 1000 * labeledStat.getDuration());
      receivedBytesPerSecond = labeledStat.getAvgBytes();
    }

    @Override
    public Instant firstTime() {
      return firstTime;
    }

    @Override
    public Instant endTime() {
      return endTime;
    }

    @Override
    public Duration elapsedTime() {
      return elapsedTime;
    }

    @Override
    public long samplesCount() {
      return samplesCount;
    }

    @Override
    public double samplesPerSecond() {
      return samplesPerSecond;
    }

    @Override
    public long errorsCount() {
      return errorsCount;
    }

    @Override
    public Duration minElapsedTime() {
      return minElapsedTime;
    }

    @Override
    public Duration maxElapsedTime() {
      return maxElapsedTime;
    }

    @Override
    public Duration meanElapsedTime() {
      return meanElapsedTime;
    }

    @Override
    public Duration elapsedTimePercentile90() {
      return elapsedTimePercentile90;
    }

    @Override
    public Duration elapsedTimePercentile95() {
      return elapsedTimePercentile95;
    }

    @Override
    public Duration elapsedTimePercentile99() {
      return elapsedTimePercentile99;
    }

    @Override
    public long receivedBytes() {
      return receivedBytes;
    }

    @Override
    public double receivedBytesPerSecond() {
      return receivedBytesPerSecond;
    }

    @Override
    public long sentBytes() {
      throw new UnsupportedOperationException(
          "BlazeMeter API does not provide an efficient way to get this value.");
    }

    @Override
    public double sentBytesPerSecond() {
      throw new UnsupportedOperationException(
          "BlazeMeter API does not provide an efficient way to get this value.");
    }

  }

}
