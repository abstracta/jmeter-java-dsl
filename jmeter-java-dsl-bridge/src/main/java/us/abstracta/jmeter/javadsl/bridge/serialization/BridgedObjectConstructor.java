package us.abstracta.jmeter.javadsl.bridge.serialization;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.bridge.serialization.constructs.BridgedObjectConstruct;
import us.abstracta.jmeter.javadsl.bridge.serialization.constructs.EngineConstruct;
import us.abstracta.jmeter.javadsl.bridge.serialization.constructs.HttpHeadersConstruct;
import us.abstracta.jmeter.javadsl.bridge.serialization.constructs.TestPlanExecutionConstruct;

public class BridgedObjectConstructor extends Constructor {

  public BridgedObjectConstructor() {
    super(new LoaderOptions());
    Map<String, List<Method>> builderMethods = Stream.of(JmeterDsl.class.getDeclaredMethods())
        .filter(m -> Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers()))
        .collect(Collectors.groupingBy(Method::getName));
    this.yamlConstructors.putAll(builderMethods.entrySet().stream()
        .collect(Collectors.toMap(e -> new Tag("!" + e.getKey()),
            e -> new BridgedObjectConstruct(this, e.getKey(),
                e.getValue().stream()
                    .map(StaticMethodBuilderMethod::new)
                    .collect(Collectors.toList())))));
    this.yamlConstructors.put(new Tag("!httpHeaders"), new HttpHeadersConstruct());
    this.yamlConstructors.put(new Tag("!testPlanExecution"),
        new TestPlanExecutionConstruct(this));
    this.yamlConstructors.put(null, new EngineConstruct(this));
  }

  @Override
  public Object constructObject(Node node) {
    return super.constructObject(node);
  }

  private static class StaticMethodBuilderMethod implements BuilderMethod {

    private final Method method;

    private StaticMethodBuilderMethod(Method method) {
      this.method = method;
    }

    @Override
    public Object invoke(Object... args) throws ReflectiveOperationException {
      return method.invoke(null, args);
    }

    @Override
    public Parameter[] getParameters() {
      return method.getParameters();
    }

  }

}
