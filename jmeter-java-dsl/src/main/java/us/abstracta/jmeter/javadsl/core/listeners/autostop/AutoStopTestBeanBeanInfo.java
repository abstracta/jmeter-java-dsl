package us.abstracta.jmeter.javadsl.core.listeners.autostop;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.util.JMeterUtils;

public class AutoStopTestBeanBeanInfo extends BeanInfoSupport {

  public AutoStopTestBeanBeanInfo() {
    super(AutoStopTestBean.class);
    PropertyDescriptor p = property("conditions");
    p.setPropertyEditorClass(TableEditor.class);
    p.setValue(TableEditor.CLASSNAME, AutoStopConditionElement.class.getName());
    String[] props = new String[]{"regex", "metric", "aggregation", "percentile",
        "aggregationResetPeriodSeconds", "comparison", "value", "holdsForSeconds"};
    p.setValue(TableEditor.HEADERS, Arrays.stream(props)
        .map(prop -> JMeterUtils.getResString("autostop_" + prop))
        .toArray(String[]::new));
    p.setValue(TableEditor.OBJECT_PROPERTIES, props);
    p.setValue(NOT_UNDEFINED, Boolean.TRUE);
    p.setValue(DEFAULT, new ArrayList<>());
    p.setValue(MULTILINE, Boolean.TRUE);
    property("regex");
    //cannot mark this property as hidden since otherwise it wouldn't be serialized
    property("testStopper");
  }

}
