package us.abstracta.jmeter.javadsl.core.controllers;

import com.blazemeter.jmeter.control.WeightedSwitchController;
import com.blazemeter.jmeter.control.WeightedSwitchControllerGui;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DslWeightedSwitchController extends BaseController {

  private final PowerTableModel model;
  private final WeightedSwitchController wsc;

  public DslWeightedSwitchController() {
    super("Weighted Switch Controller",
        WeightedSwitchControllerGui.class,
        new ArrayList<>()
    );

    model = new PowerTableModel(
        new String[]{"Name", "Weight", "Enabled"},
        new Class[]{String.class, String.class, String.class}
    );

    wsc = new WeightedSwitchController();
  }


  private void addToModel(Long probability, BaseTestElement element, Boolean enabled) throws IllegalArgumentException {

    List<Long> weights = this.model.getData().getColumnAsObjectArray("Weight")
        .stream()
        .map(
            x -> Long.parseLong(x.toString())
        ).collect(
            Collectors.toList()
        );

    if (weights.stream().reduce(0L, Long::sum) + probability <= 100) {
      this.model.addRow(new String[]{element.getName(), probability.toString(), enabled.toString()});
      this.children.add((ThreadGroupChild) element);
    } else {
      throw new IllegalArgumentException("Total sum of probabilities should be less or equal 100");
    }

  }

  public DslWeightedSwitchController add(Long probability, BaseTestElement element) {
    this.addToModel(probability, element, true);
    return this;
  }

  public DslWeightedSwitchController add(Long probability, BaseTestElement element, Boolean enabled) {
    this.addToModel(probability, element, enabled);
    return this;
  }

  @Override
  public TestElement buildTestElement() {
    wsc.setData(model);
    HashTree ret = new HashTree();
    BuildTreeContext ctx = new BuildTreeContext();
    children.forEach(c -> ctx.buildChild(c, ret));
    for (Object te : ret.list()) {
      wsc.addTestElement((TestElement) te);
    }
    return wsc;
  }

}
