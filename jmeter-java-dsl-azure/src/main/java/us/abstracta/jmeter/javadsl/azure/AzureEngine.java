package us.abstracta.jmeter.javadsl.azure;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.azure.api.LoadTest;
import us.abstracta.jmeter.javadsl.azure.api.LoadTestResource;
import us.abstracta.jmeter.javadsl.azure.api.Location;
import us.abstracta.jmeter.javadsl.azure.api.ResourceGroup;
import us.abstracta.jmeter.javadsl.azure.api.Subscription;
import us.abstracta.jmeter.javadsl.azure.api.TestRun;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.DslJmeterEngine;
import us.abstracta.jmeter.javadsl.engines.BaseRemoteEngine;

/**
 * A {@link DslJmeterEngine} which allows running DslTestPlan in Azure Load Testing.
 * <p>
 * To use this engine you need:
 * <ul>
 * <li>To create a test resource and resource group in Azure Load Testing. First defined test
 * resource for the subscription, is used by default.</li>
 * <li>Register an application in Azure with proper permissions for Azure Load Testing with an
 * associated secret. Check <a href="https://learn.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal">here</a>
 * for more details.</li>
 * </ul>
 *
 * @since 1.10
 */
public class AzureEngine extends BaseRemoteEngine<AzureClient, AzureTestPlanStats> {

  private static final Logger LOG = LoggerFactory.getLogger(AzureEngine.class);
  private static final String DEFAULT_NAME = "jmeter-java-dsl";
  private static final Duration STATUS_POLL_PERIOD = Duration.ofSeconds(5);
  private static final Duration PROVISIONING_TIMEOUT = Duration.ofMinutes(1);
  private static final Duration VALIDATION_TIMEOUT = Duration.ofMinutes(10);
  private static final Duration TEST_END_TIMEOUT = Duration.ofMinutes(2);

  private final String tenantId;
  private final String clientId;
  private final String clientSecret;
  private String subscriptionId;
  private String resourceGroupName;
  private String location;
  private String testResourceName;
  private String testName = DEFAULT_NAME;
  private Duration testTimeout = Duration.ofHours(1);
  private int engines = 1;

  /**
   * Builds a new AzureEngine from a given string containing tenant id, client id and client secrets
   * separated by colons.
   * <p>
   * This is just a handy way to specify credentials in a string (eg: environment variable) and
   * easily create an Azure Engine. For a more explicit way you may use
   * {@link #AzureEngine(String, String, String)}.
   *
   * @param credentials contains tenant id, client id and client secrets separated by colons. Eg:
   *                    myTenantId:myClientId:mySecret.
   *                    <p>
   *                    Check <a
   *                    href="https://learn.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal">the
   *                    Azure guide</a> for instructions on how to register an application with
   *                    proper access.
   *                    <p>
   *                    Tip: there is no need to specify a redirect uri.
   *                    <p>
   *                    The tenantId can easily be retrieved getting subscription info in Azure
   *                    Portal.
   */
  public AzureEngine(String credentials) {
    String[] parts = credentials.split(":", 3);
    tenantId = parts[0];
    clientId = parts[1];
    clientSecret = parts[2];
  }

  /**
   * This is a more explicit way to create AzureEngine than {@link #AzureEngine(String)}.
   * <p>
   * This is usually preferred when you already have each credential value separated, as is more
   * explicit and don't require encoding into a string.
   *
   * @param tenantId     is the tenant id for your subscription. This can easily be retrieved
   *                     getting subscription info in Azure Portal
   * @param clientId     this is the id associated to the test that needs to run in Azure. You
   *                     should use one application for each JMeter DSL project that uses Azure Load
   *                     Testing. This can be retrieved when register an application following steps
   *                     detailed in <a
   *                     href="https://learn.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal">this
   *                     Azure guide</a>.
   * @param clientSecret this is a client secret generated for the test to be run in Azure.
   */
  public AzureEngine(String tenantId, String clientId, String clientSecret) {
    this.tenantId = tenantId;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  /**
   * Allows specifying the Azure subscription ID to run the tests on.
   * <p>
   * By default, AzureEngine will use any subscription associated to the given tenant. In most of
   * the scenarios, when you only use one subscription, this behavior is good. But, when you have
   * multiple subscriptions, it is necessary to specify which subscription from the available ones
   * you want to use. This method is for those scenarios.
   *
   * @param subscriptionId specifies the Azure subscription identifier to use while running tests.
   *                       When not specified, any subscription associated to the tenant will be
   *                       used.
   * @return the engine for further configuration or usage.
   */
  public AzureEngine subscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
    return this;
  }

  /**
   * Specifies the name the resource group where tests will be created or updated.
   * <p>
   * You can use Azure resource groups to group different test resources (projects, systems under
   * test) shared by members of a team.
   * <p>
   * If a resource group exists with the given name, then that group will be used. Otherwise, a new
   * one will be created. You can use {@link #location(String)} to specify the location where the
   * resource group will be created (by default, the first available one will be used, eg: eastus).
   *
   * @param resourceGroupName specifies the name of the resource group to use. If no name is
   *                          specified, then the test resource name
   *                          ({@link #testResourceName(String)} plus "-rg" suffix is used. Eg:
   *                          jmeter-java-dsl-rg.
   * @return the engine for further configuration or usage.
   */
  public AzureEngine resourceGroupName(String resourceGroupName) {
    this.resourceGroupName = resourceGroupName;
    return this;
  }

  /**
   * Specifies the location where to create new resource groups.
   *
   * @param location the Azure location to use when creating new resource groups. If none is
   *                 specified, then the first available location will be used (eg: eastus).
   * @return the engine for further configuration or usage.
   * @see #resourceGroupName(String)
   */
  public AzureEngine location(String location) {
    this.location = location;
    return this;
  }

  /**
   * Specifies the name of the test resource where tests will be created or updated.
   * <p>
   * You can use Azure test resources to group different tests resources belonging to the same
   * project or system under test.
   * <p>
   * If a test resource exists with the given name, then that test resources will be used.
   * Otherwise, a new one will be created.
   *
   * @param testResourceName specifies the name of the test resource. If no name is specified, then
   *                         the test name ({@link #testName(String)}) is used.
   * @return the engine for further configuration or usage.
   */
  public AzureEngine testResourceName(String testResourceName) {
    this.testResourceName = testResourceName;
    return this;
  }

  /**
   * Specifies the name of the test to be created or updated.
   * <p>
   * If a test with the given name exists, then the test is updated. Otherwise, a new one is
   * created.
   *
   * @param testName specifies the name of the test to create or update. If no name is specified,
   *                 then jmeter-java-dsl is used by default.
   * @return the engine for further configuration or usage.
   */
  public AzureEngine testName(String testName) {
    this.testName = testName;
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
   * @param duration to be used as time limit for test execution. If execution takes more than this,
   *                 then a TimeoutException will be thrown by the engine.
   * @return the engine for further configuration or usage.
   */
  public AzureEngine testTimeout(Duration duration) {
    this.testTimeout = duration;
    return this;
  }

  /**
   * Specifies the number of JMeter engine instances where the test plan should run.
   * <p>
   * This value directly impact the generated load. For example: if your test plan defines to use a
   * thread group with 100 users, then using 3 engines will result in 300 parallel users. Azure Load
   * Testing simply runs the test plan in as many engines specified by this value.
   *
   * @param count specifies the number of JMeter engine instances to run the test plan on. When not
   *              specified it just runs the test plan in 1 engine.
   * @return the engine for further configuration or usage.
   */
  public AzureEngine engines(int count) {
    this.engines = count;
    return this;
  }

  @Override
  protected AzureClient buildClient() {
    return new AzureClient(tenantId, clientId, clientSecret);
  }

  @Override
  protected AzureTestPlanStats run(File jmxFile, HashTree tree, BuildTreeContext context)
      throws IOException, InterruptedException, TimeoutException {
    Subscription subscription = subscriptionId != null ? new Subscription(subscriptionId, tenantId)
        : apiClient.findSubscription();
    String resourceName = testResourceName != null ? testResourceName : testName;
    String groupName = resourceGroupName != null ? resourceGroupName : resourceName + "-rg";
    ResourceGroup resourceGroup = apiClient.findResourceGroup(groupName, subscription);
    if (resourceGroup == null) {
      resourceGroup = createResourceGroup(groupName, subscription);
    }
    LoadTestResource testResource = apiClient.findTestResource(resourceName, resourceGroup);
    if (testResource == null) {
      testResource = createTestResource(resourceName, resourceGroup);
    }
    LoadTest loadTest = apiClient.findTestByName(testName, testResource);
    if (loadTest == null) {
      loadTest = createLoadTest(testResource);
    } else {
      clearLoadTest(loadTest);
    }
    LOG.info("Uploading test script");
    apiClient.uploadTestFile(loadTest.getTestId(), jmxFile);
    LOG.info("Validating test script");
    awaitValidatedTestFile(loadTest);
    TestRun testRun = new TestRun(loadTest.getTestId());
    testRun = apiClient.createTestRun(testRun);
    if (!testRun.isAccepted()) {
      throw new IllegalStateException(
          "The test run was not accepted. Check your usage of Azure quotas.");
    }
    LOG.info("Started test run {}", testRun.getUrl());
    testRun = awaitTestEnd(testRun);
    LOG.info("Test run completed");
    return new AzureTestPlanStats(testRun);
  }

  private ResourceGroup createResourceGroup(String groupName, Subscription subscription)
      throws IOException, InterruptedException, TimeoutException {
    Location location = this.location != null ? new Location(this.location)
        : apiClient.findLocation(subscription);
    ResourceGroup ret = new ResourceGroup(groupName, location, subscription);
    apiClient.createResourceGroup(ret);
    LOG.info("Created resource group {}", ret.getUrl());
    awaitProvisionedResourceGroup(ret);
    return ret;
  }

  private void awaitProvisionedResourceGroup(ResourceGroup group)
      throws IOException, InterruptedException, TimeoutException {
    awaitStatus(group, () -> apiClient.findResourceGroup(group.getName(), group.getSubscription()),
        ResourceGroup::isPendingProvisioning, ResourceGroup::isProvisioned, PROVISIONING_TIMEOUT,
        "resource group provisioning", "Azure usage");
  }

  private <T> void awaitStatus(T entity, EntityProvider<T> entityProvider,
      Predicate<T> checkIntermediateStatus, Predicate<T> checkSuccessStatus, Duration timeout,
      String waitName, String detailsName)
      throws InterruptedException, TimeoutException, IOException {
    if (checkSuccessStatus.test(entity)) {
      return;
    }
    LOG.info("Waiting for " + waitName + "... ");
    Instant start = Instant.now();
    while (!hasTimedOut(timeout, start) && checkIntermediateStatus.test(entity)) {
      Thread.sleep(STATUS_POLL_PERIOD.toMillis());
      entity = entityProvider.get();
    }
    if (checkIntermediateStatus.test(entity)) {
      throw new TimeoutException(
          "Timeout while waiting for " + waitName + " after " + prettyDuration(timeout)
              + ". You may try executing again, or create an issue in jmeter-java-dsl GitHub "
              + "repository with " + detailsName + " details.");
    } else if (!checkSuccessStatus.test(entity)) {
      throw new IllegalArgumentException(
          firstLetterToUpper(waitName) + " failed. Create an issue in jmeter-java-dsl GitHub "
              + "repository with " + detailsName + " details.");
    }
  }

  private interface EntityProvider<T> {

    T get() throws IOException;

  }

  private String firstLetterToUpper(String str) {
    return str.substring(0, 1).toUpperCase(Locale.US) + str.substring(1);
  }

  private LoadTestResource createTestResource(String resourceName, ResourceGroup resourceGroup)
      throws IOException, InterruptedException, TimeoutException {
    LoadTestResource ret = new LoadTestResource(resourceName, resourceGroup);
    apiClient.createTestResource(ret);
    LOG.info("Created test resource {}", ret.getUrl());
    awaitProvisionedTestResource(ret);
    return ret;
  }

  private void awaitProvisionedTestResource(LoadTestResource testResource)
      throws IOException, InterruptedException, TimeoutException {
    awaitStatus(testResource,
        () -> apiClient.findTestResource(testResource.getName(), testResource.getResourceGroup()),
        LoadTestResource::isPendingProvisioning, LoadTestResource::isProvisioned,
        PROVISIONING_TIMEOUT, "test resource provisioning", "Azure usage");
  }

  private LoadTest createLoadTest(LoadTestResource testResource) throws IOException {
    LoadTest ret = new LoadTest(testName, engines, testResource);
    apiClient.createTest(ret);
    LOG.info("Created test {}", ret.getUrl());
    return ret;
  }

  private void clearLoadTest(LoadTest loadTest) throws IOException {
    LOG.info("Updating test {}", loadTest.getUrl());
    String previousScript = loadTest.getTestScriptFileName();
    if (previousScript != null) {
      apiClient.deleteTestFile(loadTest.getTestId(), previousScript);
      loadTest.removeTestScriptFile();
    }
  }

  private void awaitValidatedTestFile(final LoadTest loadTest)
      throws TimeoutException, InterruptedException, IOException {
    awaitStatus(loadTest, () -> apiClient.findTestById(loadTest.getTestId()),
        LoadTest::isPendingValidation, LoadTest::isSuccessValidation, VALIDATION_TIMEOUT,
        "test script validation", "test plan");
  }

  private TestRun awaitTestEnd(TestRun testRun)
      throws InterruptedException, IOException, TimeoutException {
    Instant start = Instant.now();
    while (!testRun.isEnded() && !hasTimedOut(testTimeout, start)) {
      Thread.sleep(STATUS_POLL_PERIOD.toMillis());
      testRun = apiClient.findTestRunById(testRun.getId());
    }
    if (!testRun.isEnded()) {
      String prettyTimeout = prettyDuration(testTimeout);
      LOG.warn("Test execution timed out after {}. Stopping test run ...", prettyTimeout);
      apiClient.stopTestRun(testRun.getId());
      LOG.info("Test run stopped.");
      throw new TimeoutException("Test execution timed out after " + prettyTimeout);
    }
    return awaitVirtualUsers(testRun);
  }

  private TestRun awaitVirtualUsers(TestRun testRun) throws InterruptedException, IOException {
    Instant start = Instant.now();
    while (testRun.getVirtualUsers() == null && !hasTimedOut(TEST_END_TIMEOUT, start)) {
      Thread.sleep(STATUS_POLL_PERIOD.toMillis());
      testRun = apiClient.findTestRunById(testRun.getId());
    }
    if (!testRun.isSuccess()) {
      throw new IllegalStateException("Test has been " + testRun.getStatus().toLowerCase());
    }
    return testRun;
  }

}
