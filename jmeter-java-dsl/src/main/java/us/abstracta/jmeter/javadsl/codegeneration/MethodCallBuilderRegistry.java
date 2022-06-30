package us.abstracta.jmeter.javadsl.codegeneration;

import java.util.Optional;

public interface MethodCallBuilderRegistry {

  <T extends MethodCallBuilder> T findBuilderByClass(Class<T> builderClass);

  Optional<MethodCallBuilder> findBuilderMatchingContext(MethodCallContext methodCallContext);

}
