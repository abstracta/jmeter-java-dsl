package us.abstracta.jmeter.javadsl.core.controllers;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.jmeter.control.IncludeController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/*
 Since include controller will not be provided in dsl nor be included in saved or executed test plan
 there is no need to extend from BaseTestElement, and only CodeBuilder is provided for proper jmx
 conversion.
 */
public class DslIncludeController {

  public static class CodeBuilder extends SingleTestElementCallBuilder<IncludeController> {

    private static final Logger LOG = LoggerFactory.getLogger(CodeBuilder.class);

    public CodeBuilder() {
      super(IncludeController.class, Collections.emptyList());
    }

    @Override
    protected MethodCall buildMethodCall(IncludeController testElement, MethodCallContext context) {
      File includeFile = new File(testElement.getIncludePath().trim());
      if (!includeFile.exists()) {
        LOG.warn("Included file ({}) could not be found. Make sure is properly located taking "
            + "into consideration current working directory.", includeFile);
        return null;
      }
      TestElement includeByFile = computeIncludeByFile(includeFile, testElement, context);
      if (includeByFile != testElement) {
        return new FragmentMethodCall(includeByFile, null, context);
      }
      MethodCall fragment = buildMethodDefinitionBody(testElement, context);
      testElement.setName(solveElementName(includeFile));
      return new FragmentMethodCall(testElement, fragment, context);
    }

    private TestElement computeIncludeByFile(File includeFile, IncludeController testElement,
        MethodCallContext context) {
      Map<String, TestElement> includedFragments = context.getRoot()
          .computeEntryIfAbsent(getClass(), HashMap::new);
      return includedFragments.computeIfAbsent(includeFile.toString(), k -> testElement);
    }

    private static MethodCall buildMethodDefinitionBody(IncludeController testElement,
        MethodCallContext context) {
      testElement.resolveReplacementSubTree(null);
      HashTree children = testElement.getReplacementSubTree();
      MethodCall fragment = buildFragmentMethodCall();
      children.list().stream()
          .map(e -> context.child((TestElement) e, children.getTree(e)).buildMethodCall())
          .forEach(fragment::child);
      return fragment;
    }

    private static MethodCall buildFragmentMethodCall() {
      try {
        return MethodCall.fromBuilderMethod(DslTestFragmentController.class.getMethod("fragment",
            ThreadGroupChild[].class), new ChildrenParam<>(ThreadGroupChild[].class));
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    private String solveElementName(File includeFile) {
      String ret = includeFile.getName().toLowerCase();
      String jmxExtension = ".jmx";
      return ret.endsWith(jmxExtension)
          ? ret.substring(0, ret.length() - jmxExtension.length())
          : ret;
    }

  }

}
