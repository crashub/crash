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

import jline.console.Operation;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.util.Safe;

import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A console state machine, which delegates the state machine to the {@link Plugin} implementation.
 *
 * @author Julien Viet
 */
public class Console {

  /** . */
  final Shell shell;

  /** The current handler. */
  final AtomicReference<Plugin> handler;

  /** The buffer. */
  final BlockingDeque<KeyStroke> buffer;

  /** . */
  final ConsoleDriver driver;

  /** . */
  final Editor editor;

  /** . */
  boolean running;

  public Console(Shell shell, ConsoleDriver driver) throws NullPointerException {
    if (shell == null) {
      throw new NullPointerException("No null shell accepted");
    }
    this.driver = driver;
    this.shell = shell;
    this.buffer = new LinkedBlockingDeque<KeyStroke>(1024);
    this.handler = new AtomicReference<Plugin>();
    this.editor = new Editor(this);
    this.running = true;
  }

  public void setMode(Mode mode) {
    editor.setMode(mode);
  }

  public void toEmacs() {
    setMode(Mode.EMACS);
  }

  public void toMove() {
    setMode(Mode.VI_MOVE);
  }

  public void toInsert() {
    setMode(Mode.VI_INSERT);
  }

  public Mode getMode() {
    return editor.getMode();
  }

  public void addModeListener(Runnable runnable) {
    editor.addModeListener(runnable);
  }

  public boolean isRunning() {
    return running;
  }

  /**
   * Initiali
   */
  public void init() {
    // Take care of pormpt
    String welcome = shell.getWelcome();
    if (welcome != null && welcome.length() > 0) {
      try {
        driver.write(welcome);
        driver.flush();
      }
      catch (IOException e) {
        // Log it
      }
    }
    edit();
  }

  public Iterable<KeyStroke> getKeyBuffer() {
    return buffer;
  }

  public void on(Operation operation, int... buffer) {
    on(new KeyStroke(operation, buffer));
  }

  public void on(KeyStroke keyStroke) {
    if (keyStroke.operation == Operation.INTERRUPT) {
      Plugin current = handler.get();
      if (current == null) {
        throw new IllegalStateException("Not initialized");
      } else if (current instanceof ProcessHandler) {
        ProcessHandler processHandler = (ProcessHandler)current;
        ProcessHandler.Reader reader = processHandler.editor.get();
        if (reader != null) {
          reader.thread.interrupt();
        }
        processHandler.process.cancel();
        return;
      }
    }
    buffer.add(keyStroke);
    iterate();
  }

  public void on(KeyStroke[] keyStrokes) {
    for (KeyStroke keyStroke : keyStrokes) {
      on(keyStroke);
    }
  }


  void close() {
    running = false;
    Safe.close(driver);
  }

  /**
   * Switch to edit.
   */
  Editor edit() {
    String prompt = shell.getPrompt();
    if (prompt != null && prompt.length() > 0) {
      try {
        driver.write(prompt);
        driver.flush();
      }
      catch (IOException e) {
        // Swallow for now...
      }
    }
    editor.reset();
    handler.set(editor);
    return editor;
  }

  /**
   * Process the state machine.
   */
  void iterate() {
    while (running) {
      Plugin current = handler.get();
      KeyStroke key = buffer.poll();
      if (key != null) {
        if (current == null) {
          throw new IllegalStateException("Not initialized");
        } else if (current instanceof Editor) {
          Editor editor = (Editor)current;
          EditorAction action = editor.getMode().on(key);
          if (action != null) {
            String line = editor.append(action, key.sequence);
            if (line != null) {
              ShellProcess process = shell.createProcess(line);
              ProcessHandler context = new ProcessHandler(this, process);
              handler.set(context);
              process.execute(context);
            }
          }
        } else if (current instanceof ProcessHandler) {
          ProcessHandler processHandler = (ProcessHandler)current;
          ProcessHandler.Reader reader = processHandler.editor.get();
          if (reader != null) {
            EditorAction action = editor.getMode().on(key);
            if (action != null) {
              String s = reader.editor.append(action, key.sequence);
              if (s != null) {
                reader.line.add(s);
              }
            }
          } else {
            KeyHandler keyHandler = processHandler.process.getKeyHandler();
            if (keyHandler != null) {
              KeyType type = KeyType.map(key.operation, key.sequence);
              keyHandler.handle(type, key.sequence);
            } else {
              buffer.addFirst(key);
            }
            return;
          }
        } else {
          throw new UnsupportedOperationException();
        }
      } else {
        return;
      }
    }
  }
}
