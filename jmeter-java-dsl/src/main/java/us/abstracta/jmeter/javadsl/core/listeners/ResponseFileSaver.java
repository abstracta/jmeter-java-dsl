package us.abstracta.jmeter.javadsl.core.listeners;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.reporters.ResultSaver;
import org.apache.jmeter.reporters.gui.ResultSaverGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.BoolParam;

/**
 * Generates one file for each response of a sample/request.
 * <p>
 * This element is dependant of the scope: this means that if you add it at test plan level it will
 * generate files for all samplers in test plan, if added at thread group level then it will
 * generate files for samplers only in the thread group, and if you add it at sampler level it will
 * generate files only for the associated sampler.
 * <p>
 * By default, it will generate one file for each response using the given (which might include the
 * directory location) prefix to create the files and adding an incremental number and an extension
 * according to the response mime type.
 * <p>
 * Eg: <pre>{@code responseFileSaver("responses/resp")}</pre> will generate files like
 * "responses/resp1.json".
 * <p>
 * Both the incremental number and the file extension can be disabled setting
 * {@link #autoNumber(boolean)} and {@link #autoFileExtension(boolean)} to false.
 *
 * @since 0.13
 */
public class ResponseFileSaver extends BaseListener {

  protected String fileNamePrefix;
  protected boolean autoNumber = true;
  protected boolean autoFileExtension = true;

  public ResponseFileSaver(String fileNamePrefix) {
    super("Save Responses to a file", ResultSaverGui.class);
    this.fileNamePrefix = fileNamePrefix;
  }

  @Override
  protected TestElement buildTestElement() {
    ResultSaver ret = new ResultSaver();
    ret.setFilename(fileNamePrefix);
    ret.setSkipAutoNumber(!autoNumber);
    ret.setSkipSuffix(!autoFileExtension);
    return ret;
  }

  /**
   * Specifies whether, or not, to append an auto incremental number to each generated response file
   * name.
   * <p>
   * <b>WARNING:</b> if you disable this feature you might not get the files for all generated
   * responses (due to potential file name collision and file rewrite). Consider using some jmeter
   * expression in file name to avoid file name collisions and overrides (eg:
   * "responses/${__threadNum}-${__jm__Thread Group__idx}").
   *
   * @param autoNumber specifies to add the auto incremental numbers to the  file when set to true.
   *                   By default, this is set to true.
   * @return the ResponseFileSaver for further configuration or usage.
   * @since 1.27
   */
  public ResponseFileSaver autoNumber(boolean autoNumber) {
    this.autoNumber = autoNumber;
    return this;
  }

  /**
   * Specifies whether, or not, to append an automatic file extension to the file name.
   * <p>
   * The automatic file extension is solved according to the response MIME type.
   *
   * @param autoFileExtension specifies to use the automatic file type extension when set to true.
   *                          By default, is set ti true.
   * @return the ResponseFileSaver for further configuration or usage.
   * @since 1.27
   */
  public ResponseFileSaver autoFileExtension(boolean autoFileExtension) {
    this.autoFileExtension = autoFileExtension;
    return this;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<ResultSaver> {

    public CodeBuilder(List<Method> builderMethods) {
      super(ResultSaver.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(ResultSaver testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      MethodCall ret = buildMethodCall(paramBuilder.stringParam(ResultSaver.FILENAME));
      MethodParam skipAutoNumber = paramBuilder.boolParam(ResultSaver.SKIP_AUTO_NUMBER, false);
      if (!skipAutoNumber.isDefault()) {
        ret.chain("autoNumber", new BoolParam(false, true));
      }
      MethodParam skipSuffix = paramBuilder.boolParam(ResultSaver.SKIP_SUFFIX, false);
      if (!skipSuffix.isDefault()) {
        ret.chain("autoFileExtension", new BoolParam(false, true));
      }
      return ret;
    }

  }

}
