package us.abstracta.jmeter.javadsl.core.assertions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.BoolParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam.EnumPropertyValue;
import us.abstracta.jmeter.javadsl.core.testelements.DslScopedTestElement;

/**
 * Allows marking a request result as success or failure by a specific result field value.
 *
 * @since 0.11
 */
public class DslResponseAssertion extends DslScopedTestElement<DslResponseAssertion> implements
    DslAssertion {

  protected static final String DEFAULT_NAME = "Response Assertion";

  protected TargetField fieldToTest = TargetField.RESPONSE_BODY;
  protected boolean ignoreStatus;
  protected List<String> testStrings = new ArrayList<>();
  protected TestStringStrategy testStrategy = TestStringStrategy.SUBSTRING;
  protected boolean invertCheck;
  protected boolean anyMatch;

  public DslResponseAssertion(String name) {
    super(name != null ? name : DEFAULT_NAME, AssertionGui.class);
  }

  /**
   * Specifies what field to apply the assertion to.
   * <p>
   * When not specified it will apply the given assertion to the response body.
   *
   * @param fieldToTest specifies the field to apply the assertion to.
   * @return the response assertion for further configuration or usage.
   * @see TargetField
   */
  public DslResponseAssertion fieldToTest(TargetField fieldToTest) {
    this.fieldToTest = fieldToTest;
    return this;
  }

  /**
   * Specifies that any previously status set to the request should be ignored, and request should
   * be marked as success by default.
   * <p>
   * This allows overriding the default behavior provided by JMeter when marking requests as failed
   * (eg: HTTP status codes like 4xx or 5xx). This is particularly useful when tested application
   * returns an unsuccessful response (eg: 400) but you want to consider some of those cases still
   * as successful using a different criteria to determine when they are actually a failure (an
   * unexpected response).
   * <p>
   * Take into consideration that if you specify multiple response assertions to the same sampler,
   * then if this flag is enabled, any previous assertion result in same sampler will be ignored
   * (marked as success). So, consider setting this flag in first response assertion only.
   *
   * @return the response assertion for further configuration or usage.
   */
  public DslResponseAssertion ignoreStatus() {
    return ignoreStatus(true);
  }

  /**
   * Same as {@link #ignoreStatus()} but allowing to enable or disable it.
   * <p>
   * This is helpful when the resolution is taken at runtime.
   *
   * @param enable specifies to enable or disable the setting. By default, it is set to false.
   * @return the response assertion for further configuration or usage.
   * @see #ignoreStatus()
   * @since 1.0
   */
  public DslResponseAssertion ignoreStatus(boolean enable) {
    ignoreStatus = enable;
    return this;
  }

  /**
   * Checks if the specified {@link #fieldToTest(TargetField)} contains the given substrings.
   * <p>
   * By default, the main sample (not sub samples) response body will be checked, and all supplied
   * substrings must be contained. Review other methods in this class if you need to check
   * substrings but in some other ways (eg: in response headers, any match is enough, or none of
   * specified substrings should be contained).
   *
   * @param substrings list of strings to be searched in the given field to test (by default
   *                   response body).
   * @return the response assertion for further configuration or usage.
   */
  public DslResponseAssertion containsSubstrings(String... substrings) {
    return testStrings(substrings, TestStringStrategy.SUBSTRING);
  }

  private DslResponseAssertion testStrings(String[] testStrings, TestStringStrategy strategy) {
    this.testStrings = Arrays.asList(testStrings);
    this.testStrategy = strategy;
    return this;
  }

  /**
   * Compares the configured {@link #fieldToTest(TargetField)} to the given strings for equality.
   * <p>
   * By default, the main sample (not sub samples) response body will be checked, and all supplied
   * strings must be equal to the body (in default setting only makes sense to specify one string).
   * Review other methods in this class if you need to check equality to entire strings but in some
   * other ways (eg: in response headers, any match is enough, or none of specified strings should
   * be equal to the field value).
   *
   * @param strings list of strings to be compared against the given field to test (by default
   *                response body).
   * @return the response assertion for further configuration or usage.
   */
  public DslResponseAssertion equalsToStrings(String... strings) {
    return testStrings(strings, TestStringStrategy.EQUALS);
  }

  /**
   * Checks if the configured {@link #fieldToTest(TargetField)} contains matches for given regular
   * expressions.
   * <p>
   * By default, the main sample (not sub samples) response body will be checked, and all supplied
   * regular expressions must contain a match in the body. Review other methods in this class if you
   * need to check regular expressions matches are contained but in some other ways (eg: in response
   * headers, any regex match is enough, or none of specified regex should be contained in the field
   * value).
   * <p>
   * By default, regular expressions evaluate in multi-line mode, which means that '.' does not
   * match new lines, '^' matches start of lines and '$' matches end of lines. To use single-line
   * mode prefix '(?s)' to the regular expressions. Regular expressions are also by default
   * case-sensitive, which can be changed to insensitive by adding '(?i)' to the regex.
   *
   * @param regexes list of regular expressions to search for matches in the field to test (by
   *                default response body).
   * @return the response assertion for further configuration or usage.
   */
  public DslResponseAssertion containsRegexes(String... regexes) {
    return testStrings(regexes, TestStringStrategy.CONTAINS_REGEX);
  }

  /**
   * Checks if the configured {@link #fieldToTest(TargetField)} matches (completely, and not just
   * part of it) given regular expressions.
   * <p>
   * By default, the main sample (not sub samples) response body will be checked, and all supplied
   * regular expressions must match the entire body. Review other methods in this class if you need
   * to check regular expressions matches but in some other ways (eg: in response headers, any regex
   * match is enough, or none of specified regex should be matched with the field value).
   * <p>
   * By default, regular expressions evaluate in multi-line mode, which means that '.' does not
   * match new lines, '^' matches start of lines and '$' matches end of lines. To use single-line
   * mode prefix '(?s)' to the regular expressions. Regular expressions are also by default
   * case-sensitive, which can be changed to insensitive by adding '(?i)' to the regex.
   *
   * @param regexes list of regular expressions the field to test (by default response body) must
   *                match.
   * @return the response assertion for further configuration or usage.
   */
  public DslResponseAssertion matchesRegexes(String... regexes) {
    return testStrings(regexes, TestStringStrategy.MATCHES_REGEX);
  }

  /**
   * Allows inverting/negating each of the checks applied by the assertion.
   * <p>
   * This is the same as the "Not" option in Response Assertion in JMeter GUI.
   * <p>
   * It is important to note that the inversion of the check happens at each check and not to the
   * final result. Eg:
   *
   * <pre>{@code
   *   responseAssertion().containsSubstrings("error", "failure").invertCheck()
   * }</pre>
   * <p>
   * Will check that the response does not contain "error" and does not contain "failure". You can
   * think it as {@code !(containsSubstring("error")) && !(containsSubstring("failure"))}.
   * <p>
   * Similar logic applies when using in combination with anyMatch method. Eg:
   *
   * <pre>{@code
   *    responseAssertion().containsSubstrings("error", "failure").invertCheck().matchAny()
   * }</pre>
   * <p>
   * Will check that response does not contain both "error" and "failure" at the same time. This is
   * analogous to {@code !(containsSubstring("error")) || !(containsSubstring("failure)}, which is
   * equivalent to {@code !(containsSubstring("error") && containsSubstring("failure))}.
   * <p>
   * Keep in mind that order of invocations of methods in response assertion is irrelevant (so
   * {@code invertCheck().matchAny()} gets the same result as {@code matchAny().invertCheck()}).
   *
   * @return the response assertion for further configuration or usage.
   */
  public DslResponseAssertion invertCheck() {
    return invertCheck(true);
  }

  /**
   * Same as {@link #invertCheck()} but allowing to enable or disable it.
   * <p>
   * This is helpful when the resolution is taken at runtime.
   *
   * @param enable specifies to enable or disable the setting. By default, it is set to false.
   * @return the response assertion for further configuration or usage.
   * @see #invertCheck()
   * @since 1.0
   */
  public DslResponseAssertion invertCheck(boolean enable) {
    this.invertCheck = enable;
    return this;
  }

  /**
   * Specifies that if any check matches then the response assertion is satisfied.
   * <p>
   * This is the same as the "Or" option in Response Assertion in JMeter GUI.
   * <p>
   * By default, when you use something like this:
   *
   * <pre>{@code
   *    responseAssertion().containsSubstrings("success", "OK")
   * }</pre>
   * <p>
   * The response assertion will be success when both "success" and "OK" sub strings appear in
   * response body (if one or both don't appear, then it fails). You can think of it like
   * {@code containsSubstring("success") && containsSubstring("OK")}.
   * <p>
   * If you want to check that any of them matches then use anyMatch, like this:
   *
   * <pre>{@code
   *     responseAssertion().containsSubstrings("success", "OK").anyMatch()
   * }</pre>
   * <p>
   * Which you can interpret as {@code containsSubstring("success") || containsSubstring("OK")}.
   *
   * @return the response assertion for further configuration or usage.
   */
  public DslResponseAssertion anyMatch() {
    return anyMatch(true);
  }

  /**
   * Same as {@link #anyMatch()} but allowing to enable or disable it.
   * <p>
   * This is helpful when the resolution is taken at runtime.
   *
   * @param enable specifies to enable or disable the setting. By default, it is set to false.
   * @return the response assertion for further configuration or usage.
   * @see #anyMatch()
   * @since 1.0
   */
  public DslResponseAssertion anyMatch(boolean enable) {
    anyMatch = enable;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    ResponseAssertion ret = new ResponseAssertion();
    setScopeTo(ret);
    ret.setProperty("Assertion.test_field", fieldToTest.propertyValue);
    ret.setAssumeSuccess(ignoreStatus);
    if (invertCheck) {
      ret.setToNotType();
    }
    if (anyMatch) {
      ret.setToOrType();
    }
    testStrategy.applyTo(ret);
    testStrings.forEach(ret::addTestString);
    return ret;
  }

  /**
   * Identifies a particular field to apply the assertion to.
   */
  public enum TargetField implements EnumPropertyValue {
    /**
     * Applies the assertion to the response body.
     */
    RESPONSE_BODY("response_data"),
    /**
     * Applies the assertion to the text obtained through <a
     * href="http://tika.apache.org/1.2/formats.html">Apache Tika</a> from the response body (which
     * might be a pdf, excel, etc.).
     */
    RESPONSE_BODY_AS_DOCUMENT("response_data_as_document"),
    /**
     * Applies the assertion to the response code (eg: the HTTP response code, like 200).
     */
    RESPONSE_CODE("response_code"),
    /**
     * Applies the assertion to the response message (eg: the HTTP response message, like OK).
     */
    RESPONSE_MESSAGE("response_message"),
    /**
     * Applies the assertion to the set of response headers. Response headers is a string with
     * headers separated by new lines and names and values separated by colons.
     */
    RESPONSE_HEADERS("response_headers"),
    /**
     * Applies the assertion to the set of request headers. Request headers is a string with headers
     * separated by new lines and names and values separated by colons.
     */
    REQUEST_HEADERS("request_headers"),
    /**
     * Applies the assertion to the requested URL.
     */
    REQUEST_URL("sample_label"),
    /**
     * Applies the assertion to the request body.
     */
    REQUEST_BODY("request_data");

    private final String propertyValue;

    TargetField(String propertyValue) {
      this.propertyValue = "Assertion." + propertyValue;
    }

    @Override
    public String propertyValue() {
      return propertyValue;
    }

  }

  public enum TestStringStrategy {
    CONTAINS_REGEX(ResponseAssertion::setToContainsType),
    MATCHES_REGEX(ResponseAssertion::setToMatchType),
    SUBSTRING(ResponseAssertion::setToSubstringType),
    EQUALS(ResponseAssertion::setToEqualsType);

    private final Consumer<ResponseAssertion> applier;

    TestStringStrategy(Consumer<ResponseAssertion> applier) {
      this.applier = applier;
    }

    private void applyTo(ResponseAssertion assertion) {
      applier.accept(assertion);
    }

  }

  public static class CodeBuilder extends ScopedTestElementCallBuilder<ResponseAssertion> {

    public CodeBuilder(List<Method> builderMethods) {
      super("Assertion", ResponseAssertion.class, builderMethods);
    }

    @Override
    protected MethodCall buildScopedMethodCall(ResponseAssertion testElement) {
      return buildMethodCall(new TestElementParamBuilder(testElement).nameParam(DEFAULT_NAME));
    }

    @Override
    protected void chainScopedElementAdditionalOptions(MethodCall ret,
        ResponseAssertion testElement) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement, "Assertion");
      ret.chain("fieldToTest", paramBuilder.enumParam("test_field", TargetField.RESPONSE_BODY));
      ret.chain("ignoreStatus", paramBuilder.boolParam("assume_success", false));
      ret.chain("anyMatch", new BoolParam(testElement.isOrType(), false));
      ret.chain("invertCheck", new BoolParam(testElement.isNotType(), false));
      ret.chain(findTestingStrategyMethod(testElement),
          new StringsCollectionParam(testElement.getTestStrings()));
    }

    public static class StringsCollectionParam extends MethodParam {

      private final CollectionProperty collection;

      protected StringsCollectionParam(CollectionProperty prop) {
        super(String[].class, prop.getStringValue());
        collection = prop;
      }

      @Override
      protected String buildCode(String indent) {
        if (collection.size() == 1) {
          return buildStringLiteral(collection.get(0).getStringValue(), indent);
        }
        return "\n"
            + propertyIterator2Stream(collection.iterator())
            .map(p -> indent + buildStringLiteral(p.getStringValue(), indent))
            .collect(Collectors.joining(",\n"))
            + "\n";
      }

    }

    private String findTestingStrategyMethod(ResponseAssertion testElement) {
      if (testElement.isSubstringType()) {
        return "containsSubstrings";
      } else if (testElement.isEqualsType()) {
        return "equalsToStrings";
      } else if (testElement.isContainsType()) {
        return "containsRegexes";
      } else if (testElement.isMatchType()) {
        return "matchesRegexes";
      }
      throw new UnsupportedOperationException(
          String.format("The response assertion has a type (%d) which is not supported by the DSL.",
              testElement.getTestType()));
    }

  }

}
