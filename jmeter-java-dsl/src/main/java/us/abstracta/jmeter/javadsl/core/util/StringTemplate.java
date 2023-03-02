package us.abstracta.jmeter.javadsl.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String template engine allowing easy string regex matching and usual template engine resolution.
 * <p>
 * This engine uses a syntax inspired in mustache using {@code {{}}} as basic indicator of special
 * treatment/replacement.
 * <p>
 * To check fo string regex matching you can have a template like
 * <pre>&lt;root version="1.2"&gt;{{\d+}}&lt;/root&gt;</pre>
 * and use {@link #matches(String)}. No need to escape regex special characters outside of
 * {@code {{}} }.
 * <p>
 * To use regular string template resolution you can use templates like
 * <pre>&lt;root version="1.2"&gt;{{value}}&lt;/root&gt;</pre>
 * and use {@link #bind(String, Object)} and {@link #solve()} to get the resulting string of
 * replacing each occurrence of {@code {{}} } with the bound value. Additionally, you can define
 * default values for each replacement expression. In this example
 * <pre>&lt;root version="1.2"&gt;{{value:3}}&lt;/root&gt;</pre>
 * it will solve "value" to string "3" if no value is bound to "value" or if bound value is null. If
 * a replacement has no binding value different from null and no default value is specified, then an
 * exception will be generated. You can always specify an empty default value (like
 * {@code {{value:}}}) which avoids the exception and generates an empty string instead.
 * <p>
 * You can even use one template for both regex matching or string template solving. Eg:
 * <pre>&lt;root version="1.2"&gt;{{value:3~\d+}}&lt;/root&gt;</pre>
 * can be used with {@link #matches(String)} or with {@link #bind(String, Object)} and
 * {@link #solve()}.
 */
public class StringTemplate {

  public static final String EXPRESSION_START_MARKER = "{{";
  public static final String EXPRESSION_END_MARKER = "}}";
  private static final Pattern EXPRESSION_PATTERN = Pattern.compile(
      Pattern.quote(EXPRESSION_START_MARKER) + "(.*?)" + Pattern.quote(EXPRESSION_END_MARKER));
  private static final Pattern EXPRESSION_WITH_VAR_NAME_PATTERN = Pattern.compile(
      "^(\\w+)?(:[^~]*)?(~.*)?$");
  private final String template;
  private final Map<String, Object> bindings = new HashMap<>();
  private boolean ignoreMissingBindings;

  public StringTemplate(String template) {
    this.template = template;
  }

  public boolean matches(String string) {
    Pattern templatePattern = Pattern.compile(
        processTemplate(Pattern::quote, this::getExpressionPattern));
    return templatePattern.matcher(string).matches();
  }

  private String processTemplate(Function<String, String> literalProcessor,
      Function<String, String> expressionProcessor) {
    StringBuilder ret = new StringBuilder();
    int currentIndex = 0;
    Matcher matcher = EXPRESSION_PATTERN.matcher(template);
    while (matcher.find()) {
      ret.append(literalProcessor.apply(template.substring(currentIndex, matcher.start())));
      String expression = template.substring(matcher.start() + EXPRESSION_START_MARKER.length(),
          matcher.end() - EXPRESSION_END_MARKER.length());
      ret.append(expressionProcessor.apply(expression));
      currentIndex = matcher.end();
    }
    if (currentIndex < template.length()) {
      ret.append(literalProcessor.apply(template.substring(currentIndex)));
    }
    return ret.toString();
  }

  private String getExpressionPattern(String expression) {
    Matcher varExpressionMatcher = EXPRESSION_WITH_VAR_NAME_PATTERN.matcher(expression);
    if (!varExpressionMatcher.matches()) {
      return expression;
    }
    String regex = varExpressionMatcher.group(3);
    if (regex != null) {
      return regex.substring(1);
    }
    String defaultVal = varExpressionMatcher.group(2);
    return defaultVal == null ? ".*" : Pattern.quote(defaultVal.substring(1));
  }

  public StringTemplate bind(String key, Object value) {
    bindings.put(key, value);
    return this;
  }

  public StringTemplate ignoreMissingBindings() {
    ignoreMissingBindings = true;
    return this;
  }

  public String solve() {
    return processTemplate(s -> s, this::solveExpression);
  }

  private String solveExpression(String expression) {
    Matcher varExpressionMatcher = EXPRESSION_WITH_VAR_NAME_PATTERN.matcher(expression);
    if (!varExpressionMatcher.matches()) {
      Object bind = bindings.get(expression);
      return bind != null ? bind.toString() : handleMissingBinding(expression);
    }
    String varName = varExpressionMatcher.group(1);
    Object bind = bindings.get(varName);
    if (bind != null) {
      return bind.toString();
    }
    String defaultVal = varExpressionMatcher.group(2);
    return defaultVal != null ? defaultVal.substring(1) : handleMissingBinding(varName);
  }

  private String handleMissingBinding(String expression) {
    if (ignoreMissingBindings) {
      return EXPRESSION_START_MARKER + expression + EXPRESSION_END_MARKER;
    }
    throw new IllegalStateException("No binding was found for: " + expression);
  }

}

