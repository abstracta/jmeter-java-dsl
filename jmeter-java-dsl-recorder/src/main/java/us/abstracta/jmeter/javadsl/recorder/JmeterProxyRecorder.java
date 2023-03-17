package us.abstracta.jmeter.javadsl.recorder;

import com.blazemeter.jmeter.correlation.CorrelationProxyControl;
import com.blazemeter.jmeter.correlation.CorrelationProxyControlGui;
import com.blazemeter.jmeter.correlation.core.CorrelationRule;
import com.blazemeter.jmeter.correlation.core.RulesGroup;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.protocol.http.control.gui.RecordController;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.controllers.BaseController;
import us.abstracta.jmeter.javadsl.core.testelements.TestElementContainer;

/**
 * Encapsulates logic for recording and allow excluding headers.
 */
/*
We implemented custom header exclusion since JMeter builtin system property proxy.headers.remove
is statically solved, which means that is not possible to do multiple recordings in same jvm with
different header exclusion. Additionally, this class allows to specify regex for headers, instead
of having to enumerate each header to be removed.
 */
//Part of this logic is based on https://github.com/Blazemeter/CorrelationRecorder/blob/v1.3/src/test/java/com/blazemeter/jmeter/correlation/regression/Recording.java
public class JmeterProxyRecorder extends CorrelationProxyControl {

  private static final Duration RECORDING_POLL_PERIOD = Duration.ofSeconds(3);
  private static final Duration RECORDING_STOP_TIMEOUT = Duration.ofSeconds(30);

  private final List<Pattern> headerExcludes = new ArrayList<>();
  private File logsDirectory;
  private JMeterTreeModel treeModel;

  public JmeterProxyRecorder() {
    setDefaultEncoding(StandardCharsets.UTF_8.name());
  }

  public JmeterProxyRecorder port(int port) {
    setPort(port);
    return this;
  }

  public JmeterProxyRecorder logsDirectory(File logsDirectory) {
    this.logsDirectory = logsDirectory;
    return this;
  }

  public JmeterProxyRecorder logFilteredRequests(boolean enabled) {
    setNotifyChildSamplerListenerOfFilteredSamplers(enabled);
    return this;
  }

  public JmeterProxyRecorder headerExcludes(List<Pattern> regexes) {
    this.headerExcludes.addAll(regexes);
    return this;
  }

  public JmeterProxyRecorder urlIncludes(List<Pattern> regexes) {
    regexes.forEach(r -> this.addIncludedPattern(r.toString()));
    return this;
  }

  public JmeterProxyRecorder urlExcludes(List<Pattern> regexes) {
    regexes.forEach(r -> this.addExcludedPattern(r.toString()));
    return this;
  }

  public JmeterProxyRecorder correlationRules(List<CorrelationRule> correlations) {
    setCorrelationGroups(Collections.singletonList(
        new RulesGroup.Builder()
            .withId("default")
            .withRules(correlations)
            .build()));
    return this;
  }

  public void startRecording() throws IOException {
    treeModel = buildTreeModel();
    setNonGuiTreeModel(treeModel);
    startProxy();
  }

  private JMeterTreeModel buildTreeModel() {
    try {
      JMeterTreeModel treeModel = new JMeterTreeModel(new TestPlan());
      JMeterTreeNode root = (JMeterTreeNode) treeModel.getRoot();
      treeModel.addSubTree(buildTree(), root);
      return treeModel;
    } catch (IllegalUserActionException e) {
      throw new RuntimeException(e);
    }
  }

  private HashTree buildTree() {
    HashTree rootTree = new ListedHashTree();
    List<TestPlanChild> recorderChildren = logsDirectory != null
        ? Collections.singletonList(JmeterDsl.jtlWriter(logsDirectory.toString())
        .withAllFields())
        : Collections.emptyList();
    JmeterDsl.testPlan(
        JmeterDsl.threadGroup(1, 1,
            JmeterDsl.httpCookies(),
            JmeterDsl.httpCache(),
            new DslRecordingController()
        ),
        new DslRecorder(this, recorderChildren)
    ).buildTreeUnder(rootTree, new BuildTreeContext());
    return rootTree;
  }

  private static class DslRecordingController extends BaseController<DslRecordingController> {

    protected DslRecordingController() {
      super("Recording Controller", RecordController.class, Collections.emptyList());
    }

    @Override
    protected TestElement buildTestElement() {
      return new RecordingController();
    }

  }

  private static class DslRecorder extends
      TestElementContainer<DslRecorder, TestPlanChild> implements TestPlanChild {

    private final CorrelationProxyControl proxy;

    protected DslRecorder(CorrelationProxyControl proxy, List<TestPlanChild> children) {
      super("Recorder", CorrelationProxyControlGui.class, children);
      this.proxy = proxy;
    }

    @Override
    protected TestElement buildTestElement() {
      return proxy;
    }

  }

  @Override
  public synchronized void deliverSampler(HTTPSamplerBase sampler, TestElement[] testElements,
      SampleResult result) {
    List<TestElement> children = new ArrayList<>(Arrays.asList(testElements));
    removeExcludedHeaders(children);
    super.deliverSampler(sampler, children.toArray(new TestElement[]{}), result);
  }

  private void removeExcludedHeaders(List<TestElement> children) {
    Optional<TestElement> headerManager = children.stream()
        .filter(e -> e instanceof HeaderManager)
        .findAny();
    if (!headerManager.isPresent()) {
      return;
    }
    CollectionProperty headers = ((HeaderManager) headerManager.get()).getHeaders();
    List<JMeterProperty> filteredHeaders = StreamSupport.stream(headers.spliterator(), false)
        .filter(h -> headerExcludes.stream().noneMatch(p -> p.matcher(h.getName()).matches()))
        .collect(Collectors.toList());
    if (filteredHeaders.isEmpty()) {
      children.remove(headerManager.get());
    } else {
      headers.clear();
      filteredHeaders.forEach(headers::addProperty);
    }
  }

  public void stopRecording() throws InterruptedException, TimeoutException {
    stopProxy();
    awaitRecordingEnd();
  }

  public void saveRecordingTo(File jmx) throws IOException {
    SaveService.saveTree(convertSubTree(treeModel.getTestPlan()),
        Files.newOutputStream(jmx.toPath()));
  }

  private void awaitRecordingEnd() throws TimeoutException, InterruptedException {
    Instant start = Instant.now();
    boolean hasPendingRequests;
    synchronized (this) {
      hasPendingRequests = !this.getPendingProxies().isEmpty();
    }
    boolean hadPendingRequests;
    boolean timeout;
    do {
      hadPendingRequests = hasPendingRequests;
      synchronized (this) {
        this.wait(RECORDING_POLL_PERIOD.toMillis());
        hasPendingRequests = !this.getPendingProxies().isEmpty();
      }
      timeout = Duration.between(start, Instant.now()).compareTo(RECORDING_STOP_TIMEOUT) >= 0;
    } while ((hasPendingRequests || hadPendingRequests) && !timeout);
    if (timeout) {
      throw new TimeoutException(
          "Timeout waiting for recording to stop adding items to test plan after "
              + RECORDING_STOP_TIMEOUT + ".");
    }
  }

  private HashTree convertSubTree(HashTree tree) {
    for (Object o : new LinkedList<>(tree.list())) {
      JMeterTreeNode item = (JMeterTreeNode) o;
      convertSubTree(tree.getTree(item));
      tree.replaceKey(item, item.getTestElement());
    }
    return tree;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<JmeterProxyRecorder> {

    protected CodeBuilder() {
      super(JmeterProxyRecorder.class, Collections.emptyList());
    }

    @Override
    protected MethodCall buildMethodCall(JmeterProxyRecorder correlationProxyControl,
        MethodCallContext methodCallContext) {
      return MethodCall.emptyCall();
    }

  }

}
