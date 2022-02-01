package us.abstracta.jmeter.javadsl.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.error.ShouldHaveContent;
import org.assertj.core.internal.Diff;
import org.assertj.core.internal.Failures;
import org.assertj.core.util.diff.Chunk;
import org.assertj.core.util.diff.Delta;
import us.abstracta.jmeter.javadsl.TestResource;

public class StringTemplate {

  public static final String EXPRESSION_START_MARKER = "{{";
  public static final String EXPRESSION_END_MARKER = "}}";
  private static final Pattern EXPRESSION_PATTERN = Pattern.compile(
      Pattern.quote(EXPRESSION_START_MARKER) + "(.*?)" + Pattern.quote(EXPRESSION_END_MARKER));
  private static final Pattern EXPRESSION_WITH_VAR_NAME_PATTERN = Pattern.compile("^(\\w+)?(:[^~]*)?(~.*)?$");
  private final String template;
  private final Map<String, Object> bindings = new HashMap<>();

  public StringTemplate(String template) {
    this.template = template;
  }

  public boolean matches(String string) {
    Pattern templatePattern = Pattern.compile(processTemplate(Pattern::quote, this::getExpressionPattern));
    return templatePattern.matcher(string).matches();
  }
  private String processTemplate(Function<String, String> literalProcessor, Function<String, String> expressionProcessor) {
    StringBuilder ret = new StringBuilder();
    int currentIndex = 0;
    Matcher matcher = EXPRESSION_PATTERN.matcher(template);
    while (matcher.find()) {
      ret.append(literalProcessor.apply(template.substring(currentIndex, matcher.start())));
      String expression = template.substring(matcher.start() + EXPRESSION_START_MARKER.length(), matcher.end() - EXPRESSION_END_MARKER.length());
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

  public String solve() {
    return processTemplate(s -> s, this::solveExpression);
  }

  private String solveExpression(String expression) {
    Matcher varExpressionMatcher = EXPRESSION_WITH_VAR_NAME_PATTERN.matcher(expression);
    if (!varExpressionMatcher.matches()) {
      Object bind = bindings.get(expression);
      if (bind == null) {
        throw buildNoBindingException(expression);
      }
      return bind.toString();
    }
    String varName = varExpressionMatcher.group(1);
    Object bind = bindings.get(varExpressionMatcher.group(1));
    if (bind != null) {
      return bind.toString();
    }
    String defaultVal = varExpressionMatcher.group(2);
    if (defaultVal == null) {
      throw buildNoBindingException("No binding was found for: " + varName);
    } else {
      return defaultVal.substring(1);
    }
  }

  private IllegalStateException buildNoBindingException(String expression) {
    return new IllegalStateException("No binding was found for: " + expression);
  }

  public static class StringTemplateAssert extends AbstractAssert<StringTemplateAssert, Path> {

    private final Diff diff;
    private final Failures failures;

    private StringTemplateAssert(Path actual) {
      super(actual, StringTemplateAssert.class);
      diff = new Diff();
      failures = Failures.instance();
    }

    public static StringTemplateAssert assertThat(Path actual) {
      return new StringTemplateAssert(actual);
    }

    public StringTemplateAssert matches(TestResource template) throws IOException {
      String actualContent = getFileContents(actual);
      String templateContents = template.getContents();
      if (!new StringTemplate(templateContents).matches(actualContent)) {
        List<Delta<String>> diffs = diff
            .diff(actual, templateContents, StandardCharsets.UTF_8).stream()
            .filter(d -> !isDiffMatching(d))
            .collect(Collectors.toList());
        throw failures.failure(this.info,
            ShouldHaveContent.shouldHaveContent(actual, StandardCharsets.UTF_8, diffs));
      }
      return this;
    }

    protected String getFileContents(Path filePath) throws IOException {
      return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }

    private boolean isDiffMatching(Delta<String> d) {
      return new StringTemplate(buildChunkString(d.getOriginal())).matches(
          buildChunkString(d.getRevised()));
    }

    private String buildChunkString(Chunk<String> original) {
      return String.join("\n", original.getLines());
    }

  }

}

