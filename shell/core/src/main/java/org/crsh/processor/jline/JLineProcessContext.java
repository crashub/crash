/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.crsh.processor.jline;

import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.text.Chunk;
import org.crsh.text.Style;
import org.crsh.text.Text;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

class JLineProcessContext implements ShellProcessContext {

  /** . */
  private static final Character NO_ECHO = (char)0;

  /** . */
  final JLineProcessor processor;

  /** . */
  final CountDownLatch latch = new CountDownLatch(1);

  /** . */
  final AtomicReference<ShellResponse> resp = new AtomicReference<ShellResponse>();

  public JLineProcessContext(JLineProcessor processor) {
    this.processor = processor;
  }

  public boolean takeAlternateBuffer() {
    if (!processor.useAlternate) {
      processor.useAlternate = true;

      // To get those codes I captured the output of telnet running top
      // on OSX:
      // 1/ sudo /usr/libexec/telnetd -debug #run telnet
      // 2/ telnet localhost >output.txt
      // 3/ type username + enter
      // 4/ type password + enter
      // 5/ type top + enter
      // 6/ ctrl-c
      // 7/ type exit + enter

      // Save screen and erase
      processor.writer.print("\033[?47h"); // Switches to the alternate screen
      // writer.print("\033[1;43r");
//      processor.writer.print("\033[m"); // Reset to normal (Sets SGR parameters : 0 m == m)
      // writer.print("\033[4l");
      // writer.print("\033[?1h");
      // writer.print("\033[=");
//      processor.writer.print("\033[H"); // Move the cursor to home
//      processor.writer.print("\033[2J"); // Clear screen
//      processor.writer.flush();
    }
    return true;
  }

  public boolean releaseAlternateBuffer() {
    if (processor.useAlternate) {
      processor.useAlternate = false;
      processor.writer.print("\033[?47l"); // Switches back to the normal screen
    }
    return true;
  }

  public int getWidth() {
    return processor.reader.getTerminal().getWidth();
  }

  public int getHeight() {
    return processor.reader.getTerminal().getHeight();
  }

  public String getProperty(String name) {
    return null;
  }

  public String readLine(String msg, boolean echo) {
    try {
      if (echo) {
        return processor.reader.readLine(msg);
      } else {
        return processor.reader.readLine(msg, NO_ECHO);
      }
    }
    catch (IOException e) {
      return null;
    }
  }

  public Class<Chunk> getConsumedType() {
    return Chunk.class;
  }

  public void write(Chunk chunk) throws IOException {
    provide(chunk);
  }

  public void provide(Chunk element) throws IOException {
    if (element instanceof Text) {
      Text textChunk = (Text)element;
      processor.writer.append(textChunk.getText());
    } else if (element instanceof Style) {
      try {
        ((Style)element).writeAnsiTo(processor.writer);
      }
      catch (IOException ignore) {
      }
    } else {


      // Clear screen
      processor.writer.print("\033[2J");
      processor.writer.print("\033[1;1H");
    }
  }

  public void flush() {
    processor.writer.flush();
  }

  public void end(ShellResponse response) {
    try {
      resp.set(response);
      latch.countDown();
    }
    finally {
      // Release screen if it wasn't done
      releaseAlternateBuffer();
    }
  }
}
