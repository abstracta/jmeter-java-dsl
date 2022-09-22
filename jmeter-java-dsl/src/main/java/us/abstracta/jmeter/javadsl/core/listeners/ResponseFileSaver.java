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
 * and an extension according to the response mime type.
 *
 * @since 0.13
 */
public class ResponseFileSaver extends BaseListener {

  protected String fileNamePrefix;

  public ResponseFileSaver(String fileNamePrefix) {
    super("Save Responses to a file", ResultSaverGui.class);
    this.fileNamePrefix = fileNamePrefix;
  }

  @Override
  protected TestElement buildTestElement() {
    ResultSaver ret = new ResultSaver();
    ret.setFilename(fileNamePrefix);
    return ret;
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
