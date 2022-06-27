package us.abstracta.jmeter.javadsl.codegeneration.params;

/**
 * Is a parameter associated to a test element name.
 * <p>
 * This type of parameter has the special consideration that when names are set to default values,
 * then they can be ignored.
 *
 * @since 0.45
 */
public class NameParam extends StringParam {

  public NameParam(String name, String defaultName) {
    super(name, defaultName);
  }

  @Override
  public boolean isIgnored() {
    return isDefault();
  }

}
