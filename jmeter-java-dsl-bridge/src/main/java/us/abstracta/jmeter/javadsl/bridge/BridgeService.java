package us.abstracta.jmeter.javadsl.bridge;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import us.abstracta.jmeter.javadsl.bridge.serialization.BridgedObjectDeserializer;
import us.abstracta.jmeter.javadsl.bridge.serialization.TestPlanStatsSerializer;
import us.abstracta.jmeter.javadsl.core.DslTestElement;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class BridgeService {

  public static void main(String[] args) throws Exception {
    String command = args[0];
    if ("run".equals(command)) {
      runTestPlan(args[1]);
    } else if ("saveAsJmx".equals(command)) {
      saveTestPlanAsJmx(args[1]);
    } else if ("showInGui".equals(command)) {
      showTestElementInGui();
    } else {
      throw new UnsupportedOperationException("Unrecognized command: " + command);
    }
    // This makes sure we close even if some awt thread is still running.
    System.exit(0);
  }

  private static void runTestPlan(String outputPath) throws Exception {
    TestPlanExecution exec = deserializeTestElement();
    TestPlanStats stats = exec.getTestPlan().runIn(exec.getEngine());
    try (FileWriter statsWriter = new FileWriter(outputPath)) {
      new TestPlanStatsSerializer().serializeToWriter(stats, statsWriter);
    }
  }

  private static <T> T deserializeTestElement() {
    return new BridgedObjectDeserializer().deserialize(new InputStreamReader(System.in));
  }

  private static void saveTestPlanAsJmx(String jmxPath)
      throws IOException {
    DslTestPlan testPlan = deserializeTestElement();
    testPlan.saveAsJmx(jmxPath);
  }

  private static void showTestElementInGui() {
    DslTestElement testElement = deserializeTestElement();
    testElement.showInGui();
  }

}
