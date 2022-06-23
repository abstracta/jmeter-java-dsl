package us.abstracta.jmeter.javadsl.wrapper.customelements;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;

public class MyAssertion extends AbstractTestElement implements Assertion {

  @Override
  public AssertionResult getResult(SampleResult response) {
    return new AssertionResult("MyAssertion");
  }

}
