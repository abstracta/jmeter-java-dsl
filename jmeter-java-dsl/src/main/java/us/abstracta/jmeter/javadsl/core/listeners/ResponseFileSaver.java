package us.abstracta.jmeter.javadsl.core.listeners;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.reporters.ResultSaver;
import org.apache.jmeter.reporters.gui.ResultSaverGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;

/**
 * Generates one file for each response of a sample/request.
 * <p>
 * This element is dependant of the scope: this means that if you add it at test plan level it will
 * generate files for all samplers in test plan, if added at thread group level then it will
 * generate files for samplers only in the thread group, and if you add it at sampler level it will
 * generate files only for the associated sampler.
 * <p>
 * By default, it will generate one file for each response using the given (which might include the
 * directory location) prefix to create the files and adding an incremental number to each response
 * and an extension according to the response mime type. Both the incremental number and the
 * extension can be set manually if skipAutoNumber and skipSuffix are set to true respectively.
 *
 * @since 0.13
 */
public class ResponseFileSaver extends BaseListener {

  protected String fileNamePrefix;
  protected boolean skipAutoNumber = false;
  protected boolean skipSuffix = false;

  public ResponseFileSaver(String fileNamePrefix) {
    super("Save Responses to a file", ResultSaverGui.class);
    this.fileNamePrefix = fileNamePrefix;
  }

  @Override
  protected TestElement buildTestElement() {
    ResultSaver ret = new ResultSaver();
    ret.setFilename(fileNamePrefix);
    ret.setSkipAutoNumber(skipAutoNumber);
    ret.setSkipSuffix(skipSuffix);
    return ret;
  }


  /**
   * Allows specifying whether the ResponseFileSaver appends a number to the end of the generated file.
   * <p>
   * By default, the ResponseFileSaver will add a number based on the samplers in the scope of the 
   * ResponseFileSaver test element. If set to true then no number will be appended.
   *
   * @param skipAutoNumber Boolean determining whether the number is added.
   * @return the ResponseFileSaver for further configuration or usage.
   */
  public ResponseFileSaver setSkipAutoNumber(boolean skipAutoNumber) {
    this.skipAutoNumber = skipAutoNumber;
    return this;
  }


  /**
   * Allows specifying whether the ResponseFileSaver will append the file type to the file name.
   * <p>
   * By default, the ResponseFileSaver will use the MIME type to append the file type to the end of the
   * generated file. If this is set to true then no file type will be appended.
   * 
   * @param skipSuffix Boolean determining whether a file type is added.
   * @return the ResponseFileSaver for further configuration or usage.
   */
  public ResponseFileSaver setSkipSuffix(boolean  skipSuffix) {
    this.skipSuffix = skipSuffix;
    return this;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<ResultSaver> {

    public CodeBuilder(List<Method> builderMethods) {
      super(ResultSaver.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(ResultSaver testElement, MethodCallContext context) {
      return buildMethodCall(
              new TestElementParamBuilder(testElement).stringParam(ResultSaver.FILENAME));
    }

  }

}
