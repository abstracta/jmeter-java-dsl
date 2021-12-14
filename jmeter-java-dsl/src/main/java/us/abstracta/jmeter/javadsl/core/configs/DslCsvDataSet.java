package us.abstracta.jmeter.javadsl.core.configs;

import com.blazemeter.jmeter.RandomCSVDataSetConfig;
import com.blazemeter.jmeter.RandomCSVDataSetConfigGui;
import java.nio.charset.StandardCharsets;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;

/**
 * Allows using a CSV file as input data for JMeter variables to use in test plan.
 * <p>
 * This element reads a CSV file and uses each line to generate JMeter variables to be used in each
 * iteration and thread of the test plan.
 * <p>
 * Is ideal to be able to easily create test plans that test with a lot of different of potential
 * requests or flows.
 * <p>
 * By default, it consumes comma separated variables, which names are included in first line of CSV,
 * automatically resets to the beginning of the file when the end is reached and the consumption of
 * the file is shared by all threads and thread groups in the test plan (ie: any iteration on a
 * thread will consume a line from the file, and advance to following line).
 * <p>
 * Additionally, this element sets by default the "quoted data" flag on JMeter CSV Data Set
 * element.
 *
 * @since 0.24
 */
public class DslCsvDataSet extends DslConfigElement {

  private final String file;
  private String delimiter = ",";
  private String encoding = StandardCharsets.UTF_8.name();
  private String[] variableNames;
  private boolean ignoreFirstLine;
  private boolean stopThread;
  private Sharing shareMode = Sharing.ALL_THREADS;
  private boolean randomOrder;

  public DslCsvDataSet(String csvFile) {
    super("CSV Data Set Config", TestBeanGUI.class);
    this.file = csvFile;
  }

  /**
   * Specifies the delimiter used by the file to separate variable values.
   *
   * @param delimiter specifies the delimiter. By default, it uses commas (,) as delimiters. If you
   *                  need to use tabs, then specify "\\t".
   * @return the DslCsvDataSet for further configuration.
   */
  public DslCsvDataSet delimiter(String delimiter) {
    this.delimiter = delimiter;
    return this;
  }

  /**
   * Specifies the file encoding used by the file.
   *
   * @param encoding the file encoding of the file. By default, it will use UTF-8 (which differs
   *                 from JMeter default, to have more consistent test plan execution). This might
   *                 require to be changed but in general is good to have all files in same encoding
   *                 (eg: UTF-8).
   * @return the DslCsvDataSet for further configuration.
   */
  public DslCsvDataSet encoding(String encoding) {
    this.encoding = encoding;
    return this;
  }

  /**
   * Specifies variable names to be assigned to the parsed values.
   * <p>
   * If you have a CSV file with existing headers and want to overwrite the name of generated
   * variables, then use {@link #ignoreFirstLine()} in conjunction with this method to specify the
   * new variable names. If you have a CSV file without a headers line, then you will need to use
   * this method to set proper names for the variables (otherwise first line of data will be used as
   * headers, which will not be good).
   *
   * @param variableNames names of variables to be extracted from the CSV file.
   * @return the DslCsvDataSet for further configuration.
   */
  public DslCsvDataSet variableNames(String... variableNames) {
    this.variableNames = variableNames;
    return this;
  }

  /**
   * Specifies to ignore first line of the CSV.
   * <p>
   * This should only be used in conjunction with {@link #variableNames(String...)} to overwrite
   * existing CSV headers names.
   *
   * @return the DslCsvDataSet for further configuration.
   */
  public DslCsvDataSet ignoreFirstLine() {
    this.ignoreFirstLine = true;
    return this;
  }

  /**
   * Specifies to stop threads when end of given CSV file is reached.
   * <p>
   * This method will automatically internally set JMeter test element property "recycle on EOF", so
   * you don't need to worry about such property.
   *
   * @return the DslCsvDataSet for further configuration.
   */
  public DslCsvDataSet stopThreadOnEOF() {
    this.stopThread = true;
    return this;
  }

  /**
   * Allows changing the way CSV file is consumed (shared) by threads.
   *
   * @param shareMode specifies the way threads consume information from the CSV file. By default,
   *                  all threads share the CSV information, meaning that any thread iteration will
   *                  advance the consumption of the file (the file is a singleton). When {@link
   *                  #randomOrder()} is used, THREAD_GROUP shared mode is not supported.
   * @return the DslCsvDataSet for further configuration.
   * @see Sharing
   */
  public DslCsvDataSet sharedIn(Sharing shareMode) {
    this.shareMode = shareMode;
    return this;
  }

  /**
   * Specifies to get file lines in random order instead of sequentially iterating over them.
   * <p>
   * When this method is invoked <a href="https://github.com/Blazemeter/jmeter-bzm-plugins/blob/master/random-csv-data-set/RandomCSVDataSetConfig.md">Random
   * CSV Data Set plugin</a> is used.
   *
   * <b>Warning:</b> Getting lines in random order has a performance penalty.
   *
   * <b>Warning:</b> When random order is enabled, share mode THREAD_GROUP is not supported.
   *
   * @return the DslCsvDataSet for further configuration.
   * @since 0.36
   */
  public DslCsvDataSet randomOrder() {
    this.randomOrder = true;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    return randomOrder ? buildRandomCsvDataSet() : buildSimpleCsvDataSet();
  }

  private TestElement buildRandomCsvDataSet() {
    guiClass = RandomCSVDataSetConfigGui.class;
    RandomCSVDataSetConfig ret = new RandomCSVDataSetConfig();
    ret.setFilename(file);
    ret.setDelimiter(delimiter);
    ret.setFileEncoding(encoding);
    if (variableNames != null) {
      ret.setVariableNames(buildVariablesPropertyValue());
    }
    ret.setIgnoreFirstLine(ignoreFirstLine);
    if (shareMode == Sharing.THREAD_GROUP) {
      throw new IllegalStateException(
          "CSV data sets with random order, don't support THREAD_GROUP sharing mode");
    }
    ret.setIndependentListPerThread(shareMode == Sharing.THREAD);
    ret.setRewindOnTheEndOfList(!stopThread);
    ret.setRandomOrder(randomOrder);
    return ret;
  }

  private String buildVariablesPropertyValue() {
    return String.join(",", variableNames);
  }

  private CSVDataSet buildSimpleCsvDataSet() {
    CSVDataSet ret = new CSVDataSet();
    ret.setFilename(file);
    ret.setDelimiter(delimiter);
    ret.setFileEncoding(encoding);
    if (variableNames != null) {
      ret.setVariableNames(buildVariablesPropertyValue());
    }
    ret.setIgnoreFirstLine(ignoreFirstLine);
    ret.setQuotedData(true);
    ret.setRecycle(!stopThread);
    ret.setStopThread(stopThread);
    ret.setShareMode(shareMode.jmeterPropertyValue);
    return ret;
  }

  /**
   * Specifies the way the threads in a test plan consume the CSV.
   */
  public enum Sharing {
    /**
     * All threads in the test plan will share the CSV file, meaning that any thread iteration will
     * consume an entry from it. You can think as having only one pointer to the current line of the
     * CSV, being advanced by any thread iteration. The file is only opened once.
     */
    ALL_THREADS("all"),
    /**
     * CSV file consumption is only shared within thread groups. This means that threads in separate
     * thread groups will use separate indexes to consume the data. The file is open once per thread
     * group.
     */
    THREAD_GROUP("group"),
    /**
     * CSV file consumption is isolated per thread. This means that each thread will start consuming
     * the CSV from the beginning and not share any information with other threads. The file is open
     * once per thread.
     */
    THREAD("thread");

    private final String jmeterPropertyValue;

    Sharing(String jmeterPropertySuffix) {
      this.jmeterPropertyValue = "shareMode." + jmeterPropertySuffix;
    }

  }

}
