package us.abstracta.jmeter.javadsl.http;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext.TreeContextEndListener;
import us.abstracta.jmeter.javadsl.core.configs.BaseConfigElement;

public abstract class AutoEnabledHttpConfigElement extends BaseConfigElement {

  protected boolean enabled = true;

  protected AutoEnabledHttpConfigElement(String name,
      Class<? extends JMeterGUIComponent> guiClass) {
    super(name, guiClass);
  }

  public void registerDependency(BuildTreeContext context) {
    registerEndListener(context, getOrCreateContextEntry(context));
  }

  public BuildContextEntry getOrCreateContextEntry(BuildTreeContext context) {
    return context.getOrCreateEntry(getClass().getSimpleName(), BuildContextEntry::new);
  }

  private static class BuildContextEntry {

    private Boolean enabled;
    private boolean registeredListener;
    private final Map<HashTree, BuildTreeContext> pendingResolution = new LinkedHashMap<>();
    private boolean hasDisabledChild;

  }

  private void registerEndListener(BuildTreeContext context, BuildContextEntry entry) {
    if (!entry.registeredListener) {
      entry.registeredListener = true;
      context.addEndListener(buildEndListener());
    }
  }

  public TreeContextEndListener buildEndListener() {
    return (context, tree) -> {
      BuildTreeContext parent = context.getParent();
      BuildContextEntry entry = getOrCreateContextEntry(context);
      if (parent == null) {
        endRootElement(context, tree, entry);
      } else if (entry.enabled == null) {
        endUnsolvedElement(context, tree, entry, parent);
      } else if (entry.enabled) {
        endEnabledElement(context, tree, entry);
      } else {
        endDisabledElement(parent);
      }
    };
  }

  private void endRootElement(BuildTreeContext context, HashTree tree,
      BuildContextEntry contextEntry) {
    if (contextEntry.hasDisabledChild) {
      addConfigToPendingResolutionChildren(contextEntry);
    } else if (contextEntry.enabled == null || contextEntry.enabled) {
      super.buildTreeUnder(tree, context);
    }
  }

  private void addConfigToPendingResolutionChildren(BuildContextEntry contextEntry) {
    contextEntry.pendingResolution.forEach(super::buildTreeUnder);
  }

  private void endUnsolvedElement(BuildTreeContext context, HashTree tree,
      BuildContextEntry entry, BuildTreeContext parent) {
    BuildContextEntry parentEntry = getOrCreateContextEntry(parent);
    if (entry.hasDisabledChild) {
      addConfigToPendingResolutionChildren(entry);
      parentEntry.hasDisabledChild = true;
    } else {
      parentEntry.pendingResolution.put(tree, context);
    }
    registerEndListener(parent, parentEntry);
  }

  private void endEnabledElement(BuildTreeContext context, HashTree tree, BuildContextEntry entry) {
    if (entry.hasDisabledChild) {
      addConfigToPendingResolutionChildren(entry);
    } else {
      super.buildTreeUnder(tree, context);
    }
  }

  private void endDisabledElement(BuildTreeContext parent) {
    BuildContextEntry parentEntry = getOrCreateContextEntry(parent);
    parentEntry.hasDisabledChild = true;
    registerEndListener(parent, parentEntry);
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    BuildTreeContext parentContext = context.getParent();
    BuildContextEntry entry = getOrCreateContextEntry(parentContext);
    entry.enabled = enabled;
    registerEndListener(parentContext, entry);
    return parent;
  }

  public abstract static class CodeBuilder<T extends TestElement> extends
      SingleTestElementCallBuilder<T> {

    public CodeBuilder(Class<T> testElementClass,
        List<Method> builderMethods) {
      super(testElementClass, builderMethods);
    }

    public void registerDependency(MethodCallContext context, MethodCall call) {
      if (!findConfigElementInSamplerScope(context)) {
        registerPendingDisableConfig(context, call);
      }
    }

    private boolean findConfigElementInSamplerScope(MethodCallContext samplerContext) {
      MethodCallContext parent = samplerContext.getParent();
      return samplerContext.getChildrenTree().list().stream()
          .anyMatch(e -> testElementClass.isInstance(e) && ((TestElement) e).isEnabled())
          || parent != null && findConfigElementInSamplerScope(parent);
    }

    private void registerPendingDisableConfig(MethodCallContext context, MethodCall call) {
      MethodCallContext parentContext = context.getParent();
      if (parentContext == null) {
        addDisabledChild(call);
        return;
      }
      CallContextEntry parentEntry = solveContextEntry(parentContext);
      parentEntry.pendingDisableConfigs.add(call);
      if (parentEntry.endListenerRegistered) {
        return;
      }
      parentEntry.endListenerRegistered = true;
      parentContext.addEndListener((ctx, listenerCall) -> {
        if (parentEntry.hasChildWithConfig) {
          parentEntry.pendingDisableConfigs.forEach(this::addDisabledChild);
        } else {
          registerPendingDisableConfig(ctx, listenerCall);
        }
      });
    }

    private void addDisabledChild(MethodCall call) {
      call.child(buildMethodCall().chain("disable"));
    }

    private CallContextEntry solveContextEntry(MethodCallContext context) {
      return context.computeEntryIfAbsent(getClass(), CallContextEntry::new);
    }

    @Override
    protected MethodCall buildMethodCall(T testElement, MethodCallContext context) {
      if (!testElement.isEnabled()) {
        return MethodCall.emptyCall();
      }
      if (findSamplerInConfigScope(context)) {
        MethodCallContext parent = context.getParent();
        while (parent != null) {
          solveContextEntry(parent).hasChildWithConfig = true;
          parent = parent.getParent();
        }
        return MethodCall.emptyCall();
      } else {
        return buildMethodCall();
      }
    }

    private boolean findSamplerInConfigScope(MethodCallContext context) {
      MethodCallContext parent = context.getParent();
      return parent.getTestElement() instanceof HTTPSamplerProxy || findSamplerInTree(
          parent.getChildrenTree());
    }

    private boolean findSamplerInTree(HashTree tree) {
      return tree != null && tree.list().stream()
          .anyMatch(c -> ((TestElement) c).isEnabled() && (c instanceof HTTPSamplerProxy
              || findSamplerInTree(tree.getTree(c))));
    }

    private static class CallContextEntry {

      private final List<MethodCall> pendingDisableConfigs = new ArrayList<>();
      private boolean endListenerRegistered;
      private boolean hasChildWithConfig;

    }

  }

}
