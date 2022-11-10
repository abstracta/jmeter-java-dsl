package us.abstracta.jmeter.javadsl.core.controllers;

import java.util.Collections;
import org.apache.jmeter.control.ModuleController;
import org.apache.jmeter.control.gui.ModuleControllerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;

public class DslModuleController extends BaseController<DslTestFragmentController> {

  protected DslModuleController() {
    super("Module Controller", ModuleControllerGui.class, Collections.emptyList());
  }

  @Override
  protected TestElement buildTestElement() {
    throw new UnsupportedOperationException();
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<ModuleController> {

    public CodeBuilder() {
      super(ModuleController.class, Collections.emptyList());
    }

    @Override
    protected MethodCall buildMethodCall(ModuleController testElement, MethodCallContext context) {
      CollectionProperty nodePath = (CollectionProperty) testElement.getProperty(
          "ModuleController.node_path");
      TestElement element = findElementInPath(nodePath, context);
      //TODO this should replace original method call by the fragment method call
      return new FragmentMethodCall(null, element, context);
    }

    private TestElement findElementInPath(CollectionProperty nodePath, MethodCallContext context) {
      PropertyIterator pathIterator = nodePath.iterator();
      // path always starts with test plan repeated twice. we just skip this part of the path.
      pathIterator.next();
      pathIterator.next();
      return findElementInPath(pathIterator, context.getRoot().getTestElement(),
          context.getRoot().getChildrenTree(), "");
    }

    private TestElement findElementInPath(PropertyIterator pathIterator, TestElement element,
        HashTree children, String path) {
      if (!pathIterator.hasNext()) {
        return element;
      }
      String nodeName = pathIterator.next().getStringValue();
      String childPath = path + "/" + nodeName;
      TestElement child = children.list().stream()
          .map(e -> (TestElement) e)
          .filter(e -> nodeName.equals(e.getName()))
          .findAny()
          .orElseThrow(
              () -> new IllegalArgumentException("Could not find element at path " + childPath));
      return findElementInPath(pathIterator, child, children.getTree(child), childPath);
    }

  }

}
