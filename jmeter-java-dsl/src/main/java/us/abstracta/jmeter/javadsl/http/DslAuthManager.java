package us.abstracta.jmeter.javadsl.http;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.AuthManager.Mechanism;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.gui.AuthPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;
import us.abstracta.jmeter.javadsl.core.configs.BaseConfigElement;

/**
 * Allows specifying HTTP authentication to be automatically included in HTTP requests.
 * <p>
 * This is a handy way of specifying HTTP authentication without having to worry about particular
 * requests or HTTP header handling.
 * <p>
 * When specifying multiple authentications, consider that the first matching authentication will be
 * applied. So, if you have two urls with different authentication which one is contained by the
 * other, define the longest one first and then the shorter one to get expected behavior. E.g:
 *
 * <pre>{@code
 *  httpAuth()
 *    .basicAuth("http://myservice/user", "user1", "pass1")
 *    .basicAuth("http://myservice", "user2", "pass2")
 * }</pre>
 *
 * @since 1.5
 */
public class DslAuthManager extends BaseConfigElement {

  private final List<Authorization> authorizations = new ArrayList<>();

  public DslAuthManager() {
    super("HTTP Authorization Manager", AuthPanel.class);
  }

  /**
   * Specifies authentication credentials to use with HTTP Basic authentication for a given base
   * url.
   *
   * @param baseUrl  allows filtering requests to which HTTP Basic authentication should be sent.
   *                 All requests with a URL starting with this given base url will include Basic
   *                 authentication.
   *                 <p>
   *                 Even though you can use empty string to match any URL and simplify
   *                 configuration, this is not advised since it may lead to sending the
   *                 authentication credentials to an unexpected URL and cause a security leak.
   * @param user specifies the username for the basic authentication.
   * @param password specifies the password for the basic authentication.
   * @return the test element for further configuration and usage.
   */
  public DslAuthManager basicAuth(String baseUrl, String user, String password) {
    Authorization auth = new Authorization();
    auth.setURL(baseUrl);
    auth.setUser(user);
    auth.setPass(password);
    authorizations.add(auth);
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    AuthManager ret = new AuthManager();
    ret.setClearEachIteration(true);
    authorizations.forEach(ret::addAuth);
    return ret;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<AuthManager> {

    public CodeBuilder(List<Method> builderMethods) {
      super(AuthManager.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(AuthManager testElement, MethodCallContext context) {
      MethodCall ret = buildMethodCall();
      CollectionProperty auths = testElement.getAuthObjects();
      for (JMeterProperty authProp : auths) {
        chainAuth((Authorization) authProp.getObjectValue(), ret);
      }
      return ret;
    }

    private void chainAuth(Authorization auth, MethodCall ret) {
      if (auth.getMechanism() == Mechanism.BASIC) {
        ret.chainComment(
                "TODO including passwords in code repositories may lead to security leaks. "
                    + "Review generated code and consider externalizing any credentials. "
                    + "Eg: System.getenv(\"AUTH_PASSWORD\")")
            .chain("basicAuth", new StringParam(auth.getURL()), new StringParam(auth.getUser()),
                new StringParam(auth.getPass()));
      } else {
        /*
         This will generate a method that will not compile. We consider it a good way to report to
         the user about something that is still not supported, so they can report an issue in
         the repository, and we can accordingly prioritize and later support
         */
        ret.chain(new MethodCall(auth.getMechanism().name().toLowerCase(Locale.US) + "Auth",
            DslAuthManager.class, new StringParam(auth.getURL()),
            new StringParam(auth.getUser())));
      }
    }
  }

}
