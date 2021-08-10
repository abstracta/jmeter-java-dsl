package us.abstracta.jmeter.javadsl.core;

import org.apache.jorphan.collections.HashTree;

/**
 * Interface to be implemented by all elements composing a JMeter test plan.
 *
 * @since 0.1
 */
public interface DslTestElement {

  /**
   * Builds the JMeter HashTree for this TestElement under the provided tree node.
   *
   * @param parent the node which will be the parent for the created tree.
   * @param context context information which contains information shared by elements while building
   * the test plan tree (eg: adding additional items to test plan when a particular protocol element
   * is added).
   * @return The tree created under the parent node.
   * @since 0.17
   */
  HashTree buildTreeUnder(HashTree parent, BuildTreeContext context);

  /**
   * Shows the test element in it's defined GUI in a popup window.
   *
   * This might be handy to visualize the element as it looks in JMeter GUI.
   *
   * @since 0.18
   */
  void showInGui();

}
