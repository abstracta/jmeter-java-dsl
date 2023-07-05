package us.abstracta.jmeter.javadsl.bridge.serialization.constructs;

import java.util.Map;
import org.yaml.snakeyaml.nodes.Node;
import us.abstracta.jmeter.javadsl.bridge.TestPlanExecution;
import us.abstracta.jmeter.javadsl.bridge.serialization.BridgedObjectConstructor;
import us.abstracta.jmeter.javadsl.bridge.serialization.TestElementConstructorException;
import us.abstracta.jmeter.javadsl.core.DslJmeterEngine;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class TestPlanExecutionConstruct extends BaseBridgedObjectConstruct {

  private static final String TAG = "testPlanExecution";
  private final BridgedObjectConstructor constructor;

  public TestPlanExecutionConstruct(BridgedObjectConstructor constructor) {
    this.constructor = constructor;
  }

  @Override
  public Object construct(Node node) {
    Map<String, Node> properties = getNodeProperties(node, TAG);
    DslJmeterEngine engine = (DslJmeterEngine) constructor.constructObject(
        properties.remove("engine"));
    DslTestPlan testPlan = (DslTestPlan) constructor.constructObject(
        properties.remove("testPlan"));
    if (!properties.isEmpty()) {
      throw new TestElementConstructorException(TAG, node,
          "unknown properties " + String.join(",", properties.keySet()));
    }
    return new TestPlanExecution(engine, testPlan);
  }

}
