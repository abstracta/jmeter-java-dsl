package us.abstracta.jmeter.javadsl.azure;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.http.entity.ContentType;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.azure.api.AppComponents;
import us.abstracta.jmeter.javadsl.azure.api.FileInfo;
import us.abstracta.jmeter.javadsl.azure.api.LoadTest;
import us.abstracta.jmeter.javadsl.azure.api.LoadTestResource;
import us.abstracta.jmeter.javadsl.azure.api.Location;
import us.abstracta.jmeter.javadsl.azure.api.ResourceGroup;
import us.abstracta.jmeter.javadsl.azure.api.Subscription;
import us.abstracta.jmeter.javadsl.azure.api.TestRun;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.DslJmeterEngine;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.engines.TestStopper;
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
  private String testRunName;
  private Duration testTimeout = Duration.ofHours(1);
  private int engines = 1;
  private final List<File> assets = new ArrayList<>();
  private boolean splitCsvs;
  private final List<String> monitoredResources = new ArrayList<>();

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
   * explicit and doesn't require encoding into a string.
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
   *                          ({@link #testResourceName(String)}) plus "-rg" suffix is used. Eg:
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
   * Allows to specify the name for the individual test run.
   * <p>
   * This is helpful, for example, if you want to add info to correlate to other entities like CI/CD
   * job id, product version, git tag, etc.
   *
   * @param testRunName specifies the name to be used in the test run. By default,
   *                    TestRun_&lt;YYYY-MM-DD_HH:MM:SS&gt; is used.
   * @return the engine for further configuration or usage.
   * @since 1.18
   */
  public AzureEngine testRunName(String testRunName) {
    this.testRunName = testRunName;
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

  /**
   * Allows specifying asset files that need to be uploaded to Azure Load Testing for proper test
   * plan execution.
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
   * @since 1.18
   */
  public AzureEngine assets(File... files) {
    assets.addAll(Arrays.asList(files));
    return this;
  }

  /**
   * Allow to enable/disable splitting provided CSV files between test engines.
   * <p>
   * Enabling the split allows to use a separate set of records in each engine.
   *
   * @param enabled specifies to split or not CSV files between engines. By default, it is set to
   *                false.
   * @return the engine for further configuration or usage.
   * @since 1.18
   */
  public AzureEngine splitCsvsBetweenEngines(boolean enabled) {
    splitCsvs = enabled;
    return this;
  }

  /**
   * Allows registering application components to monitor and collect statistics alongside the test
   * collected statistics.
   * <p>
   * This is useful to get a full view of test execution results and analyze how the test affects
   * the service under test components.
   *
   * @param resourceIds specifies the Azure resources ids of each of the application components to
   *                    collect metrics from. To get the ids you can navigate in Azure portal to the
   *                    resource and copy part of the URL from the browser. For example a resource
   *                    id for a container app looks like
   *                    <pre>/subscriptions/my-subscription-id/resourceGroups/my-resource-group/providers/Microsoft.App/containerapps/my-papp</pre>.
   * @return the engine for further configuration or usage.
   * @since 1.18
   */
  public AzureEngine monitoredResources(String... resourceIds) {
    monitoredResources.addAll(Arrays.asList(resourceIds));
    return this;
  }

  @Override
  protected HashTree buildTree(DslTestPlan testPlan, BuildTreeContext context) {
    HashTree ret = super.buildTree(testPlan, context);
    if (isAutoStoppableTest(ret)) {
      AzureTestStopper.addClientSecretVariableToTree(ret, context);
    }
    return ret;
  }

  @Override
  protected AzureClient buildClient() {
    return new AzureClient(tenantId, clientId, clientSecret);
  }

  @Override
  protected TestStopper buildTestStopper() {
    return new AzureTestStopper();
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
      if (!monitoredResources.isEmpty()) {
        apiClient.updateAppComponents(loadTest.getTestId(), new AppComponents(monitoredResources));
      }
    } else {
      updateLoadTest(loadTest);
      clearTestFiles(loadTest);
      updateAppComponents(loadTest);
    }
    uploadTestFiles(jmxFile, tree, context, loadTest);
    TestRun testRun = new TestRun(loadTest.getTestId(), solveTestRunName());
    if (isAutoStoppableTest(tree)) {
      AzureTestStopper.setupTestRun(testRun, tenantId, clientId, clientSecret,
          apiClient.getDataPlaneUrl());
    }
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
        "resource group provisioning", "Azure usage", null);
  }

  private <T> void awaitStatus(T entity, EntityProvider<T> entityProvider,
      Predicate<T> checkIntermediateStatus, Predicate<T> checkSuccessStatus, Duration timeout,
      String waitName, String detailsName, Function<T, String> failureDetailExtractor)
      throws InterruptedException, TimeoutException, IOException {
    if (checkSuccessStatus.test(entity)) {
      return;
    }
    LOG.info("Waiting for {}... ", waitName);
    Instant start = Instant.now();
    while (!hasTimedOut(timeout, start) && checkIntermediateStatus.test(entity)) {
      Thread.sleep(STATUS_POLL_PERIOD.toMillis());
      entity = entityProvider.get();
    }
    if (checkIntermediateStatus.test(entity)) {
      throw new TimeoutException(String.format("Timeout while waiting for %s after %s. "
          + "You may try executing again, or create an issue in jmeter-java-dsl GitHub repository "
          + "with %s details.", waitName, prettyDuration(timeout), detailsName));
    } else if (!checkSuccessStatus.test(entity)) {
      String failureDetails =
          failureDetailExtractor != null ? failureDetailExtractor.apply(entity) : null;
      throw new IllegalArgumentException(String.format("%s failed%s. "
              + "Create an issue in jmeter-java-dsl GitHub repository with %s details.",
          firstLetterToUpper(waitName), failureDetails != null ? " due to: " + failureDetails : "",
          detailsName));
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
        PROVISIONING_TIMEOUT, "test resource provisioning", "Azure usage", null);
  }

  private LoadTest createLoadTest(LoadTestResource testResource) throws IOException {
    LoadTest ret = new LoadTest(testName, engines, splitCsvs, testResource);
    apiClient.updateTest(ret);
    LOG.info("Created test {}", ret.getUrl());
    return ret;
  }

  private void updateLoadTest(LoadTest loadTest) throws IOException {
    LOG.info("Updating test {}", loadTest.getUrl());
    int prevEngines = loadTest.getEngineInstances();
    boolean prevSplitCsvs = loadTest.isSplitCsvs();
    loadTest.setEngineInstances(engines);
    loadTest.setSplitCsvs(splitCsvs);
    if (prevSplitCsvs != splitCsvs || prevEngines != engines) {
      apiClient.updateTest(loadTest);
    }
  }

  private void clearTestFiles(LoadTest loadTest) throws IOException {
    for (String f : apiClient.findTestFiles(loadTest.getTestId())) {
      apiClient.deleteTestFile(f, loadTest.getTestId());
    }
  }

  private void updateAppComponents(LoadTest loadTest) throws IOException {
    AppComponents components = apiClient.findTestAppComponents(loadTest.getTestId());
    if (components.updateWith(monitoredResources)) {
      apiClient.updateAppComponents(loadTest.getTestId(), components);
    }
  }

  private void uploadTestFiles(File jmxFile, HashTree tree, BuildTreeContext context,
      LoadTest loadTest) throws IOException, InterruptedException, TimeoutException {
    for (File f : assets) {
      context.processAssetFile(f.getPath());
    }
    for (File f : findDependencies(tree, context)) {
      context.processAssetFile(f.getPath());
    }
    context.processAssetFile(jmxFile.getPath());
    for (Map.Entry<String, File> asset : context.getAssetFiles().entrySet()) {
      FileInfo testFile = apiClient.uploadTestFile(asset.getValue(), asset.getKey(),
          loadTest.getTestId());
      awaitValidatedTestFile(testFile, loadTest.getTestId());
    }
  }

  private void awaitValidatedTestFile(FileInfo testFile, String testId)
      throws IOException, InterruptedException, TimeoutException {
    String fileName = testFile.getFileName();
    awaitStatus(testFile, () -> apiClient.findTestFile(fileName, testId),
        FileInfo::isPendingValidation, FileInfo::isSuccessValidation, VALIDATION_TIMEOUT,
        "test file '" + fileName + "' validation", "test plan",
        FileInfo::getValidationFailureDetails);
  }

  private String solveTestRunName() {
    return testRunName != null ? testRunName
        : "TestRun_" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date.from(Instant.now()));
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
    AzureTestStopper.handleTestEnd(testRun);
    return awaitVirtualUsers(testRun);
  }

  private TestRun awaitVirtualUsers(TestRun testRun) throws InterruptedException, IOException {
    Instant start = Instant.now();
    while (testRun.getVirtualUsers() == null && !hasTimedOut(TEST_END_TIMEOUT, start)) {
      Thread.sleep(STATUS_POLL_PERIOD.toMillis());
      testRun = apiClient.findTestRunById(testRun.getId());
    }
    if (!testRun.isSuccess()) {
      throw new IllegalStateException("Test " + testRun.getStatus().toLowerCase());
    }
    return testRun;
  }

}
