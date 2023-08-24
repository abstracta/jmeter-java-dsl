package us.abstracta.jmeter.javadsl.core.postprocessors;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.gui.RegexExtractorGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam.EnumPropertyValue;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;

/**
 * Allows extracting part of a request or response using regular expressions to store into a
 * variable.
 * <p>
 * By default, the regular extractor is configured to extract from the main sample (does not include
 * sub samples) response body the first capturing group (part of regular expression that is inside
 * of parenthesis) of the first match of the regex. If no match is found, then the variable will not
 * be created or modified.
 *
 * @since 0.8
 */
public class DslRegexExtractor extends DslVariableExtractor<DslRegexExtractor> {

  protected String regex;
  protected String template = "$1$";
  protected TargetField fieldToCheck = TargetField.RESPONSE_BODY;

  public DslRegexExtractor(String varName, String regex) {
    super("Regular Expression Extractor", RegexExtractorGui.class, varName);
    this.regex = regex;
  }

  /**
   * Sets the match number to be extracted.
   * <p>
   * For example, if a response looks like this:
   * <pre>{@code user=test&user=tester}</pre>
   * and you use {@code user=([^&]+)} as regular expression, first match (1) would extract
   * {@code test} and second match (2) would extract {@code tester}.
   * <p>
   * When not specified, the first match will be used. When 0 is specified, a random match will be
   * used. When negative, all the matches are extracted to variables with name {@code
   * <variableName>_<matchNumber>}, the number of matches is stored in {@code
   * <variableName>_matchNr}, and default value is assigned to {@code <variableName>}.
   *
   * @param matchNumber specifies the match number to use.
   * @return the extractor for further configuration or usage.
   */
  public DslRegexExtractor matchNumber(int matchNumber) {
    this.matchNumber = matchNumber;
    return this;
  }

  /**
   * Specifies the final string to store in the JMeter Variable.
   * <p>
   * The string may contain capturing groups (regular expression segments between parenthesis)
   * references by using {@code $<groupId>$} expressions (eg: {@code $1$} for first group). Check <a
   * href="https://jmeter.apache.org/usermanual/component_reference.html#Regular_Expression_Extractor">JMeter
   * Regular Expression Extractor documentation</a> for more details.
   * <p>
   * For example, if a response looks like this:
   * <pre>{@code email=tester@abstracta.us}</pre>
   * And you use {@code user=([^&]+)} as regular expression. Then {@code $1$-$2$} will result in
   * storing in the specified JMeter variable the value {@code tester-abstracta}.
   * <p>
   * When not specified {@code $1$} will be used.
   *
   * @param template specifies template to use for storing in the JMeter variable.
   * @return the extractor for further configuration or usage.
   */
  public DslRegexExtractor template(String template) {
    this.template = template;
    return this;
  }

  /**
   * Sets the default value to be stored in the JMeter variable when the regex does not match.
   * <p>
   * When match number is negative then the value is always assigned to the variable name.
   * <p>
   * A common pattern is to specify this value to a known value (e.g.:
   * &lt;VAR&gt;_EXTRACTION_FAILURE) and then add some assertion on the variable to mark request as
   * failure when the match doesn't work.
   * <p>
   * When not specified then the variable will not be set if no match is found.
   *
   * @param defaultValue specifies the default value to be used.
   * @return the extractor for further configuration or usage.
   */
  public DslRegexExtractor defaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  /**
   * Allows specifying what part of request or response to apply the regular extractor to.
   * <p>
   * When not specified then the regular extractor will be applied to the response body.
   *
   * @param fieldToCheck field to apply the regular extractor to.
   * @return the extractor for further configuration or usage.
   * @see TargetField
   */
  public DslRegexExtractor fieldToCheck(TargetField fieldToCheck) {
    this.fieldToCheck = fieldToCheck;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    RegexExtractor ret = new RegexExtractor();
    setScopeTo(ret);
    ret.setUseField(fieldToCheck.propertyValue);
    ret.setRefName(varName);
    ret.setRegex(regex);
    /*
     we use string instead of int method to generate same JMX as JMeter GUI generates and avoid
     incompatibility with potential JMX parsers (like the OctoPerf one).
     */
    ret.setMatchNumber(String.valueOf(matchNumber));
    ret.setTemplate(template);
    if (defaultValue != null) {
      if (defaultValue.isEmpty()) {
        ret.setDefaultEmptyValue(true);
      } else {
        ret.setDefaultValue(defaultValue);
      }
    }
    return ret;
  }

  /**
   * Used to specify the field the regular extractor will apply to.
   */
  public enum TargetField implements EnumPropertyValue {
    /**
     * Applies the regular extractor to the plain string of the response body.
     *
     * @since 0.10
     */
    RESPONSE_BODY(RegexExtractor.USE_BODY),
    /**
     * Applies the regular extractor to the response body replacing all HTML escape codes.
     *
     * @since 0.10
     */
    RESPONSE_BODY_UNESCAPED(RegexExtractor.USE_BODY_UNESCAPED),
    /**
     * Applies the regular extractor to the string representation obtained from parsing the response
     * body with <a href="http://tika.apache.org/1.2/formats.html">Apache Tika</a>.
     *
     * @since 0.10
     */
    RESPONSE_BODY_AS_DOCUMENT(RegexExtractor.USE_BODY_AS_DOCUMENT),
    /**
     * Applies the regular extractor to response headers. Response headers is a string with headers
     * separated by new lines and names and values separated by colons.
     */
    RESPONSE_HEADERS(RegexExtractor.USE_HDRS),
    /**
     * Applies the regular extractor to request headers. Request headers is a string with headers
     * separated by new lines and names and values separated by colons.
     */
    REQUEST_HEADERS(RegexExtractor.USE_REQUEST_HDRS),
    /**
     * Applies the regular extractor to the request URL.
     *
     * @since 0.10
     */
    REQUEST_URL(RegexExtractor.USE_URL),
    /**
     * Applies the regular extractor to response code.
     */
    RESPONSE_CODE(RegexExtractor.USE_CODE),
    /**
     * Applies the regular extractor to response message.
     */
    RESPONSE_MESSAGE(RegexExtractor.USE_MESSAGE);

    private final String propertyValue;

    TargetField(String propertyValue) {
      this.propertyValue = propertyValue;
    }

    @Override
    public String propertyValue() {
      return propertyValue;
    }

  }

  public static class CodeBuilder extends ScopedTestElementCallBuilder<RegexExtractor> {

    public CodeBuilder(List<Method> builderMethods) {
      super(RegexExtractor.class, builderMethods);
    }

    @Override
    protected MethodCall buildScopedMethodCall(RegexExtractor testElement) {
      TestElementParamBuilder paramBuilder = buildParamBuilder(testElement);
      return buildMethodCall(paramBuilder.stringParam("refname"),
          paramBuilder.stringParam("regex"));
    }

    private TestElementParamBuilder buildParamBuilder(RegexExtractor testElement) {
      return new TestElementParamBuilder(testElement, "RegexExtractor");
    }

    @Override
    protected void chainScopedElementAdditionalOptions(MethodCall ret, RegexExtractor testElement) {
      TestElementParamBuilder paramBuilder = buildParamBuilder(testElement);
      ret.chain("fieldToCheck", paramBuilder.enumParam("useHeaders", TargetField.RESPONSE_BODY));
      ret.chain("matchNumber", paramBuilder.intParam("match_number", 1));
      ret.chain("template", paramBuilder.stringParam("template", "$1$"));
      ret.chain("defaultValue", buildDefaultParam(paramBuilder));
    }

    private MethodParam buildDefaultParam(TestElementParamBuilder regexParamBuilder) {
      MethodParam param = regexParamBuilder.boolParam("default_empty_value", false);
      if (!param.isDefault()) {
        return new StringParam("");
      } else {
        MethodParam sourceDefaultParam = regexParamBuilder.stringParam("default");
        return sourceDefaultParam.isDefault() ? new StringParam(null) : sourceDefaultParam;
      }
    }

  }

}
