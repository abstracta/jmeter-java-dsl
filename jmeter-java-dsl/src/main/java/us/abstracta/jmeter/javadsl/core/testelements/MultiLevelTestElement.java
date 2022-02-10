package us.abstracta.jmeter.javadsl.core.testelements;

import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.samplers.BaseSampler.SamplerChild;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * This is just a simple interface to avoid code duplication for test elements that apply at
 * different levels of a test plan (at test plan, thread group or as sampler child).
 *
 * @since 0.11
 */
public interface MultiLevelTestElement extends TestPlanChild, ThreadGroupChild, SamplerChild {

}
