package us.abstracta.jmeter.javadsl.core.engines;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import javax.swing.WindowConstants;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.MainFrame;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.Load;
import org.apache.jmeter.gui.action.LookAndFeelCommand;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.plugin.PluginManager;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.gui.JMeterUIDefaults;
import org.apache.jorphan.gui.ui.KerningOptimizer;

/**
 * Displays JMeter GUI in current JVM.
 * <p>
 * This class allows opening JMeter GUI using existing classpath (no need to copy jars to libs
 * folder) with an existing plan and much faster than opening JMeter standalone GUI and loading a
 * JMX plan.
 * <p>
 * This can be helpful mainly for JMeter users migrating to DSL usage, to review DSL generated test
 * plan, debug, run and visualize test plan with known GUI.
 *
 * @since 0.48
 */
/* Most of this logic has been extracted and adapted from org.apache.jmeter.JMeter class */
public class JmeterGui {

  private final CountDownLatch closeLatch = new CountDownLatch(1);

  public JmeterGui() throws IOException {
    loadUiResources();
    initLookAndFeel();
    openFrame();
  }

  private void initLookAndFeel() {
    KerningOptimizer.INSTANCE.setMaxTextLengthWithKerning(
        JMeterUtils.getPropDefault("text.kerning.max_document_size", 10000));
    JMeterUIDefaults.INSTANCE.install();
    String jMeterLaf = LookAndFeelCommand.getPreferredLafCommand();
    LookAndFeelCommand.activateLookAndFeel(jMeterLaf);
    JMeterUtils.applyHiDPIOnFonts();
  }

  private void loadUiResources() {
    PluginManager.install(new JMeter(), true);
  }

  private void openFrame() {
    JMeterTreeModel treeModel = new JMeterTreeModel();
    JMeterTreeListener treeListener = new JMeterTreeListener(treeModel);
    ActionRouter actionRouter = ActionRouter.getInstance();
    actionRouter.populateCommandMap();
    treeListener.setActionHandler(actionRouter);
    GuiPackage.initInstance(treeListener, treeModel);
    MainFrame frame = new MainFrame(treeModel, treeListener);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        super.windowClosed(e);
        closeLatch.countDown();
      }
    });
    ComponentUtil.centerComponentInWindow(frame, 80);
    frame.setVisible(true);
    frame.toFront();
    actionRouter.actionPerformed(new ActionEvent(frame, 1, ActionNames.ADD_ALL));
  }

  public void load(HashTree tree) throws IllegalUserActionException {
    Load.insertLoadedTree(1, tree);
  }

  public void awaitClose() throws InterruptedException {
    closeLatch.await();
  }

}
