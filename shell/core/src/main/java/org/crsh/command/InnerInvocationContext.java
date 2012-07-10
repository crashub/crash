package org.crsh.command;

import org.crsh.text.ShellPrintWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class InnerInvocationContext<P> implements InvocationContext<Void, P> {

  /** . */
  final InvocationContext<?, ?> outter;

  /** . */
  final Class<? extends P> producedType;

  /** . */
  List<P> products;

  /** . */
  final boolean piped;

  InnerInvocationContext(
    InvocationContext<?, ?> outter,
    Class<? extends P> producedType,
    boolean piped) {
    this.outter = outter;
    this.products = Collections.emptyList();
    this.producedType = producedType;
    this.piped = piped;
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

  public ShellPrintWriter getWriter() {
    return outter.getWriter();
  }

  public boolean isPiped() {
    return piped;
  }

  public Iterable<Void> consume() throws IllegalStateException {
    throw new IllegalStateException();
  }

  public void produce(P product) {
    if (products.isEmpty()) {
      products = new ArrayList<P>();
    }
    products.add(product);
  }

  public Map<String, Object> getSession() {
    return outter.getSession();
  }

  public Map<String, Object> getAttributes() {
    return outter.getAttributes();
  }
}
