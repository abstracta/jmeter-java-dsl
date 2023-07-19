package us.abstracta.jmeter.javadsl.core.assertions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.assertions.JSONPathAssertion;
import org.apache.jmeter.assertions.gui.JSONPathAssertionGui;
import org.apache.jmeter.assertions.jmespath.JMESPathAssertion;
import org.apache.jmeter.assertions.jmespath.gui.JMESPathAssertionGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsonExtractor.CodeBuilder.JsonPathQueryLanguageParam;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsonExtractor.JsonQueryLanguage;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;

/**
 * Allows asserting that part of a JSON response exists or has some value.
 * <p>
 * By default, the assertion will just check for the existence of given JSON JMESPath.
 *
 * @since 1.15
 */
public class DslJsonAssertion extends BaseTestElement implements DslAssertion {

  private static final String DEFAULT_JMESPATH_NAME = "JSON JMESPath Assertion";
  private static final String DEFAULT_JSONPATH_NAME = "JSON Assertion";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  protected String query;
  protected boolean isRegex;
  protected boolean validateValue;
  protected String value;
  protected boolean not;
  protected JsonQueryLanguage queryLanguage = JsonQueryLanguage.JMES_PATH;

  public DslJsonAssertion(String name, String query) {
    super(name, null);
    this.query = query;
  }

  /**
   * Specifies to check the value extracted with the given query to match the given regular
   * expression.
   *
   * @param regex specifies the regular expression to check extracted value with.
   * @return the assertion element for further configuration or usage.
   */
  public DslJsonAssertion matches(String regex) {
    validateValue = true;
    isRegex = true;
    value = regex;
    return this;
  }

  /**
   * Specifies to check the value extracted is the given value.
   * <p>
   * <b>Warning:</b> This method serializes the value with Jackson library and then configures
   * JMeter element with such value. If you want to pass the raw JSON to compare with, use
   * {@link #equalsToJson(String)} instead.
   * <p>
   * If you want to use a JMeter expression (Eg: {@code "${VAR}"}) for checking for some primitive
   * type (like int or boolean), then use {@link #equalsToJson(String)} instead.
   *
   * @param value specifies the value to check the extracted value against. Here you can pass
   *              primitive types (like ints, booleans, etc), Strings, collections (lists, maps),
   *              arrays, and even objects to be serialized with Jackson library. You can also
   *              specify null if you want to check if extracted value is null.
   * @return the assertion element for further configuration or usage.
   * @see #equalsToJson(String)
   */
  public DslJsonAssertion equalsTo(Object value) {
    validateValue = true;
    isRegex = false;
    try {
      this.value = value != null ? OBJECT_MAPPER.writeValueAsString(value) : null;
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
    return this;
  }

  /**
   * Specifies the raw JSON value to check the extracted value is equals to.
   * <p>
   * When using this method you must provide the actual JSON representation of the value. For
   * example, if you want to check the value is a given string, then you have to quote it like
   * {@code "\"test string\""}.
   * <p>
   * In general prefer using {@link #equalsTo(Object)} which is simpler.
   *
   * @param json specifies the raw json to check the extracted value against. You can specify here
   *             simple values like {@code "1"}, {@code "\"test string\""}, or more complex ones
   *             like {@code "[1, 2, 3]"} or {@code "{\"prop\": 1}"}. If you want to use a JMeter
   *             expression (eg: {@code "${VAR}"}) for checking for a string value, you should add
   *             quotes to it (eg: {@code "\"${VAR}\""}).
   * @return the assertion element for further configuration or usage.
   * @see #equalsTo(Object)
   * @since 1.16
   */
  public DslJsonAssertion equalsToJson(String json) {
    validateValue = true;
    isRegex = false;
    this.value = json;
    return this;
  }

  /**
   * Allows to check the inverse/negated condition specified by the rest of assertion settings.
   * <p>
   * For example, you can use it to check that a given path doesn't exist, or that extracted value
   * is not a given one or does not match a given pattern.
   *
   * @return the assertion element for further configuration or usage.
   */
  public DslJsonAssertion not() {
    not = !not;
    return this;
  }

  /**
   * Sames as {@link #not} but allowing to enable/disable the setting with a variable.
   *
   * @param negated when true, specifies to negate the check. When false, specifies to not negate
   *                the check.
   * @return the assertion element for further configuration or usage.
   */
  public DslJsonAssertion not(boolean negated) {
    not = negated;
    return this;
  }

  /**
   * Allows selecting the query language to use for JSON assertion.
   *
   * @param queryLanguage specifies the query language to use for assertions. When no value is
   *                      specified, JMESPath is used by default.
   * @return the assertion for further configuration and usage.
   * @see JsonQueryLanguage
   */
  public DslJsonAssertion queryLanguage(JsonQueryLanguage queryLanguage) {
    this.queryLanguage = queryLanguage;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    return queryLanguage == JsonQueryLanguage.JMES_PATH ? buildJmesPathAssertion()
        : buildJsonPathAssertion();
  }

  private TestElement buildJmesPathAssertion() {
    name = name != null ? name : DEFAULT_JMESPATH_NAME;
    guiClass = JMESPathAssertionGui.class;
    JMESPathAssertion ret = new JMESPathAssertion();
    ret.setJmesPath(query);
    ret.setJsonValidationBool(validateValue);
    ret.setIsRegex(isRegex);
    ret.setExpectedValue(value != null ? value.toString() : null);
    ret.setExpectNull(validateValue && value == null);
    ret.setInvert(not);
    return ret;
  }

  private TestElement buildJsonPathAssertion() {
    name = name != null ? name : DEFAULT_JSONPATH_NAME;
    guiClass = JSONPathAssertionGui.class;
    JSONPathAssertion ret = new JSONPathAssertion();
    ret.setJsonPath(query);
    ret.setJsonValidationBool(validateValue);
    ret.setIsRegex(isRegex);
    ret.setExpectedValue(value != null ? value.toString() : null);
    ret.setExpectNull(validateValue && value == null);
    ret.setInvert(not);
    return ret;
  }

  public static class CodeBuilder extends MethodCallBuilder {

    public CodeBuilder(List<Method> builderMethods) {
      super(builderMethods);
    }

    @Override
    public boolean matches(MethodCallContext context) {
      TestElement testElement = context.getTestElement();
      return testElement.getClass() == JMESPathAssertion.class
          || testElement.getClass() == JSONPathAssertion.class;
    }

    @Override
    protected MethodCall buildMethodCall(MethodCallContext context) {
      return context.getTestElement().getClass() == JSONPathAssertion.class
          ? buildJsonPathMethodCall(context)
          : buildJmesPathMethodCall(context);
    }

    private MethodCall buildJsonPathMethodCall(MethodCallContext context) {
      TestElement testElement = context.getTestElement();
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      MethodCall ret = buildAssertionMethodCall(DEFAULT_JSONPATH_NAME, JSONPathAssertion.JSONPATH,
          paramBuilder);
      ret.chain("queryLanguage", new JsonPathQueryLanguageParam());
      chainNot(ret, JSONPathAssertion.INVERT, paramBuilder);
      chainValueMatch(ret, JSONPathAssertion.JSONVALIDATION, JSONPathAssertion.ISREGEX,
          JSONPathAssertion.EXPECTEDVALUE, JSONPathAssertion.EXPECT_NULL, testElement);
      return ret;
    }

    private MethodCall buildAssertionMethodCall(String defaultName, String queryProp,
        TestElementParamBuilder paramBuilder) {
      return buildMethodCall(paramBuilder.nameParam(defaultName),
          paramBuilder.stringParam(queryProp));
    }

    private static void chainNot(MethodCall ret, String propName,
        TestElementParamBuilder paramBuilder) {
      ret.chain("not", paramBuilder.boolParam(propName, false));
    }

    private void chainValueMatch(MethodCall ret, String validationProp, String regexProp,
        String valueProp, String nullProp, TestElement element) {
      if (!element.getPropertyAsBoolean(validationProp)) {
        return;
      }
      if (element.getPropertyAsBoolean(regexProp)) {
        ret.chain("matches",
            new StringParam(element.getPropertyAsString(valueProp)));
      } else {
        if (element.getPropertyAsBoolean(nullProp)) {
          ret.chain("equalsTo", new NullParam());
        } else {
          ret.chain("equalsToJson", new StringParam(element.getPropertyAsString(valueProp)));
        }
      }
    }

    private MethodCall buildJmesPathMethodCall(MethodCallContext context) {
      TestElement testElement = context.getTestElement();
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      MethodCall ret = buildAssertionMethodCall(DEFAULT_JMESPATH_NAME, "JMES_PATH",
          paramBuilder);
      chainNot(ret, "INVERT", paramBuilder);
      chainValueMatch(ret, "JSONVALIDATION", "ISREGEX", "EXPECTED_VALUE", "EXPECT_NULL",
          testElement);
      return ret;
    }

  }

  private static class NullParam extends MethodParam {

    protected NullParam() {
      super(Object.class, null);
    }

    @Override
    public boolean isDefault() {
      return false;
    }

    @Override
    protected String buildCode(String indent) {
      return "null";
    }

  }

}
