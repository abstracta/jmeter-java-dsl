package us.abstracta.jmeter.javadsl.codegeneration.params.timeconverter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public interface TimeConverter {
    long convert(Duration input);


    ChronoUnit getOutputUnit();
}
