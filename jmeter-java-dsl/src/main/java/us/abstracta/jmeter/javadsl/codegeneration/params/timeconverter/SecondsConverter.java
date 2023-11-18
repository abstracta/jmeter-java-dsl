package us.abstracta.jmeter.javadsl.codegeneration.params.timeconverter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class SecondsConverter implements TimeConverter {

    @Override
    public long convert(Duration input) {
        return input.getSeconds();
    }

    @Override
    public ChronoUnit getOutputUnit() {
        return ChronoUnit.SECONDS;
    }
}
