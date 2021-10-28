package us.abstracta.jmeter.javadsl.core.postprocessors;

import java.util.function.Consumer;
import org.apache.jmeter.extractor.BoundaryExtractor;
import org.apache.jmeter.extractor.gui.BoundaryExtractorGui;
import org.apache.jmeter.testelement.TestElement;

/**
 * Provides simple means for extracting into a variable a part of a request or response using just
 * left and right boundaries surrounding the desired text.
 *
 * By default, the extractor is configured to extract from the main sample (does not include sub
 * samples) response body the first found match. If no match is found, then the variable will not be
 * created or modified.
 *
 * @since 0.28
 */
public class DslBoundaryExtractor extends DslVariableExtractor<DslBoundaryExtractor> {

  private final String leftBoundary;
  private final String rightBoundary;
  private TargetField fieldToCheck = TargetField.RESPONSE_BODY;

  public DslBoundaryExtractor(String varName, String leftBoundary, String rightBoundary) {
    super(varName, "Boundary Extractor", BoundaryExtractorGui.class);
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
   * @return the DslBoundaryExtractor to allow fluent usage and setting other properties.
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
   * @return the DslBoundaryExtractor to allow fluent usage and setting other properties.
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
   * @return the DslBoundaryExtractor to allow fluent usage and setting other properties.
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
    fieldToCheck.applyTo(ret);
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
  public enum TargetField {
    /**
     * Applies the extractor to the plain string of the response body.
     */
    RESPONSE_BODY(BoundaryExtractor::useBody),
    /**
     * Applies the extractor to the response body replacing all HTML escape codes.
     */
    RESPONSE_BODY_UNESCAPED(BoundaryExtractor::useUnescapedBody),
    /**
     * Applies the extractor to the string representation obtained from parsing the response body
     * with <a href="http://tika.apache.org/1.2/formats.html">Apache Tika</a>.
     */
    RESPONSE_BODY_AS_DOCUMENT(BoundaryExtractor::useBodyAsDocument),
    /**
     * Applies the extractor to response headers. Response headers is a string with headers
     * separated by new lines and names and values separated by colons.
     */
    RESPONSE_HEADERS(BoundaryExtractor::useHeaders),
    /**
     * Applies the extractor to request headers. Request headers is a string with headers separated
     * by new lines and names and values separated by colons.
     */
    REQUEST_HEADERS(BoundaryExtractor::useRequestHeaders),
    /**
     * Applies the extractor to the request URL.
     */
    REQUEST_URL(BoundaryExtractor::useUrl),
    /**
     * Applies the extractor to response code.
     */
    RESPONSE_CODE(BoundaryExtractor::useCode),
    /**
     * Applies the extractor to response message.
     */
    RESPONSE_MESSAGE(BoundaryExtractor::useMessage);

    private final Consumer<BoundaryExtractor> applier;

    TargetField(Consumer<BoundaryExtractor> applier) {
      this.applier = applier;
    }

    private void applyTo(BoundaryExtractor re) {
      applier.accept(re);
    }

  }

}
