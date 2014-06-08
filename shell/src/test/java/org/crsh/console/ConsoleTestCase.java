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
package org.crsh.console;

import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.spi.Completion;
import org.crsh.keyboard.KeyHandler;
import org.crsh.keyboard.KeyType;
import test.shell.sync.SyncCompleter;
import test.shell.sync.SyncProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Julien Viet
 */
public class ConsoleTestCase extends AbstractConsoleTestCase {

  public void testEditor() {
    console.init();
    console.on(KeyStrokes.a);
    driver.assertChar('a').assertFlush().assertEmpty();
  }

  public void testInsert() {
    console.init();
    console.on(KeyStrokes.a);
    driver.assertChar('a').assertFlush().assertEmpty();
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.b);
    driver.assertMoveLeft().assertFlush().assertChars("ba").assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.b);
    driver.assertMoveLeft().assertFlush().assertChars("bba").assertMoveLeft().assertMoveLeft().assertFlush().assertEmpty();
    assertEquals("bba", getCurrentLine());
  }

  public void testDeletePrevChar() {
    console.init();
    console.on(KeyStrokes.DELETE_PREV_CHAR);
    driver.assertEmpty();
    console.on(KeyStrokes.a);
    driver.assertChar('a').assertFlush().assertEmpty();
    console.on(KeyStrokes.DELETE_PREV_CHAR);
    driver.assertDel().assertFlush().assertEmpty();
  }

  public void testDeleteNextChar() {
    console.init();
    console.on(KeyStrokes.DELETE_NEXT_CHAR);
    driver.assertEmpty();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.b);
    driver.assertChar('a').assertFlush();
    driver.assertChar('b').assertFlush().assertEmpty();
    console.on(KeyStrokes.DELETE_NEXT_CHAR);
    driver.assertEmpty();
    console.on(KeyStrokes.LEFT);
    driver.assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.LEFT);
    driver.assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.DELETE_NEXT_CHAR);
    driver.assertMoveRight().assertMoveLeft().assertChar('b').assertChar(' ').assertMoveLeft().assertMoveLeft().assertFlush().assertEmpty();
    assertEquals("b", getCurrentLine());
    console.on(KeyStrokes.DELETE_NEXT_CHAR);
    driver.assertMoveRight().assertDel().assertFlush().assertEmpty();
    assertEquals("", getCurrentLine());
  }

  public void testDeleteEnd() {
    console.init();
    console.on(KeyStrokes.DELETE_END);
    driver.assertEmpty();
    assertEquals("", getClipboard());
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.b);
    console.on(KeyStrokes.c);
    driver.assertChar('a').assertFlush();
    driver.assertChar('b').assertFlush();
    driver.assertChar('c').assertFlush().assertEmpty();
    console.on(KeyStrokes.LEFT);
    driver.assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.LEFT);
    driver.assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.DELETE_END);
    driver.assertMoveRight().assertMoveRight().assertDel().assertDel().assertFlush().assertEmpty();
    assertEquals("a", getCurrentLine());
    assertEquals("bc", getClipboard());
  }

  public void testDeleteBeginning() {
    console.init();
    console.on(KeyStrokes.DELETE_BEGINNING);
    driver.assertEmpty();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.b);
    console.on(KeyStrokes.c);
    driver.assertChar('a').assertFlush();
    driver.assertChar('b').assertFlush();
    driver.assertChar('c').assertFlush().assertEmpty();
    console.on(KeyStrokes.LEFT);
    driver.assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.DELETE_BEGINNING);
    assertEquals("c", getCurrentLine());
    driver.assertMoveLeft().assertChars("c ").assertMoveLeft().assertMoveLeft().assertMoveLeft().assertChars("c ").assertMoveLeft().assertMoveLeft().assertFlush().assertEmpty();
  }

/*
  public void testDeleteNextWord() {
    console.init();
    console.on(KeyEvents.DELETE_NEXT_WORD);
    driver.assertEmpty();
    assertEquals("", getClipboard());
    console.on(KeyEvents.a, KeyEvents.b, KeyEvents.SPACE, KeyEvents.c, KeyEvents.d);
    driver.assertChar('a').assertFlush().assertChar('b').assertFlush().assertChar(' ').assertFlush().assertChar('c').assertFlush().assertChar('d').assertFlush().assertEmpty();
    console.on(KeyEvents.LEFT, KeyEvents.LEFT, KeyEvents.LEFT, KeyEvents.LEFT);
    driver.assertMoveLeft().assertFlush().assertMoveLeft().assertFlush().assertMoveLeft().assertFlush().assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyEvents.DELETE_NEXT_WORD);
    driver.assertMoveRight().assertMoveLeft().assertChars(" cd ").assertMoveLeft(4).assertFlush().assertEmpty();
    assertEquals("a cd", getCurrentLine());
    assertEquals("b", getClipboard());
    console.on(KeyEvents.DELETE_NEXT_WORD);
    driver.assertMoveRight(3).assertDel(3).assertFlush().assertEmpty();
    assertEquals("a", getCurrentLine());
    assertEquals(" cd", getClipboard());
  }
*/

  public void testDeletePrevWord() {
    console.init();
    console.on(KeyStrokes.DELETE_PREV_WORD);
    driver.assertEmpty();
    assertEquals("", getClipboard());
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.b);
    console.on(KeyStrokes.SPACE);
    console.on(KeyStrokes.c);
    console.on(KeyStrokes.d);
    driver.assertChar('a').assertFlush().assertChar('b').assertFlush().assertChar(' ').assertFlush().assertChar('c').assertFlush().assertChar('d').assertFlush().assertEmpty();
    console.on(KeyStrokes.LEFT);
    driver.assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.DELETE_PREV_WORD);
    driver.assertMoveLeft().assertChars("d ").assertMoveLeft().assertMoveLeft().assertFlush().assertEmpty();
    assertEquals("ab d", getCurrentLine());
    assertEquals("c", getClipboard());
    console.on(KeyStrokes.DELETE_PREV_WORD);
    driver.assertMoveLeft().assertChars("d ").assertMoveLeft(3).assertChars("d ").assertMoveLeft(3).assertChars("d ").assertMoveLeft(2).assertFlush().assertEmpty();
    assertEquals("d", getCurrentLine());
    assertEquals("ab ", getClipboard());
  }

/*
  public void testPasteBefore() {
    console.init();
    console.on(KeyEvents.PASTE_BEFORE);
    driver.assertEmpty();
    setClipboard("ab");
    console.on(KeyEvents.PASTE_BEFORE);
    driver.assertChar('a').assertChar('b').assertFlush().assertEmpty();
    assertEquals("ab", getCurrentLine());
    assertEquals("ab", getClipboard());
    setClipboard("cd");
    console.on(KeyEvents.LEFT, KeyEvents.LEFT);
    driver.assertMoveLeft().assertFlush().assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyEvents.PASTE_BEFORE);
    driver.assertChars("cdab").assertMoveLeft().assertMoveLeft().assertFlush().assertEmpty();
    assertEquals("cdab", getCurrentLine());
    assertEquals("cd", getClipboard());
  }
*/

  public void testMovePrevChar() {
    console.init();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.LEFT);
    driver.assertChar('a').assertFlush().assertMoveLeft().assertFlush().assertEmpty();
  }

  public void testMoveNextChar() {
    console.init();
    console.on(KeyStrokes.RIGHT);
    driver.assertEmpty();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.LEFT);
    driver.assertChar('a').assertFlush().assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.RIGHT);
    driver.assertMoveRight().assertFlush().assertEmpty();
  }

  public void testMovePrevWord() {
    console.init();
    console.on(KeyStrokes.MOVE_PREV_WORD);
    driver.assertEmpty();
    console.on(KeyStrokes.SPACE);
    driver.assertChar(' ').assertFlush().assertEmpty();
    console.on(KeyStrokes.a);
    driver.assertChar('a').assertFlush().assertEmpty();
    console.on(KeyStrokes.b);
    driver.assertChar('b').assertFlush().assertEmpty();
    console.on(KeyStrokes.MOVE_PREV_WORD);
    driver.assertMoveLeft().assertMoveLeft().assertFlush().assertEmpty();
  }

  public void testMoveNextWord() {
    console.init();
    console.on(KeyStrokes.MOVE_NEXT_WORD);
    driver.assertEmpty();
    console.on(KeyStrokes.a);
    driver.assertChar('a').assertFlush().assertEmpty();
    console.on(KeyStrokes.b);
    driver.assertChar('b').assertFlush().assertEmpty();
    console.on(KeyStrokes.SPACE);
    driver.assertChar(' ').assertFlush().assertEmpty();
    console.on(KeyStrokes.c);
    driver.assertChar('c').assertFlush().assertEmpty();
    console.on(KeyStrokes.LEFT);
    driver.assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.LEFT);
    driver.assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.LEFT);
    driver.assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.LEFT);
    driver.assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.MOVE_NEXT_WORD);
    driver.assertMoveRight().assertMoveRight().assertFlush().assertEmpty();
  }

  public void testMoveBeginning() {
    console.init();
    console.on(KeyStrokes.MOVE_BEGINNING);
    driver.assertEmpty();
    console.on(KeyStrokes.a);
    driver.assertChar('a').assertFlush().assertEmpty();
    console.on(KeyStrokes.SPACE);
    driver.assertChar(' ').assertFlush().assertEmpty();
    console.on(KeyStrokes.b);
    driver.assertChar('b').assertFlush().assertEmpty();
    console.on(KeyStrokes.MOVE_BEGINNING);
    driver.assertMoveLeft().assertMoveLeft().assertMoveLeft().assertFlush().assertEmpty();
  }

  public void testMoveEnd() {
    console.init();
    console.on(KeyStrokes.MOVE_END);
    driver.assertEmpty();
    console.on(KeyStrokes.a);
    driver.assertChar('a').assertFlush().assertEmpty();
    console.on(KeyStrokes.SPACE);
    driver.assertChar(' ').assertFlush().assertEmpty();
    console.on(KeyStrokes.b);
    driver.assertChar('b').assertFlush().assertEmpty();
    console.on(KeyStrokes.LEFT);
    driver.assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.LEFT);
    driver.assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.LEFT);
    driver.assertMoveLeft().assertFlush().assertEmpty();
    console.on(KeyStrokes.MOVE_END);
    driver.assertMoveRight().assertMoveRight().assertMoveRight().assertFlush().assertEmpty();
  }

  public void testProcess() {
    final ArrayBlockingQueue<String> requests = new ArrayBlockingQueue<String>(1);
    final ArrayBlockingQueue<ShellProcessContext> contexts = new ArrayBlockingQueue<ShellProcessContext>(1);
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        requests.add(request);
        contexts.add(context);
      }
    });
    console.init();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.ENTER);
    console.on(KeyStrokes.b);
    console.on(KeyStrokes.ENTER);
    assertEquals("a", requests.poll());
    ShellProcessContext context = contexts.poll();
    assertNotNull(context);
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        contexts.add(context);
      }
    });
    context.end(ShellResponse.ok());
    context = contexts.poll();
    assertNotNull(context);
    context.end(ShellResponse.ok());
  }

  public void testPrompt() {
    driver.assertEmpty();
    prompt = "% ";
    console.init();
    driver.assertChar('%').assertChar(' ').assertFlush().assertEmpty();
    final ArrayBlockingQueue<ShellProcessContext> contexts = new ArrayBlockingQueue<ShellProcessContext>(1);
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        contexts.add(context);
      }
    });
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.ENTER);
    driver.assertChar('a').assertFlush().assertCRLF().assertFlush().assertEmpty();
    driver.assertEmpty();
    ShellProcessContext context = contexts.poll();
    assertNotNull(context);
    context.end(ShellResponse.ok());
    driver.assertCRLF().assertFlush().assertChar('%').assertChar(' ').assertFlush().assertEmpty();
  }

  public void testHandleKeyInProcess() {
    final ArrayBlockingQueue<Integer> keys = new ArrayBlockingQueue<Integer>(1);
    shell.addProcess(new SyncProcess() {
      @Override
      protected KeyHandler createKeyHandler() {
        return new KeyHandler() {
          @Override
          public void handle(KeyType type, int[] sequence) {
            for (int c : sequence) {
              keys.add(c);
            }
          }
        };
      }
    });
    console.init();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.ENTER);
    console.on(KeyStrokes.b);
    assertEquals(1, keys.size());
    assertEquals((int)'b', (int)keys.poll());
    assertFalse(console.getKeyBuffer().iterator().hasNext());
  }

  public void testHandleKeyExceptionInProcess() {
    shell.addProcess(new SyncProcess() {
      @Override
      protected KeyHandler createKeyHandler() {
        return new KeyHandler() {
          @Override
          public void handle(KeyType type, int[] sequence) {
            throw new RuntimeException();
          }
        };
      }
    });
    console.init();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.ENTER);
    console.on(KeyStrokes.b);
  }

  public void testCompleteEmpty() {
    shell.setCompleter(new SyncCompleter() {
      @Override
      public CompletionMatch complete(String prefix) {

        return null;
      }
    });
    console.init();
    console.on(KeyStrokes.COMPLETE);
    driver.assertEmpty();
  }

  public void testCompleteSingle1() {
    shell.setCompleter(new SyncCompleter() {
      @Override
      public CompletionMatch complete(String prefix) {
        Completion.Builder builder = Completion.builder("");
        builder.add("foo", false);
        return new CompletionMatch(Delimiter.EMPTY, builder.build());
      }
    });
    console.init();
    console.on(KeyStrokes.COMPLETE);
    driver.assertChars("foo").assertFlush().assertEmpty();
    assertEquals("foo", getCurrentLine());
  }

  public void testCompleteSingle2() {
    shell.setCompleter(new SyncCompleter() {
      @Override
      public CompletionMatch complete(String prefix) {
        Completion.Builder builder = Completion.builder("");
        builder.add("foo", true);
        return new CompletionMatch(Delimiter.EMPTY, builder.build());
      }
    });
    console.init();
    console.on(KeyStrokes.COMPLETE);
    driver.assertChars("foo").assertChar(' ').assertFlush().assertEmpty();
    assertEquals("foo ", getCurrentLine());
  }

  public void testCompleteMulti1() {
    shell.setCompleter(new SyncCompleter() {
      @Override
      public CompletionMatch complete(String prefix) {
        Completion.Builder builder = Completion.builder("");
        builder.add("foo", false);
        builder.add("bar", false);
        return new CompletionMatch(Delimiter.EMPTY, builder.build());
      }
    });
    console.init();
    prompt = "% ";
    console.on(KeyStrokes.COMPLETE);
    driver.assertChars("\nfoo  bar  \n% ").assertFlush().assertEmpty();
    assertEquals("", getCurrentLine());
  }

  public void testCompleteMulti2() {
    shell.setCompleter(new SyncCompleter() {
      @Override
      public CompletionMatch complete(String prefix) {
        Completion.Builder builder = Completion.builder("");
        builder.add("afoo", false);
        builder.add("abar", false);
        return new CompletionMatch(Delimiter.EMPTY, builder.build());
      }
    });
    console.init();
    prompt = "% ";
    console.on(KeyStrokes.COMPLETE);
    driver.assertChars("\nafoo  abar  \n% a").assertFlush().assertEmpty();
    assertEquals("a", getCurrentLine());
  }

  public void testMultiline1() {
    console.init();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.BACKSLASH);
    console.on(KeyStrokes.ENTER);
    console.on(KeyStrokes.b);
    assertEquals("b", getCurrentLine());
    driver.assertChar('a').assertFlush();
    driver.assertChar('\\').assertFlush();
    driver.assertCRLF().assertChars("> ").assertFlush();
    driver.assertChar('b').assertFlush().assertEmpty();
    final ArrayList<String> requests = new ArrayList<String>();
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        requests.add(request);
      }
    });
    console.on(KeyStrokes.ENTER);
    assertEquals(Arrays.asList("ab"), requests);
  }

  public void testMultiline2() {
    console.init();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.QUOTE);
    console.on(KeyStrokes.ENTER);
    console.on(KeyStrokes.b);
    assertEquals("b", getCurrentLine());
    driver.assertChar('a').assertFlush();
    driver.assertChar('"').assertFlush();
    driver.assertCRLF().assertChars("> ").assertFlush();
    driver.assertChar('b').assertFlush().assertEmpty();
    console.on(KeyStrokes.ENTER);
    console.on(KeyStrokes.c);
    console.on(KeyStrokes.QUOTE);
    console.on(KeyStrokes.d);
    driver.assertCRLF().assertChars("> ").assertFlush();
    driver.assertChar('c').assertFlush();
    driver.assertChar('"').assertFlush();
    driver.assertChar('d').assertFlush().assertEmpty();
    final ArrayList<String> requests = new ArrayList<String>();
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        requests.add(request);
      }
    });
    console.on(KeyStrokes.ENTER);
    assertEquals(Arrays.asList("a\"\nb\nc\"d"), requests);
  }

  public void testCompleteMultiline() {
    shell.setCompleter(new SyncCompleter() {
      @Override
      public CompletionMatch complete(String prefix) {
        Completion.Builder builder = Completion.builder("s");
        if (prefix.equals("s")) {
          builder.add("end", true);
          builder.add("et", true);
        } else if (prefix.equals("sen")) {
          builder.add("d", true);
        }
        return new CompletionMatch(Delimiter.EMPTY, builder.build());
      }
    });
    console.init();
    console.on(KeyStrokes.s);
    console.on(KeyStrokes.BACKSLASH);
    console.on(KeyStrokes.ENTER);
    driver.assertChar('s').assertFlush();
    driver.assertChar('\\').assertFlush();
    driver.assertCRLF().assertChars("> ").assertFlush();
    console.on(KeyStrokes.COMPLETE);
    driver.assertChars("\nsend  set   \ns\\\n> e").assertFlush().assertEmpty();
    console.on(KeyStrokes.n);
    driver.assertChar('n').assertFlush().assertEmpty();
    console.on(KeyStrokes.COMPLETE);
    driver.assertChars("d ").assertFlush().assertEmpty();
  }

  public void testInterruptMultiline() throws Exception {
    final ArrayBlockingQueue<String> keys = new ArrayBlockingQueue<String>(1);
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        keys.add(request);
      }
    });
    console.init();
    console.on(KeyStrokes.QUOTE);
    console.on(KeyStrokes.ENTER);
    driver.assertChar('"').assertFlush();
    driver.assertCRLF().assertChars("> ").assertFlush().assertEmpty();
    console.on(KeyStrokes.INTERRUPT);
    driver.assertCRLF().assertFlush().assertEmpty();
    console.on(KeyStrokes.ENTER);
    driver.assertCRLF().assertFlush().assertEmpty();
    String request = keys.poll(1, TimeUnit.SECONDS);
    assertEquals("", request);
  }

  public void testHistory() {
    console.init();
    console.on(KeyStrokes.UP);
    driver.assertEmpty();
    console.on(KeyStrokes.DOWN);
    driver.assertEmpty();
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        context.end(ShellResponse.ok());
      }
    });
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.ENTER);
    driver.assertChar('a').assertFlush().assertCRLF().assertFlush().assertCRLF().assertFlush().assertEmpty();
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        context.end(ShellResponse.ok());
      }
    });
    console.on(KeyStrokes.b);
    console.on(KeyStrokes.ENTER);
    driver.assertChar('b').assertFlush().assertCRLF().assertFlush().assertCRLF().assertFlush().assertEmpty();
    console.on(KeyStrokes.c);
    driver.assertChar('c').assertFlush().assertEmpty();
    console.on(KeyStrokes.UP);
    driver.assertDel().assertChar('b').assertFlush().assertEmpty();
    console.on(KeyStrokes.UNDERSCORE);
    driver.assertChar('_').assertFlush().assertEmpty();
    console.on(KeyStrokes.UP);
    driver.assertDel().assertDel().assertChar('a').assertFlush().assertEmpty();
    console.on(KeyStrokes.UP);
    driver.assertEmpty();
    console.on(KeyStrokes.DOWN);
    driver.assertDel().assertChar('b').assertChar('_').assertFlush().assertEmpty();
    console.on(KeyStrokes.DOWN);
    driver.assertDel().assertDel().assertChar('c').assertFlush().assertEmpty();
    console.on(KeyStrokes.DOWN);
    driver.assertEmpty();
    console.on(KeyStrokes.UP);
    driver.assertDel().assertChar('b').assertChar('_').assertFlush().assertEmpty();
    console.on(KeyStrokes.ONE);
    driver.assertChar('1').assertFlush().assertEmpty();
    final ArrayList<String> requests = new ArrayList<String>();
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        requests.add(request);
        context.end(ShellResponse.ok());
      }
    });
    console.on(KeyStrokes.ENTER);
    driver.assertCRLF().assertFlush().assertCRLF().assertFlush().assertEmpty();
    assertEquals(Arrays.asList("b_1"), requests);
    console.on(KeyStrokes.UP);
    driver.assertChar('b').assertChar('_').assertChar('1').assertFlush().assertEmpty();
    console.on(KeyStrokes.UP);
    driver.assertDel().assertDel().assertDel().assertChar('b').assertChar('_').assertFlush().assertEmpty();
    console.on(KeyStrokes.UP);
    driver.assertDel().assertDel().assertChar('a').assertFlush().assertEmpty();
  }

  public void testReadLine1() throws Exception {
    final ArrayBlockingQueue<ShellProcessContext> contexts = new ArrayBlockingQueue<ShellProcessContext>(1);
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        contexts.add(context);
      }
    });
    console.init();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.ENTER);
    driver.assertChar('a').assertFlush().assertCRLF().assertFlush().assertEmpty();
    driver.assertEmpty();
    final ShellProcessContext context = contexts.poll();
    assertNotNull(context);
    final ArrayBlockingQueue<String> lines = new ArrayBlockingQueue<String>(1);
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          String s = context.readLine("m", true);
          lines.add(s);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    t.start();
    while (t.getState() != Thread.State.WAITING) {
      // Wait until the other thread is waiting
    }
    console.on(KeyStrokes.b);
    console.on(KeyStrokes.ENTER);
    driver.assertChar('m').assertFlush().assertChar('b').assertFlush().assertCRLF().assertFlush().assertEmpty();
    String line = lines.poll(3, TimeUnit.SECONDS);
    assertEquals("b", line);
  }

  /**
   * Same than {@link #testReadLine1()} but we provide input before the thread read lines effectively
   * which leads to a simpler test since we need only one thread
   */
  public void testReadLine2() throws Exception {
    final ArrayBlockingQueue<ShellProcessContext> contexts = new ArrayBlockingQueue<ShellProcessContext>(1);
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        contexts.add(context);
      }
    });
    console.init();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.ENTER);
    driver.assertChar('a').assertFlush().assertCRLF().assertFlush().assertEmpty();
    driver.assertEmpty();
    final ShellProcessContext context = contexts.poll();
    assertNotNull(context);
    console.on(KeyStrokes.b);
    console.on(KeyStrokes.ENTER);
    driver.assertEmpty();
    String line = context.readLine("m", true);
    driver.assertChar('m').assertFlush().assertChar('b').assertFlush().assertCRLF().assertFlush().assertEmpty();
    assertEquals("b", line);
  }

  /**
   * Make sure that
   * - we cannot call readline twice at the same time and
   * - if the reading thread is interrupted then it throws InterruptedIOException
   */
  public void testReadLine3() throws Exception {
    final ArrayBlockingQueue<ShellProcessContext> contexts = new ArrayBlockingQueue<ShellProcessContext>(1);
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        contexts.add(context);
      }
    });
    console.init();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.ENTER);
    driver.assertChar('a').assertFlush().assertCRLF().assertFlush().assertEmpty();
    driver.assertEmpty();
    final ShellProcessContext context = contexts.poll();
    assertNotNull(context);
    final ArrayBlockingQueue<Boolean> interrupteds = new ArrayBlockingQueue<Boolean>(1);
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          context.readLine("", true);
        }
        catch (InterruptedException e) {
          interrupteds.add(true);
        }
        catch (Exception e) {
          interrupteds.add(false);
        }
      }
    };
    t.start();
    while (t.getState() != Thread.State.WAITING) {
      // Wait until the other thread is waiting
    }
    try {
      context.readLine("", true);
      fail();
    }
    catch (IllegalStateException expected) {
    }
    t.interrupt();
    boolean interrupted = interrupteds.poll(3, TimeUnit.SECONDS);
    assertTrue(interrupted);
  }

  /**
   * Make sure that a reading thread is interrupted when the process ends
   */
  public void testReadLine4() throws Exception {
    final ArrayBlockingQueue<ShellProcessContext> contexts = new ArrayBlockingQueue<ShellProcessContext>(1);
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        contexts.add(context);
      }
    });
    console.init();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.ENTER);
    driver.assertChar('a').assertFlush().assertCRLF().assertFlush().assertEmpty();
    driver.assertEmpty();
    final ShellProcessContext context = contexts.poll();
    assertNotNull(context);
    final ArrayBlockingQueue<Boolean> interrupteds = new ArrayBlockingQueue<Boolean>(1);
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          context.readLine("", true);
        }
        catch (InterruptedException e) {
          interrupteds.add(true);
        }
        catch (Exception e) {
          interrupteds.add(false);
        }
      }
    };
    t.start();
    while (t.getState() != Thread.State.WAITING) {
      // Wait until the other thread is waiting
    }
    context.end(ShellResponse.ok());
    boolean interrupted = interrupteds.poll(3, TimeUnit.SECONDS);
    assertTrue(interrupted);
  }

  /**
   * Make sure that a reading thread is interrupted when the process is cancelled
   */
  public void testReadLine5() throws Exception {
    final ArrayBlockingQueue<ShellProcessContext> contexts = new ArrayBlockingQueue<ShellProcessContext>(1);
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        contexts.add(context);
      }
    });
    console.init();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.ENTER);
    driver.assertChar('a').assertFlush().assertCRLF().assertFlush().assertEmpty();
    driver.assertEmpty();
    final ShellProcessContext context = contexts.poll();
    assertNotNull(context);
    final ArrayBlockingQueue<Boolean> interrupteds = new ArrayBlockingQueue<Boolean>(1);
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          context.readLine("", true);
        }
        catch (InterruptedException e) {
          interrupteds.add(true);
        }
        catch (Exception e) {
          interrupteds.add(false);
        }
      }
    };
    t.start();
    while (t.getState() != Thread.State.WAITING) {
      // Wait until the other thread is waiting
    }
    console.on(KeyStrokes.INTERRUPT);
    boolean interrupted = interrupteds.poll(3, TimeUnit.SECONDS);
    assertTrue(interrupted);
  }
}
