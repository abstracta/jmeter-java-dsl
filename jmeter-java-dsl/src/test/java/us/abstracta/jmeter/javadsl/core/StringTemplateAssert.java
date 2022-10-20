package us.abstracta.jmeter.javadsl.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.error.ShouldHaveContent;
import org.assertj.core.internal.Diff;
import org.assertj.core.internal.Failures;
import org.assertj.core.util.diff.Chunk;
import org.assertj.core.util.diff.Delta;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;
import us.abstracta.jmeter.javadsl.util.TestResource;

public class StringTemplateAssert extends AbstractAssert<StringTemplateAssert, Path> {

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
      String templateContents = template.contents();
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