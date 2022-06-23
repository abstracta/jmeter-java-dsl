package us.abstracta.jmeter.javadsl.wrapper.customelements;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jorphan.collections.ListedHashTree;

public class MyThreadGroup extends AbstractThreadGroup {

  @Override
  public boolean stopThread(String threadName, boolean now) {
    return true;
  }

  @Override
  public int numberOfActiveThreads() {
    return 0;
  }

  @Override
  public void start(int groupCount, ListenerNotifier notifier,
      ListedHashTree threadGroupTree, StandardJMeterEngine engine) {
  }

  @Override
  public JMeterThread addNewThread(int delay, StandardJMeterEngine engine) {
    return null;
  }

  @Override
  public boolean verifyThreadsStopped() {
    return true;
  }

  @Override
  public void waitThreadsStopped() {
  }

  @Override
  public void tellThreadsToStop() {
  }

  @Override
  public void stop() {
  }

  @Override
  public void threadFinished(JMeterThread thread) {
  }

}
