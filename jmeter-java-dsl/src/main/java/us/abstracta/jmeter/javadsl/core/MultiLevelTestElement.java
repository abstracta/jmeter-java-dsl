package us.abstracta.jmeter.javadsl.core;

import us.abstracta.jmeter.javadsl.core.DslSampler.SamplerChild;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;

/**
 * This is just a simple interface to avoid code duplication for test elements that apply at
 * different levels of a test plan (at test plan, thread group or as sampler child).
 */
public interface MultiLevelTestElement extends TestPlanChild, ThreadGroupChild, SamplerChild {

}
