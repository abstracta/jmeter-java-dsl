package us.abstracta.jmeter.javadsl.core.controllers;

import java.util.List;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.control.gui.TransactionControllerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;

/**
 * Allows specifying JMeter transaction controllers which group different samples associated to same
 * transaction.
 *
 * This is usually used when grouping different steps of a flow, for example group requests of login
 * flow, adding item to cart, purchase, etc. It provides aggregate metrics of all it's samples.
 *
 * @since 0.14
 */
public class DslTransactionController extends DslController {

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
