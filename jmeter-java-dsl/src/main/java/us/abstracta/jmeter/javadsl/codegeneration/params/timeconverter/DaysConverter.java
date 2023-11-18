package us.abstracta.jmeter.javadsl.codegeneration.params.timeconverter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class DaysConverter implements TimeConverter {
    @Override
    public long convert(Duration input) {
        return input.toDays();
    }

    @Override
    public ChronoUnit getOutputUnit() {
        return ChronoUnit.DAYS;
    }
}
