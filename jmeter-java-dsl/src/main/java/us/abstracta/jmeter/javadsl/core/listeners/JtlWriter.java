package us.abstracta.jmeter.javadsl.core.listeners;

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.SimpleDataWriter;
import us.abstracta.jmeter.javadsl.core.BaseTestElement;
import us.abstracta.jmeter.javadsl.core.DslSampler.SamplerChild;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;

/**
 * Allows to generate a result log file (JTL) with data for each sample for a test plan, thread
 * group or sampler, depending at what level of test plan is added.
 *
 * If jtlWriter is added at testPlan level it will log information about all samples in the test
 * plan, if added at thread group level it will only log samples for samplers contained within it,
 * if added as a sampler child, then only that sampler samples will be logged.
 *
 * By default this writer will use JMeter default JTL format (csv with label, elapsed time, status
 * code, etc). In the future additional methods may be added to configure different fields to
 * include, or use different format.
 *
 * See <a href="http://jmeter.apache.org/usermanual/listeners.html">JMeter listeners doc</a> for
 * more details on JTL format and settings.
 */
public class JtlWriter extends BaseTestElement implements TestPlanChild, ThreadGroupChild,
    SamplerChild {

  private final String jtlFile;

  public JtlWriter(String jtlFile) {
    super("Simple Data Writer", SimpleDataWriter.class);
    this.jtlFile = jtlFile;
  }

  @Override
  public TestElement buildTestElement() {
    ResultCollector logger = new ResultCollector();
    logger.setFilename(jtlFile);
    return logger;
  }

}
