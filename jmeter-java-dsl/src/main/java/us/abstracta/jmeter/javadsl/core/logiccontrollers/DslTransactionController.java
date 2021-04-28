package us.abstracta.jmeter.javadsl.core.logiccontrollers;

import java.util.List;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.control.gui.TransactionControllerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.core.TestElementContainer;

/**
 * Allows specifying JMeter transaction controllers which group different samples associated to same
 * transaction.
 *
 * This is usually used when grouping different steps of a flow, for example group requests of login
 * flow, adding item to cart, purchase, etc. It provides aggregate metrics of all it's samples.
 */
public class DslTransactionController extends TestElementContainer<ThreadGroupChild> implements
    ThreadGroupChild {

  public DslTransactionController(String name, List<ThreadGroupChild> children) {
    super(name, TransactionControllerGui.class, children);
  }

  @Override
  protected TestElement buildTestElement() {
    TransactionController ret = new TransactionController();
    ret.setIncludeTimers(false);
    return ret;
  }

}
