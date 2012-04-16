package org.crsh.command;

import java.io.PrintWriter;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class InnerInvocationContext implements InvocationContext<Void, Void> {

  /** . */
  final InvocationContext<?, ?> outter;

  InnerInvocationContext(InvocationContext<?, ?> outter) {
    this.outter = outter;
  }

  public int getWidth() {
    return outter.getWidth();
  }

  public String getProperty(String propertyName) {
    return outter.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) {
    return outter.readLine(msg, echo);
  }

  public PrintWriter getWriter() {
    return outter.getWriter();
  }

  public boolean isPiped() {
    return false;
  }

  public Iterable<Void> consume() throws IllegalStateException {
    throw new IllegalStateException();
  }

  public void produce(Void product) {
    throw new IllegalStateException();
  }

  public Map<String, Object> getAttributes() {
    return outter.getAttributes();
  }
}
