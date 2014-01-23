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
import java.util.ArrayList;
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
  final BlockingDeque<KeyEvent> buffer;

  /** . */
  final ConsoleDriver driver;

  /** . */
  final Editor editor;

  /** . */
  boolean running;

  /** . */
  private Status mode;

  /** . */
  private final ArrayList<Runnable> modeListeners;

  public Console(Shell shell, ConsoleDriver driver) throws NullPointerException {
    if (shell == null) {
      throw new NullPointerException("No null shell accepted");
    }
    this.driver = driver;
    this.shell = shell;
    this.buffer = new LinkedBlockingDeque<KeyEvent>(1024);
    this.handler = new AtomicReference<Plugin>();
    this.editor = new Editor(this);
    this.mode = new Status.Emacs();
    this.running = true;
    this.modeListeners = new ArrayList<Runnable>();
  }

  public void setMode(Status mode) {
    this.mode = mode;
    for (Runnable listener : modeListeners) {
      listener.run();
    }
  }

  public void toEmacs() {
    setMode(new Status.Emacs());
  }

  public void toMove() {
    setMode(new Status.Move());
  }

  public void toInsert() {
    setMode(new Status.Insert());
  }

  public Status getMode() {
    return mode;
  }

  public void addModeListener(Runnable runnable) {
    modeListeners.add(runnable);
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

  public Iterable<KeyEvent> getKeyBuffer() {
    return buffer;
  }

  public void on(Operation operation, int... buffer) {
    mode.on(this, operation, buffer);
  }


  /**
   * Key event.
   *
   * @param event the event
   */
  public void on(KeyEvent event) {
    on(new KeyEvent[]{event});
  }

  /**
   * Key event.
   *
   * @param events the events
   */
  public void on(KeyEvent... events) {
    for (KeyEvent event : events) {
      if (event.getAction() == EditorAction.INTERRUPT) {
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
          continue;
        }
      }
      buffer.add(event);
      iterate();
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
      KeyEvent key = buffer.poll();
      if (key != null) {
        if (current == null) {
          throw new IllegalStateException("Not initialized");
        } else if (current instanceof Editor) {
          Editor editor = (Editor)current;
          String line = editor.append(key);
          if (line != null) {
            ShellProcess process = shell.createProcess(line);
            ProcessHandler context = new ProcessHandler(this, process);
            handler.set(context);
            process.execute(context);
          }
        } else if (current instanceof ProcessHandler) {
          ProcessHandler processHandler = (ProcessHandler)current;
          ProcessHandler.Reader reader = processHandler.editor.get();
          if (reader != null) {
            String s = reader.editor.append(key);
            if (s != null) {
              reader.line.add(s);
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
