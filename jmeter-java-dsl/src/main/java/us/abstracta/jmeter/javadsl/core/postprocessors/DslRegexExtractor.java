package us.abstracta.jmeter.javadsl.core.postprocessors;

import java.util.function.Consumer;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.gui.RegexExtractorGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.DslScopedTestElement;
import us.abstracta.jmeter.javadsl.core.MultiLevelTestElement;

/**
 * Allows extracting part of a request or response using regular expressions to store into a
 * variable.
 *
 * By default, the regular extractor is configured to extract from the main sample (does not include
 * sub samples) response body the first capturing group (part of regular expression that is inside
 * of parenthesis) of the first match of the regex. If no match is found, then the variable will not
 * be created or modified.
 *
 * @since 0.8
 */
public class DslRegexExtractor extends DslScopedTestElement<DslRegexExtractor> implements
    MultiLevelTestElement {

  private final String variableName;
  private final String regex;
  private int matchNumber = 1;
  private String template = "$1$";
  private String defaultValue;
  private TargetField fieldToCheck = TargetField.RESPONSE_BODY;

  public DslRegexExtractor(String variableName, String regex) {
    super("Regular Expression Extractor", RegexExtractorGui.class);
    this.variableName = variableName;
    this.regex = regex;
  }

  /**
   * Sets the match number to be extracted.
   * <p>
   * For example, if a response looks like this:
   * <pre>{@code user=test&user=tester}</pre>
   * and you use {@code user=([^&]+)} as regular expression, first match (1) would extract {@code
   * test} and second match (2) would extract {@code tester}.
   * <p>
   * When not specified, the first match will be used. When 0 is specified, a random match will be
   * used. When negative, all the matches are extracted to variables with name {@code
   * <variableName>_<matchNumber>}, the number of matches is stored in {@code
   * <variableName>_matchNr}, and default value is assigned to {@code <variableName>}.
   *
   * @param matchNumber specifies the match number to use.
   * @return the DslRegexExtractor to allow fluent usage and setting other properties.
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
   * When not specified {@code $1$ will be used}.
   *
   * @param template specifies template to use for storing in the JMeter variable.
   * @return the DslRegexExtractor to allow fluent usage and setting other properties.
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
   * @return the DslRegexExtractor to allow fluent usage and setting other properties.
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
   * @return the DslRegexExtractor to allow fluent usage and setting other properties.
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
    fieldToCheck.applyTo(ret);
    ret.setRefName(variableName);
    ret.setRegex(regex);
    ret.setMatchNumber(matchNumber);
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
  public enum TargetField {
    /**
     * Applies the regular extractor to the plain string of the response body.
     *
     * @since 0.10
     */
    RESPONSE_BODY(RegexExtractor::useBody),
    /**
     * Applies the regular extractor to the response body replacing all HTML escape codes.
     *
     * @since 0.10
     */
    RESPONSE_BODY_UNESCAPED(RegexExtractor::useUnescapedBody),
    /**
     * Applies the regular extractor to the string representation obtained from parsing the response
     * body with <a href="http://tika.apache.org/1.2/formats.html">Apache Tika</a>.
     *
     * @since 0.10
     */
    RESPONSE_BODY_AS_DOCUMENT(RegexExtractor::useBodyAsDocument),
    /**
     * Applies the regular extractor to response headers. Response headers is a string with headers
     * separated by new lines and names and values separated by colons.
     */
    RESPONSE_HEADERS(RegexExtractor::useHeaders),
    /**
     * Applies the regular extractor to request headers. Request headers is a string with headers
     * separated by new lines and names and values separated by colons.
     */
    REQUEST_HEADERS(RegexExtractor::useRequestHeaders),
    /**
     * Applies the regular extractor to the request URL.
     *
     * @since 0.10
     */
    REQUEST_URL(RegexExtractor::useUrl),
    /**
     * Applies the regular extractor to response code.
     */
    RESPONSE_CODE(RegexExtractor::useCode),
    /**
     * Applies the regular extractor to response message.
     */
    RESPONSE_MESSAGE(RegexExtractor::useMessage);

    private final Consumer<RegexExtractor> applier;

    TargetField(Consumer<RegexExtractor> applier) {
      this.applier = applier;
    }

    private void applyTo(RegexExtractor re) {
      applier.accept(re);
    }
  }

}
