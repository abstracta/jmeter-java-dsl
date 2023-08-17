package us.abstracta.jmeter.javadsl.core.listeners.autostop;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jorphan.documentation.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.engines.TestStopper;

/*
 implementing it as a listener instead of an assertion, so it always executes after all defined
 assertions, and is not possible by mistake to not count some failed assertion in errors count.
 Needs to implement Visualizer so that TestBeanGUI can find the correct GUI class
 */
public class AutoStopTestBean extends AbstractListenerElement implements TestBean,
    SampleListener, TestStateListener, NoThreadClone, Visualizer {

  private static final Logger LOG = LoggerFactory.getLogger(AutoStopTestBean.class);

  private Pattern regex;
  private List<AutoStopConditionElement> conditions;
  private TestStopper testStopper;
  private boolean stopped;

  public AutoStopTestBean() {
    this(null, new ArrayList<>(), null);
  }

  public AutoStopTestBean(Pattern regex, List<AutoStopConditionElement> conditions,
      TestStopper testStopper) {
    this.regex = regex;
    this.conditions = conditions;
    this.testStopper = testStopper;
  }

  public String getRegex() {
    return regex != null ? regex.toString() : null;
  }

  public void setRegex(String regex) {
    this.regex = regex != null && !regex.isEmpty() ? Pattern.compile(regex) : null;
  }

  public List<AutoStopConditionElement> getConditions() {
    return conditions;
  }

  public void setConditions(List<AutoStopConditionElement> conditions) {
    this.conditions = conditions;
  }

  public TestStopper getTestStopper() {
    return testStopper;
  }

  public void setTestStopper(TestStopper testStopper) {
    this.testStopper = testStopper;
  }

  @Override
  public void add(SampleResult sample) {
  }

  @Override
  public boolean isStats() {
    return false;
  }

  @Override
  public synchronized void sampleOccurred(SampleEvent e) {
    for (AutoStopConditionElement condition : conditions) {
      if (condition.getRegex() == null && regex != null
          && !regex.matcher(e.getResult().getSampleLabel()).matches()) {
        break;
      }
      /*
       Since stop is async, we check that result property is not already set to avoid overriding it
       and losing the original condition that triggered the stop
       */
      if (!stopped && condition.eval(e.getResult())) {
        stopped = true;
        String stopMessage = String.format("%s: %s%s", getName(),
            buildSamplesMatchingMessage(condition), condition);
        if (testStopper == null) {
          LOG.error("{} but no test stopper configured, so is not possible to stop test execution. "
                  + "This is probably caused by a JMeter DSL engine not supporting autoStop. "
                  + "Create an issue in the GitHub repository so we can implement proper support.",
              stopMessage);
          return;
        }
        testStopper.stop(stopMessage);
      }
    }
  }

  @Override
  public void sampleStarted(SampleEvent e) {
  }

  @Override
  public void sampleStopped(SampleEvent e) {
  }

  private String buildSamplesMatchingMessage(AutoStopConditionElement condition) {
    return condition.getRegex() == null && regex != null ? "samples matching '" + regex + "' " : "";
  }

  @Override
  public void testStarted() {
    conditions.forEach(AutoStopConditionElement::start);
  }

  @Override
  public void testStarted(String host) {
    testStarted();
  }

  @Override
  public void testEnded() {
  }

  @Override
  public void testEnded(String host) {
  }

  @VisibleForTesting
  public void setClock(Clock clock) {
    this.conditions.forEach(c -> c.setClock(clock));
  }

}
