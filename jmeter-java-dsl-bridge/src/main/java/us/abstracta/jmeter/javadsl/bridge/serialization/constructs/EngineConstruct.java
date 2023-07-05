package us.abstracta.jmeter.javadsl.bridge.serialization.constructs;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.nodes.Node;
import us.abstracta.jmeter.javadsl.bridge.serialization.BridgedObjectConstructor;
import us.abstracta.jmeter.javadsl.bridge.serialization.BuilderMethod;
import us.abstracta.jmeter.javadsl.bridge.serialization.TestElementConstructorException;
import us.abstracta.jmeter.javadsl.core.DslJmeterEngine;
import us.abstracta.jmeter.javadsl.core.engines.EmbeddedJmeterEngine;

public class EngineConstruct extends AbstractConstruct {

  private final BridgedObjectConstructor constructor;

  public EngineConstruct(BridgedObjectConstructor constructor) {
    this.constructor = constructor;
  }

  @Override
  public Object construct(Node node) {
    Class<?> engineClass = findEngineClass(node);
    return new BridgedObjectConstruct(constructor, node.getTag().getValue().substring(1),
        Arrays.stream(engineClass.getConstructors())
            .map(ConstructorBuilderMethod::new)
            .collect(Collectors.toList()))
        .construct(node);
  }

  private Class<? extends DslJmeterEngine> findEngineClass(Node node) {
    String tag = node.getTag().getValue().substring(1);
    if ("embeddedJmeterEngine".equals(tag)) {
      return EmbeddedJmeterEngine.class;
    } else {
      try {
        return (Class<? extends DslJmeterEngine>) Class.forName(
            "us.abstracta.jmeter.javadsl." + tag);
      } catch (ClassNotFoundException e) {
        throw new TestElementConstructorException(tag, node,
            "could not find the engine class. Check that is included in classpath.");
      }
    }
  }

  private static class ConstructorBuilderMethod implements BuilderMethod {

    private final Constructor<?> method;

    private ConstructorBuilderMethod(Constructor<?> method) {
      this.method = method;
    }

    @Override
    public Object invoke(Object... args) throws ReflectiveOperationException {
      return method.newInstance(args);
    }

    @Override
    public Parameter[] getParameters() {
      return method.getParameters();
    }

  }

}
