package us.abstracta.jmeter.javadsl.csvrandom;

import com.blazemeter.jmeter.RandomCSVDataSetConfig;
import com.blazemeter.jmeter.RandomCSVDataSetConfigGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;
import us.abstracta.jmeter.javadsl.core.testelements.MultiLevelTestElement;

/**
 * The Random CSV Data Set Config plugin provides the capability to parameterize your Test Plan
 * from a CSV file in random order of records
 * <p>
 * This element uses
 * <a href="https://github.com/Blazemeter/jmeter-bzm-plugins/blob/master/random-csv-data-set/RandomCSVDataSetConfig.md">
 * Random CSV Data Set Config</a>, check its documentation for more details.
 * <p>
 * By default, this element execute with next params: file encoding is 'UTF-8', delimeter is ',', random order is true,
 * rewind on the end of list is true and finally ignore first line is true. The defaults came from JMeter.
 *
 * @since 0.34
 */
public class RandomCsvDataSetConfig extends BaseTestElement implements MultiLevelTestElement {

    private final String filename;
    private String fileEncoding = "UTF-8";
    private String delimiter = ",";
    private String[] variableNames;

    private boolean ignoreFirstLine = false;
    private boolean randomOrder = true;
    private boolean rewindOnTheEndOfList = true;
    private boolean independentListPerThread = false;


    public RandomCsvDataSetConfig(String csvFile) {
        super("bzm - Random CSV Data Set Config", RandomCSVDataSetConfigGui.class);
        this.filename = csvFile;
    }

    /**
     * Builds a RandomCsvDataSetConfig.
     *
     * @param csvFile file path string to push file into data set.
     * @return the RandomCsvDataSetConfig for additional configuration and usage.
     */
    public static RandomCsvDataSetConfig csvRandomDataSet(String csvFile) {
        return new RandomCsvDataSetConfig(csvFile);
    }

    /**
     * Specifies the delimiter used by the file to separate variable values.
     *
     * @param delimiter specifies the delimiter. By default, it uses commas (,) as delimiters. If you
     *                  need to use tabs, then specify "\\t".
     * @return the RandomCsvDataSetConfig for further configuration.
     */
    public RandomCsvDataSetConfig delimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }


    /**
     * Specifies the file encoding used by the file.
     *
     * @param fileEncoding the file encoding of the file. By default it will use the system default one,
     *                 but in some scenarios this might need to be changed. In general is good to have all files in
     *                 same encoding (eg: UTF_8).
     * @return the RandomCsvDataSetConfig for further configuration.
     */
    public RandomCsvDataSetConfig encoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
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
     * @return the RandomCsvDataSetConfig for further configuration.
     */
    public RandomCsvDataSetConfig variableNames(String... variableNames) {
        this.variableNames = variableNames;
        return this;
    }

    /**
     * Specifies to ignore first line of the CSV.
     * <p>
     * This should only be used in conjunction with {@link #variableNames(String...)} to overwrite
     * existing CSV headers names.
     * <p>
     * Default is disable.
     *
     * @return the RandomCsvDataSetConfig for further configuration.
     */
    public RandomCsvDataSetConfig ignoreFirstLine() {
        this.ignoreFirstLine = true;
        return this;
    }

    /**
     * Specifies random or sequential reading of a file.
     * <p>
     * For sequential reading, you can also use the standard item 'csvDataSet'
     * <p>
     * Default is enable.
     *
     * @return the RandomCsvDataSetConfig for further configuration.
     */
    public RandomCsvDataSetConfig randomOrder() {
        this.randomOrder = true;
        return this;
    }

    /**
     * If the flag is selected and an iteration loop has reached the end,
     * the new loop will be started.
     * <p>
     * Default is enable.
     *
     * @return the RandomCsvDataSetConfig for further configuration.
     */
    public RandomCsvDataSetConfig rewindOnTheEndOfList() {
        this.rewindOnTheEndOfList = true;
        return this;
    }

    /**
     * When this is checked with “Random order”, each thread runs its own copy of CSV values with random order.
     * When unchecked, all of threads go over the same randomized list of values.
     * <p>
     * Default is disable.
     *
     * @return the RandomCsvDataSetConfig for further configuration.
     */
    public RandomCsvDataSetConfig independentListPerThread() {
        this.independentListPerThread = true;
        return this;
    }

    @Override
    protected TestElement buildTestElement() {
        RandomCSVDataSetConfig ret = new RandomCSVDataSetConfig();
        ret.setFilename(filename);
        ret.setDelimiter(delimiter);
        ret.setFileEncoding(fileEncoding);
        if (variableNames != null) {
            ret.setVariableNames(String.join(",", variableNames));
        }
        ret.setIgnoreFirstLine(ignoreFirstLine);
        ret.setIndependentListPerThread(independentListPerThread);
        ret.setRewindOnTheEndOfList(rewindOnTheEndOfList);
        ret.setRandomOrder(randomOrder);
        return ret;
    }

}
