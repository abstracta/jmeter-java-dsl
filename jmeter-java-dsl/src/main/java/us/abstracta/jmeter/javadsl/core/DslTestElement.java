package us.abstracta.jmeter.javadsl.core;

import org.apache.jorphan.collections.HashTree;

/**
 * Interface to be implemented by all elements composing a JMeter test plan.
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
   */
  HashTree buildTreeUnder(HashTree parent, BuildTreeContext context);

}
