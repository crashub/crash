package org.crsh.cmdline;

import org.crsh.cmdline.spi.ValueCompletion;

import java.io.Serializable;

/**
 * A completion result.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class CommandCompletion implements Serializable {

  /** . */
  private final Delimiter delimiter;

  /** . */
  private final ValueCompletion value;

  public CommandCompletion(Delimiter delimiter, ValueCompletion value) throws NullPointerException {
    if (delimiter == null) {
      throw new NullPointerException("No null delimiter accepted");
    }
    if (value == null) {
      throw new NullPointerException("No null value accepted");
    }

    //
    this.delimiter = delimiter;
    this.value = value;
  }

  public Delimiter getDelimiter() {
    return delimiter;
  }

  public ValueCompletion getValue() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof CommandCompletion) {
      CommandCompletion that = (CommandCompletion)obj;
      return delimiter.equals(that.delimiter) && value.equals(that.value);
    }
    return false;
  }

  @Override
  public String toString() {
    return "CommandCompletion[delimiter=" + delimiter + ",value=" + value + "]";
  }
}
