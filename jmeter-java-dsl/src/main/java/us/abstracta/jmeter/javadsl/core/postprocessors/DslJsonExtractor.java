package us.abstracta.jmeter.javadsl.core.postprocessors;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.jmeter.extractor.json.jmespath.JMESPathExtractor;
import org.apache.jmeter.extractor.json.jmespath.gui.JMESPathExtractorGui;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.extractor.json.jsonpath.gui.JSONPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.FixedParam;
import us.abstracta.jmeter.javadsl.core.testelements.DslScopedTestElement;

/**
 * Allows extracting part of a JSON response using JMESPath or JSONPath to store into a variable.
 * <p>
 * By default, the extractor is configured to use JMESPath and to extract from the main sample (does
 * not include sub samples) response body the first match of the JMESPath. If no match is found,
 * then variable will be assigned empty string.
 *
 * @since 0.28
 */
public class DslJsonExtractor extends DslVariableExtractor<DslJsonExtractor> {

  protected String query;
  protected JsonQueryLanguage queryLanguage = JsonQueryLanguage.JMES_PATH;

  public DslJsonExtractor(String varName, String query) {
    super(null, null, varName);
    this.query = query;
  }

  /**
   * Sets the match number to be extracted.
   * <p>
   * For example, if a response looks like this: <code>[{"name":"test"},{"name":"tester"}]</code>
   * and you use {@code [].name} as JMESPath, first match (1) would extract {@code test} and second
   * match (2) would extract {@code tester}.
   * <p>
   * When not specified, the first match will be used. When 0 is specified, a random match will be
   * used. When negative, all the matches are extracted to variables with name {@code
   * <variableName>_<matchNumber>}, the number of matches is stored in {@code
   * <variableName>_matchNr}, and default value is assigned to {@code <variableName>}.
   *
   * @param matchNumber specifies the match number to use.
   * @return the extractor for further configuration and usage.
   */
  public DslJsonExtractor matchNumber(int matchNumber) {
    this.matchNumber = matchNumber;
    return this;
  }

  /**
   * Sets the default value to be stored in the JMeter variable when no match is found.
   * <p>
   * When match number is negative then the value is always assigned to the variable name.
   * <p>
   * A common pattern is to specify this value to a known value (e.g.:
   * &lt;VAR&gt;_EXTRACTION_FAILURE) and then add some assertion on the variable to mark request as
   * failure when the match doesn't work.
   * <p>
   * When not specified, then the variable will be assigned to empty string.
   *
   * @param defaultValue specifies the default value to be used.
   * @return the extractor for further configuration and usage.
   */
  public DslJsonExtractor defaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  /**
   * Allows selecting the query language to use for extracting values from a given JSON.
   *
   * @param queryLanguage specifies the query language to use to extracting values. When no value is
   *                      specified, JMESPath is used by default.
   * @return the extractor for further configuration and usage.
   * @see JsonQueryLanguage
   * @since 1.12
   */
  public DslJsonExtractor queryLanguage(JsonQueryLanguage queryLanguage) {
    this.queryLanguage = queryLanguage;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    return queryLanguage == JsonQueryLanguage.JSON_PATH ? buildJsonPathExtractor()
        : buildJmesPathExtractor();
  }

  private TestElement buildJsonPathExtractor() {
    name = "JSON Extractor";
    guiClass = JSONPostProcessorGui.class;
    JSONPostProcessor ret = new JSONPostProcessor();
    setScopeTo(ret);
    ret.setRefNames(varName);
    ret.setJsonPathExpressions(query);
    ret.setMatchNumbers(String.valueOf(matchNumber));
    ret.setDefaultValues(defaultValue != null ? defaultValue : "");
    return ret;
  }

  private TestElement buildJmesPathExtractor() {
    name = "JSON JMESPath Extractor";
    guiClass = JMESPathExtractorGui.class;
    JMESPathExtractor ret = new JMESPathExtractor();
    setScopeTo(ret);
    ret.setRefName(varName);
    ret.setJmesPathExpression(query);
    ret.setMatchNumber(String.valueOf(matchNumber));
    ret.setDefaultValue(defaultValue);
    return ret;
  }

  public static class CodeBuilder extends MethodCallBuilder {

    public CodeBuilder(List<Method> builderMethods) {
      super(builderMethods);
    }

    @Override
    public boolean matches(MethodCallContext context) {
      TestElement testElement = context.getTestElement();
      return testElement.getClass() == JMESPathExtractor.class
          || testElement.getClass() == JSONPostProcessor.class;
    }

    @Override
    protected MethodCall buildMethodCall(MethodCallContext context) {
      return context.getTestElement().getClass() == JSONPostProcessor.class
          ? buildJsonPathMethodCall(context)
          : buildJmesPathMethodCall(context);
    }

    private MethodCall buildJsonPathMethodCall(MethodCallContext context) {
      TestElement testElement = context.getTestElement();
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement,
          "JSONPostProcessor");
      MethodCall ret = buildMethodCall(paramBuilder.stringParam("referenceNames"),
          paramBuilder.stringParam("jsonPathExprs"));
      ret.chain("queryLanguage", new JsonPathQueryLanguageParam());
      DslScopedTestElement.ScopedTestElementCallBuilder.chainScopeTo(ret, testElement, "Sample");
      return ret.chain("matchNumber", paramBuilder.intParam("match_numbers", 1))
          .chain("defaultValue", paramBuilder.stringParam("defaultValues"));
    }

    private MethodCall buildJmesPathMethodCall(MethodCallContext context) {
      TestElement testElement = context.getTestElement();
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement,
          "JMESExtractor");
      MethodCall ret = buildMethodCall(paramBuilder.stringParam("referenceName"),
          paramBuilder.stringParam("jmesPathExpr"));
      DslScopedTestElement.ScopedTestElementCallBuilder.chainScopeTo(ret, testElement, "Sample");
      return ret.chain("matchNumber", paramBuilder.intParam("matchNumber", 1))
          .chain("defaultValue", paramBuilder.stringParam("defaultValue"));
    }

    public static class JsonPathQueryLanguageParam extends FixedParam<JsonQueryLanguage> {

      public JsonPathQueryLanguageParam() {
        super(JsonQueryLanguage.class, JsonQueryLanguage.JSON_PATH, null);
      }

      @Override
      public Set<String> getImports() {
        return Collections.singleton(paramType.getName());
      }

      @Override
      public String buildCode(String indent) {
        return paramType.getSimpleName() + "." + value.name();
      }

    }
  }

  /**
   * Specifies the query language used to extract from JSON.
   */
  public enum JsonQueryLanguage {
    /**
     * Specifies to use JMESPath.
     * <p>
     * Check <a href="https://jmespath.org/">JMESPath site</a> for more details.
     */
    JMES_PATH,
    /**
     * Specifies to use JSONPath. You can check
     * <a href="https://github.com/json-path/JsonPath">here</a> for documentation on JMeter
     * implementation of JSON Path.
     */
    JSON_PATH
  }

}
