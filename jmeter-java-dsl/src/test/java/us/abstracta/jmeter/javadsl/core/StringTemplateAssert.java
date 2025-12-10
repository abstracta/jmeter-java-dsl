package us.abstracta.jmeter.javadsl.core;

import com.helger.commons.io.stream.StringInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.error.AbstractShouldHaveTextContent;
import org.assertj.core.error.ErrorMessageFactory;
import org.assertj.core.error.ShouldHaveContent;
import org.assertj.core.internal.Diff;
import org.assertj.core.internal.Failures;
import org.assertj.core.util.diff.Chunk;
import org.assertj.core.util.diff.Delta;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;
import us.abstracta.jmeter.javadsl.util.TestResource;

public abstract class StringTemplateAssert<SELF extends StringTemplateAssert<SELF, ACTUAL>, ACTUAL> extends
    AbstractAssert<SELF, ACTUAL> {

  private final Diff diff;
  private final Failures failures;

  protected StringTemplateAssert(ACTUAL actual, Class<?> selfType) {
    super(actual, selfType);
    diff = new Diff();
    failures = Failures.instance();
  }

  public static StringTemplateAssertPath assertThat(Path actual) {
    return new StringTemplateAssertPath(actual);
  }

  public static StringTemplateAssertString assertThat(String actual) {
    return new StringTemplateAssertString(actual);
  }

  protected abstract String getActualContents() throws IOException;

  protected abstract ErrorMessageFactory getErrorMessageFactory(List<Delta<String>> diffs);

  public SELF matches(TestResource template) throws IOException {
    return matches(template.rawContents());
  }

  public SELF matches(String templateContents) throws IOException {
    String actualContent = getActualContents();
    if (!new StringTemplate(templateContents).matches(actualContent)) {
      List<Delta<String>> diffs = diff
          .diff(new StringInputStream(actualContent, StandardCharsets.UTF_8),
              new StringInputStream(templateContents, StandardCharsets.UTF_8)).stream()
          .filter(d -> !isDiffMatching(d))
          .collect(Collectors.toList());
      if (diffs.isEmpty()) {
        objects.assertEqual(info, actualContent, templateContents);
      } else {
        throw failures.failure(this.info, getErrorMessageFactory(diffs));
      }
    }
    return (SELF) this;
  }

  private boolean isDiffMatching(Delta<String> d) {
    return new StringTemplate(buildChunkString(d.getOriginal())).matches(
        buildChunkString(d.getRevised()));
  }

  private String buildChunkString(Chunk<String> original) {
    return String.join("\n", original.getLines());
  }

  public static class StringTemplateAssertPath extends
      StringTemplateAssert<StringTemplateAssertPath, Path> {

    private StringTemplateAssertPath(Path actual) {
      super(actual, StringTemplateAssertPath.class);
    }

    @Override
    protected String getActualContents() throws IOException {
      return String.join("\n", Files.readAllLines(actual, StandardCharsets.UTF_8));
    }

    @Override
    protected ErrorMessageFactory getErrorMessageFactory(List<Delta<String>> diffs) {
      return ShouldHaveContent.shouldHaveContent(actual, StandardCharsets.UTF_8, diffs);
    }

  }

  public static class StringTemplateAssertString extends
      StringTemplateAssert<StringTemplateAssertString, String> {

    private StringTemplateAssertString(String actual) {
      super(actual, StringTemplateAssertString.class);
    }

    @Override
    protected String getActualContents() {
      return actual;
    }

    @Override
    protected ErrorMessageFactory getErrorMessageFactory(List<Delta<String>> diffs) {
      return new StringHasTextContent(diffs);
    }

    private static class StringHasTextContent extends AbstractShouldHaveTextContent {

      public StringHasTextContent(List<Delta<String>> diffs) {
        super("%nActual value does not match expected content:%n%n");
        this.diffs = diffsAsString(diffs);
      }

    }

  }

}