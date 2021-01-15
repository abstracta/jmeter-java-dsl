package us.abstracta.jmeter.javadsl.core.listeners;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.error.ShouldHaveContent;
import org.assertj.core.internal.Diff;
import org.assertj.core.internal.Failures;
import org.assertj.core.util.diff.Chunk;
import org.assertj.core.util.diff.Delta;

public class FileTemplateAssert extends AbstractAssert<FileTemplateAssert, Path> {

  private final Diff diff;
  private final Failures failures;

  private FileTemplateAssert(Path actual) {
    super(actual, FileTemplateAssert.class);
    diff = new Diff();
    failures = Failures.instance();
  }

  public static FileTemplateAssert assertThat(Path actual) {
    return new FileTemplateAssert(actual);
  }

  public FileTemplateAssert matches(Path templatePath) throws IOException {
    String actualContent = getFileContents(actual);
    String templateContent = getFileContents(templatePath);
    if (!getTemplatePattern(templateContent).matcher(actualContent).matches()) {
      List<Delta<String>> diffs = diff
          .diff(actual, StandardCharsets.UTF_8, templatePath, StandardCharsets.UTF_8).stream()
          .filter( d -> !isDiffMatching(d))
          .collect(Collectors.toList());
      throw failures.failure(this.info,
          ShouldHaveContent.shouldHaveContent(actual, StandardCharsets.UTF_8, diffs));
    }
    return this;
  }

  private Pattern getTemplatePattern(String template) {
    int currentIndex = 0;
    StringBuilder patternString = new StringBuilder();
    String patternStartMarker = "{{";
    int patternStartIndex = template.indexOf(patternStartMarker);
    while (patternStartIndex >= 0) {
      patternString.append(Pattern.quote(template.substring(currentIndex, patternStartIndex)));
      String patternEndMarker = "}}";
      int patternEndIndex = template.indexOf(patternEndMarker, patternStartIndex);
      if (patternEndIndex < 0) {
        patternEndIndex = template.length();
      }
      patternString
          .append(template, patternStartIndex + patternStartMarker.length(), patternEndIndex);
      currentIndex = patternEndIndex + patternEndMarker.length();
      patternStartIndex =
          currentIndex < template.length() ? template.indexOf(patternStartMarker, currentIndex)
              : -1;
    }
    if (currentIndex < template.length()) {
      patternString.append(Pattern.quote(template.substring(currentIndex)));
    }
    return Pattern.compile(patternString.toString());
  }

  protected String getFileContents(Path filePath) throws IOException {
    return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
  }

  private boolean isDiffMatching(Delta<String> d) {
    return getTemplatePattern(buildChunkString(d.getOriginal())).matcher(buildChunkString(d.getRevised())).matches();
  }

  private String buildChunkString(Chunk<String> original) {
    return String.join("\n", original.getLines());
  }

}