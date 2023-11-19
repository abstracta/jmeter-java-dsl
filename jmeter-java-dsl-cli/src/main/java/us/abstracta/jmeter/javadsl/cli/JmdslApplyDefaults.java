package us.abstracta.jmeter.javadsl.cli;

import com.fasterxml.jackson.annotation.JsonIgnore;
import picocli.CommandLine;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class JmdslApplyDefaults {
    protected static void applyDefaultsFromTo(Object defaults, Object target) {
        Arrays.stream(target.getClass().getDeclaredFields())
                .filter(f -> !f.isAnnotationPresent(JsonIgnore.class)
                        && (f.isAnnotationPresent(CommandLine.Option.class) || f.isAnnotationPresent(CommandLine.Parameters.class)))
                .filter(f -> {
                    Object prevVal = JmdslApplyDefaults.getField(f, target);
                    String defaultValue = f.isAnnotationPresent(CommandLine.Option.class)
                            ? f.getAnnotation(CommandLine.Option.class).defaultValue()
                            : f.getAnnotation(CommandLine.Parameters.class).defaultValue();
                    if (JmdslConfig.PICOCLI_NO_DEFAULT_VALUE_MARKER.equals(defaultValue)) {
                        defaultValue = null;
                    }
                    return prevVal == null
                            || defaultValue != null && defaultValue.equals(prevVal.toString())
                            || prevVal instanceof Boolean && !(Boolean) prevVal
                            || prevVal instanceof List && (((List) prevVal).isEmpty());
                })
                .forEach(f -> JmdslApplyDefaults.setField(f, target, JmdslApplyDefaults.getField(f, defaults)));
        Arrays.stream(target.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(CommandLine.ArgGroup.class))
                .forEach(f -> JmdslApplyDefaults.applyDefaultsFromTo(JmdslApplyDefaults.getField(f, defaults), JmdslApplyDefaults.getField(f, target)));
    }

    private static Object getField(Field field, Object instance) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(Field field, Object instance, Object value) {
        // keep with default field value if value to set is null
        if (value == null) {
            return;
        }
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
