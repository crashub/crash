package org.crsh.text;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TextChunk extends Chunk {

  /** . */
  final StringBuilder buffer;

  public TextChunk() {
    this.buffer = new StringBuilder();
  }

  public TextChunk(CharSequence s) {
    this.buffer = new StringBuilder().append(s);
  }

  public CharSequence getText() {
    return buffer;
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this || obj instanceof TextChunk && buffer.toString().equals(((TextChunk)obj).buffer.toString());
  }

  @Override
  public String toString() {
    return "TextChunk[" + buffer + "]";
  }
}
