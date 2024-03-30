package us.abstracta.jmeter.javadsl.codegeneration;

import java.util.ArrayList;
import java.util.List;

public class MethodChainHandler {
  private final List<CodeSegment> chain = new ArrayList<>();

  public void add(CodeSegment segment) {
    chain.add(segment);
  }

  public void clear() {
    chain.clear();
  }

  public int size() {
    return chain.size();
  }

  public String buildChainedCode(String indent) {
    StringBuilder ret = new StringBuilder();
    for (CodeSegment seg : chain) {
      String segCode = seg.buildCode(indent);
      if (!segCode.isEmpty()) {
        ret.append("\n")
            .append(indent)
            .append(seg instanceof MethodCall ? "." : "")
            .append(segCode);
      }
    }
    return ret.toString();
  }
}
