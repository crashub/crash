package org.crsh.text;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface ShellAppendable extends Appendable {

  ShellAppendable append(Style style);

  boolean isEmpty();

  ShellAppendable cls();

}
