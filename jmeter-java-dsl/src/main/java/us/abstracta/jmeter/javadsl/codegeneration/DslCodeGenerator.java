package us.abstracta.jmeter.javadsl.codegeneration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.core.DslTestElement;
import us.abstracta.jmeter.javadsl.core.controllers.DslRecordingController;
import us.abstracta.jmeter.javadsl.core.engines.JmeterEnvironment;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;

/**
 * Generates DSL code from JMX files.
 * <p>
 * Take into account that code generation is a continuous improving process, so please report any
 * unexpected or missing conversions as issues in repository, so we keep improving it.
 * <p>
 * Check {@link MethodCallBuilder} for instructions on how to implement DSL code generation for new
 * DSL test elements.
 *
 * @see MethodCallBuilder
 * @since 0.45
 */
public class DslCodeGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(DslCodeGenerator.class);

  private final List<MethodCallBuilder> builders = new ArrayList<>();

  public DslCodeGenerator() {
    builders.addAll(findCallBuilders(JmeterDsl.class));
    builders.add(new DslRecordingController.CodeBuilder());
    sortBuilders();
  }

  private void sortBuilders() {
    builders.sort(Comparator.comparing(MethodCallBuilder::order));
  }

  /**
   * Generates DSL code from JMX file.
   *
   * @param file is the JMX file from which DSL code will be generated.
   * @return the generated DSL code.
   * @throws IOException when there is some problem reading the file.
   */
  public String generateCodeFromJmx(File file) throws IOException {
    JmeterEnvironment env = new JmeterEnvironment();
    HashTree tree = env.loadTree(new File(file.getPath()));
    TestElement testPlanElem = (TestElement) tree.getArray()[0];
    return new MethodCallContext(testPlanElem, tree.getTree(testPlanElem), this)
        .buildMethodCall()
        .buildCode();
  }

  /**
   * Allows registering DSL classes containing builder methods, which can be used to generate DSL
   * code for.
   * <p>
   * This method allows you to register DSL classes from none core modules or your own custom DSL
   * classes. This is the way DslGenerators can discover new DSL test elements, and their associated
   * MethodCallBuilder instances, to generate code for.
   *
   * @param dslClasses are the classes containing builder methods.
   * @return the DslCodeGenerator instance for further configuration or usage.
   */
  public DslCodeGenerator addBuildersFrom(Class<?>... dslClasses) {
    builders.addAll(findCallBuilders(dslClasses));
    sortBuilders();
    return this;
  }

  /**
   * Allows registering MethodCallBuilders that are not associated to a DSL builder method.
   * <p>
   * This is helpful when some element has no DSL builder method counterpart, but still there is a
   * way to convert the element (eg: ignoring it all together, only converting children, etc).
   *
   * @param builders list of MethodCallBuilders to register into the generator.
   * @return the DslCodeGenerator instance for further configuration or usage.
   * @since 0.50
   */
  public DslCodeGenerator addBuilders(MethodCallBuilder... builders) {
    this.builders.addAll(Arrays.asList(builders));
    sortBuilders();
    return this;
  }

  private List<MethodCallBuilder> findCallBuilders(Class<?>... dslClasses) {
    Map<Class<? extends DslTestElement>, List<Method>> builderMethods = findBuilderMethods(
        dslClasses);
    return builderMethods.entrySet().stream()
        .map(e -> buildCallBuilder(e.getKey(), e.getValue()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private Map<Class<? extends DslTestElement>, List<Method>> findBuilderMethods(
      Class<?>... dslClasses) {
    return Arrays.stream(dslClasses)
        .flatMap(c -> Arrays.stream(c.getDeclaredMethods())
            .filter(m -> Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())
                && BaseTestElement.class.isAssignableFrom(m.getReturnType())))
        .collect(Collectors.groupingBy(m -> (Class<? extends DslTestElement>) m.getReturnType()));
  }

  private MethodCallBuilder buildCallBuilder(
      Class<? extends DslTestElement> testElementClass, List<Method> builderMethods) {
    String builderClass = testElementClass.getName() + "$CodeBuilder";
    try {
      return (MethodCallBuilder) Class.forName(builderClass)
          .getConstructor(List.class)
          .newInstance(builderMethods);
    } catch (ClassNotFoundException e) {
      LOG.debug("No code builder associated to {}", testElementClass);
      return null;
    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
             | IllegalAccessException e) {
      throw new RuntimeException("Problem instantiating builder for " + builderClass
          + ". Check builder constructor with a list of methods and registry as parameters.", e);
    }
  }

  protected List<MethodCallBuilder> getBuilders() {
    return builders;
  }

  protected <T extends MethodCallBuilder> T findBuilder(Class<T> builderClass) {
    return builders.stream()
        .filter(builderClass::isInstance)
        .map(builderClass::cast)
        .findAny()
        .orElse(null);
  }

}
