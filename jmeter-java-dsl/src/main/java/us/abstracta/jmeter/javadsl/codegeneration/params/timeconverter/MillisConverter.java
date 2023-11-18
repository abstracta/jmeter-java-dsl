package us.abstracta.jmeter.javadsl.codegeneration.params.timeconverter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class MillisConverter implements TimeConverter {
    @Override
    public long convert(Duration input) {
        return input.toMillis();
    }
    @Override
    public ChronoUnit getOutputUnit() {
        return ChronoUnit.MILLIS;
    }
}
