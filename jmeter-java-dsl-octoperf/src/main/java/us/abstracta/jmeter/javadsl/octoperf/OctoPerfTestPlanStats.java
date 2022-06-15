package us.abstracta.jmeter.javadsl.octoperf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.CSVSaveService;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.engines.EmbeddedStatsSummary;
import us.abstracta.jmeter.javadsl.octoperf.api.BenchResult;

public class OctoPerfTestPlanStats extends TestPlanStats {

  private static final char DELIMITER = ',';

  public OctoPerfTestPlanStats(BenchResult result) {
    super(EmbeddedStatsSummary::new);
    setStart(result.getCreated());
    setEnd(result.getLastModified());
  }

  public void loadJtlFile(InputStream jtlFileContents) throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(jtlFileContents))) {
      // skipping headers line
      reader.readLine();
      String[] fields = CSVSaveService.csvReadFile(reader, DELIMITER);
      while (fields.length != 0) {
        int fieldIndex = 0;
        SampleResult result = new SampleResult();
        long timeStampEpoch = Long.parseLong(fields[fieldIndex++]);
        result.setStampAndTime(timeStampEpoch, Long.parseLong(fields[fieldIndex++]));
        result.setSampleLabel(fields[fieldIndex++]);
        result.setSuccessful(Boolean.parseBoolean(fields[fieldIndex++]));
        result.setBytes(Long.parseLong(fields[fieldIndex++]));
        result.setBodySize(Long.parseLong(fields[fieldIndex]));
        addSampleResult(result);
        fields = CSVSaveService.csvReadFile(reader, DELIMITER);
      }
    }
  }

}
