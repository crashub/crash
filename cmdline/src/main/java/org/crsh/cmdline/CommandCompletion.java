package org.crsh.cmdline;

import org.crsh.cmdline.spi.ValueCompletion;

/**
 * A completion result.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class CommandCompletion {

  /** . */
  private final Termination termination;

  /** . */
  private final ValueCompletion value;

  public CommandCompletion(Termination termination, ValueCompletion value) throws NullPointerException {
    if (termination == null) {
      throw new NullPointerException("No null termination accepted");
    }
    if (value == null) {
      throw new NullPointerException("No null value accepted");
    }

    //
    this.termination = termination;
    this.value = value;
  }

  public Termination getTermination() {
    return termination;
  }

  public String getTerminationValue() {
    return termination.getEnd();
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
      return termination.equals(that.termination) && value.equals(that.value);
    }
    return false;
  }

  @Override
  public String toString() {
    return "CommandCompletion[termination=" + termination + ",value=" + value + "]";
  }
}
