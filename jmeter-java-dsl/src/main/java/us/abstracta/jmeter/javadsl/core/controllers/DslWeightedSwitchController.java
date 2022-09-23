package us.abstracta.jmeter.javadsl.core.controllers;

import com.blazemeter.jmeter.control.WeightedSwitchController;
import com.blazemeter.jmeter.control.WeightedSwitchControllerGui;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kg.apc.jmeter.JMeterPluginsUtils;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.LongParam;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.samplers.DslSampler;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Selects a child in each iteration according to specified relative weights.
 * <p>
 * Internally this uses <a
 * href="https://github.com/Blazemeter/jmeter-bzm-plugins/blob/master/wsc/WeightedSwitchController.md">
 * BlazeMeter Weighted Switch Controller plugin</a>.
 * <p>
 * This controller is handy when you want part of the test plan to act in a probabilistic manner
 * switching between different alternatives.
 * <p>
 * If you configure for example children weights with (50, child1), (100, child2), (50, child3) and
 * 10 iterations, then you will get this execution: child2, child1, child3, child2, child2, child1,
 * child3, child2, child2, child1.
 *
 * @since 0.53
 */
public class DslWeightedSwitchController extends BaseController<DslWeightedSwitchController> {

  public static final long DEFAULT_WEIGHT = 100;

  public DslWeightedSwitchController() {
    super("Weighted Switch Controller", WeightedSwitchControllerGui.class, new ArrayList<>());
  }

  /* we provide two separate methods instead of one just for ThreadGroupChild to only allow users
  specifying weights for elements that make sense */

  /**
   * Adds a child to the controller with a configured weight for selecting it in iterations.
   *
   * @param weight is the weight to assign to this particular element for execution in iterations.
   *               Keep in mind that if you use {@link #children(ThreadGroupChild...)} to add
   *               samplers or controllers, their default assigned weight will be 100.
   * @param child  is the element to add as controller child that will be selected for execution
   *               during iterations according to given weight.
   * @return the controller for further configuration and usage.
   */
  public DslWeightedSwitchController child(long weight, DslController child) {
    return addWeightedChild(weight, child);
  }

  /**
   * Adds a child to the controller with a configured weight for selecting it in iterations.
   *
   * @param weight is the weight to assign to this particular element for execution in iterations.
   *               Keep in mind that if you use {@link #children(ThreadGroupChild...)} to add
   *               samplers or controllers, their default assigned weight will be 100.
   * @param child  is the element to add as controller child that will be selected for execution
   *               during iterations according to given weight.
   * @return the controller for further configuration and usage.
   */
  public DslWeightedSwitchController child(long weight, DslSampler child) {
    return addWeightedChild(weight, child);
  }

  private DslWeightedSwitchController addWeightedChild(long weight, ThreadGroupChild child) {
    children.add(new WeightedChild(weight, child));
    return this;
  }

  private static class WeightedChild implements ThreadGroupChild {

    private final long weight;
    private final ThreadGroupChild element;

    private WeightedChild(long weight,
        ThreadGroupChild element) {
      this.weight = weight;
      this.element = element;
    }

    @Override
    public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
      return element.buildTreeUnder(parent, context);
    }

    @Override
    public void showInGui() {
      element.showInGui();
    }

  }

  /**
   * Allows specifying children test elements which don't have an explicit weight associated.
   * <p>
   * This is method should mainly be used to add elements which weight does not affect like
   * listeners, timers, assertions, pre- and post-processors and config elements.
   * <p>
   * <b>Note:</b> If a sampler or controller is added with this method, it's weight will default to
   * 100.
   *
   * @param children list of test elements to add as children of this controller.
   * @return the controller for further configuration and usage.
   */
  public DslWeightedSwitchController children(ThreadGroupChild... children) {
    return super.children(children);
  }

  @Override
  public TestElement buildTestElement() {
    return new WeightedSwitchController();
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    WeightedSwitchController controller = (WeightedSwitchController) buildConfiguredTestElement();
    HashTree ret = parent.add(controller);
    PowerTableModel model = buildTableModel();
    for (ThreadGroupChild child : children) {
      HashTree childTree = context.buildChild(child, ret);
      if (child instanceof WeightedChild) {
        addWeightedChildToModel(getChildName(ret, childTree), ((WeightedChild) child).weight,
            model);
      } else if (child instanceof DslSampler || child instanceof DslController) {
        addWeightedChildToModel(getChildName(ret, childTree), DEFAULT_WEIGHT, model);
      }
    }
    controller.setData(model);
    return ret;
  }

  private String getChildName(HashTree tree, HashTree child) {
    return tree.list().stream()
        .filter(t -> tree.getTree(t) == child)
        .map(t -> ((TestElement) t).getName())
        .findAny()
        .orElseThrow(IllegalStateException::new);
  }

  private static PowerTableModel buildTableModel() {
    return new PowerTableModel(
        new String[]{"Name", WeightedSwitchController.WEIGHTS, "Enabled"},
        new Class[]{String.class, String.class, String.class}
    );
  }

  private void addWeightedChildToModel(String name, long weight, PowerTableModel model) {
    model.addRow(new String[]{name, String.valueOf(weight), "true"});
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<WeightedSwitchController> {

    public CodeBuilder(List<Method> builderMethods) {
      super(WeightedSwitchController.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(WeightedSwitchController testElement,
        MethodCallContext context) {
      MethodCall ret = buildMethodCall();
      Map<String, Long> weights = extractSamplersWeights(testElement);
      chainChildren(ret, context, weights);
      return ret;
    }

    private void chainChildren(MethodCall ret, MethodCallContext context,
        Map<String, Long> weights) {
      List<MethodCall> childrenCalls = new ArrayList<>();
      List<Object> children = new ArrayList<>(context.getChildrenTree().list());
      for (Object child : children) {
        String childName = ((TestElement) child).getName();
        MethodCall childCall = context.removeChild(t -> t == child).buildMethodCall();
        if (weights.containsKey(childName)) {
          appendChildrenCalls(childrenCalls, ret);
          childrenCalls = new ArrayList<>();
          ret.chain("child", new LongParam(weights.get(childName)), new ChildParam(childCall));
        } else {
          childrenCalls.add(childCall);
        }
      }
      appendChildrenCalls(childrenCalls, ret);
    }

    private void appendChildrenCalls(List<MethodCall> children, MethodCall ret) {
      if (!children.isEmpty()) {
        ret.chain("children", new ChildrenParam<>(ThreadGroupChild[].class, children));
      }
    }

    public Map<String, Long> extractSamplersWeights(WeightedSwitchController testElement) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      CollectionProperty weightsProp = (CollectionProperty) paramBuilder.prop(
          WeightedSwitchController.WEIGHTS);
      PowerTableModel tableModel = buildTableModel();
      JMeterPluginsUtils.collectionPropertyToTableModelRows(weightsProp, tableModel);
      Map<String, Long> ret = new HashMap<>();
      for (int i = 0; i < tableModel.getRowCount(); i++) {
        Object[] rowData = tableModel.getRowData(i);
        long weight = Long.parseLong(stringProp(rowData[1]));
        if (weight != DEFAULT_WEIGHT) {
          ret.put(stringProp(rowData[0]), weight);
        }
      }
      return ret;
    }

    private static String stringProp(Object val) {
      return ((JMeterProperty) val).getStringValue();
    }

  }

  private static class ChildParam extends MethodParam {

    private final MethodCall child;

    protected ChildParam(MethodCall child) {
      super(DslSampler.class.isAssignableFrom(child.getReturnType()) ? DslSampler.class
          : DslController.class, null);
      this.child = child;
    }

    @Override
    public Set<String> getStaticImports() {
      return child.getStaticImports();
    }

    @Override
    public Set<String> getImports() {
      return child.getImports();
    }

    @Override
    public String buildCode(String indent) {
      return child.buildCode(indent);
    }

  }

}
