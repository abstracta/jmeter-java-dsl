package us.abstracta.jmeter.javadsl.bridge.serialization;

import java.lang.reflect.Parameter;

public interface BuilderMethod {

  Object invoke(Object... args) throws ReflectiveOperationException;

  Parameter[] getParameters();

}
