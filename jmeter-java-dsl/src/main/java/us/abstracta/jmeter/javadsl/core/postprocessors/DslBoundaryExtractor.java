package us.abstracta.jmeter.javadsl.core.postprocessors;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.extractor.BoundaryExtractor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.gui.BoundaryExtractorGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam.EnumPropertyValue;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;

/**
 * Provides simple means for extracting into a variable a part of a request or response using just
 * left and right boundaries surrounding the desired text.
 * <p>
 * By default, the extractor is configured to extract from the main sample (does not include sub
 * samples) response body the first found match. If no match is found, then the variable will not be
 * created or modified.
 *
 * @since 0.28
 */
public class DslBoundaryExtractor extends DslVariableExtractor<DslBoundaryExtractor> {

  protected String leftBoundary;
  protected String rightBoundary;
  protected TargetField fieldToCheck = TargetField.RESPONSE_BODY;

  public DslBoundaryExtractor(String varName, String leftBoundary, String rightBoundary) {
    super("Boundary Extractor", BoundaryExtractorGui.class, varName);
    this.leftBoundary = leftBoundary;
    this.rightBoundary = rightBoundary;
  }

  /**
   * Sets the match number to be extracted.
   * <p>
   * For example, if a response looks like this:
   * <pre>{@code user=test&user=tester&}</pre>
   * and you use {@code user=} and {@code &} as left and right boundaries, first match (1) would
   * extract {@code test} and second match (2) would extract {@code tester}.
   * <p>
   * When not specified, the first match will be used. When 0 is specified, a random match will be
   * used. When negative, all the matches are extracted to variables with name {@code
   * <variableName>_<matchNumber>}, the number of matches is stored in {@code
   * <variableName>_matchNr}, and default value is assigned to {@code <variableName>}.
   *
   * @param matchNumber specifies the match number to use.
   * @return the extractor for further configuration or usage.
   */
  public DslBoundaryExtractor matchNumber(int matchNumber) {
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
   * When not specified then the variable will not be set if no match is found.
   *
   * @param defaultValue specifies the default value to be used.
   * @return the extractor for further configuration or usage.
   */
  public DslBoundaryExtractor defaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  /**
   * Allows specifying what part of request or response to apply the extractor to.
   * <p>
   * When not specified then the extractor will be applied to the response body.
   *
   * @param fieldToCheck field to apply the extractor to.
   * @return the extractor for further configuration or usage.
   * @see TargetField
   */
  public DslBoundaryExtractor fieldToCheck(TargetField fieldToCheck) {
    this.fieldToCheck = fieldToCheck;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    BoundaryExtractor ret = new BoundaryExtractor();
    setScopeTo(ret);
    ret.setUseField(fieldToCheck.propertyValue);
    ret.setRefName(varName);
    ret.setLeftBoundary(leftBoundary);
    ret.setRightBoundary(rightBoundary);
    ret.setMatchNumber(matchNumber);
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
   * Used to specify the field the extractor will apply to.
   */
  public enum TargetField implements EnumPropertyValue {
    /**
     * Applies the extractor to the plain string of the response body.
     */
    RESPONSE_BODY(RegexExtractor.USE_BODY),
    /**
     * Applies the extractor to the response body replacing all HTML escape codes.
     */
    RESPONSE_BODY_UNESCAPED(RegexExtractor.USE_BODY_UNESCAPED),
    /**
     * Applies the extractor to the string representation obtained from parsing the response body
     * with <a href="http://tika.apache.org/1.2/formats.html">Apache Tika</a>.
     */
    RESPONSE_BODY_AS_DOCUMENT(RegexExtractor.USE_BODY_AS_DOCUMENT),
    /**
     * Applies the extractor to response headers. Response headers is a string with headers
     * separated by new lines and names and values separated by colons.
     */
    RESPONSE_HEADERS(RegexExtractor.USE_HDRS),
    /**
     * Applies the extractor to request headers. Request headers is a string with headers separated
     * by new lines and names and values separated by colons.
     */
    REQUEST_HEADERS(RegexExtractor.USE_REQUEST_HDRS),
    /**
     * Applies the extractor to the request URL.
     */
    REQUEST_URL(RegexExtractor.USE_URL),
    /**
     * Applies the extractor to response code.
     */
    RESPONSE_CODE(RegexExtractor.USE_CODE),
    /**
     * Applies the extractor to response message.
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

  public static class CodeBuilder extends ScopedTestElementCallBuilder<BoundaryExtractor> {

    public CodeBuilder(List<Method> builderMethods) {
      super(BoundaryExtractor.class, builderMethods);
    }

    @Override
    protected MethodCall buildScopedMethodCall(BoundaryExtractor testElement) {
      TestElementParamBuilder paramBuilder = buildParamBuilder(testElement);
      return buildMethodCall(paramBuilder.stringParam("refname"),
          paramBuilder.stringParam("lboundary"), paramBuilder.stringParam("rboundary"));
    }

    private TestElementParamBuilder buildParamBuilder(BoundaryExtractor testElement) {
      return new TestElementParamBuilder(testElement, "BoundaryExtractor");
    }

    @Override
    protected void chainScopedElementAdditionalOptions(MethodCall ret,
        BoundaryExtractor testElement) {
      TestElementParamBuilder paramBuilder = buildParamBuilder(testElement);
      ret.chain("fieldToCheck",
          paramBuilder.enumParam("useHeaders", TargetField.RESPONSE_BODY));
      ret.chain("matchNumber", paramBuilder.intParam("match_number", 1));
      ret.chain("defaultValue", buildDefaultParam(paramBuilder));
    }

    private MethodParam buildDefaultParam(TestElementParamBuilder paramBuilder) {
      MethodParam param = paramBuilder.boolParam("default_empty_value", false);
      if (!param.isDefault()) {
        return new StringParam("");
      } else {
        MethodParam sourceDefaultParam = paramBuilder.stringParam("default");
        return sourceDefaultParam.isDefault() ? new StringParam(null) : sourceDefaultParam;
      }
    }

  }

}
