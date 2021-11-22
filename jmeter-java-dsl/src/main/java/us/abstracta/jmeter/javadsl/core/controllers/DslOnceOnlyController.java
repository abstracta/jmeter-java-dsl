package us.abstracta.jmeter.javadsl.core.controllers;

import org.apache.jmeter.control.OnceOnlyController;
import org.apache.jmeter.control.gui.OnceOnlyControllerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup;

import java.util.List;

/**
 * Allows running a part of a test plan only once and only on the first iteration of each thread group.
 *
 * Internally this uses JMeter Once Only Controller.
 *
 * @since 0.34
 */


public class DslOnceOnlyController extends DslController{

    public DslOnceOnlyController(List<BaseThreadGroup.ThreadGroupChild> children) {
        super("Once Only Controller", OnceOnlyControllerGui.class, children);
    }

    @Override
    protected TestElement buildTestElement() {
        return new OnceOnlyController();
    }
}
