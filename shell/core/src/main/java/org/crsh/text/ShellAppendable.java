package org.crsh.text;

import org.crsh.shell.io.ShellWriter;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface ShellAppendable extends Appendable {

  ShellWriter append(Style style);

  boolean isEmpty();

}
