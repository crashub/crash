/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.web.client;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextArea;

import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class Term extends TextArea {

  /** . */
  private final int maxHeight;

  /** . */
  private final StringBuilder buffer;

  /** . */
  private final GreetingServiceAsync crash;

  public Term(int maxHeight, GreetingServiceAsync crash) {

    //
    if (maxHeight < 0) {
      throw new IllegalArgumentException("Max height cannot be negative");
    }

    addKeyPressHandler(new KeyPressHandler() {
      public void onKeyPress(KeyPressEvent event) {
        char c = event.getCharCode();
        if (Character.isLetterOrDigit(c) || c == ' ') {
          buffer.append(c);
          print(c);
          refresh();
        }
        event.preventDefault();
        event.stopPropagation();
      }
    });

    //
    addKeyDownHandler(new KeyDownHandler() {
      public void onKeyDown(KeyDownEvent event) {
        int a = event.getNativeKeyCode();
        if (KeyCodes.KEY_ENTER == a) {

          // Make call
          Term.this.crash.process(buffer.toString(), new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {

            }
            public void onSuccess(String result) {
              print(result);
              refresh();
            }
          });

          // Clear buffer
          buffer.setLength(0);

          // Do we need that ?
          print('\n');
          refresh();

          //
          event.preventDefault();
          event.stopPropagation();
        }
      }
    });

    //
    LinkedList<StringBuilder> state = new LinkedList<StringBuilder>();
    state.addLast(new StringBuilder());

    //
    this.maxHeight = maxHeight;
    this.state = state;
    this.buffer = new StringBuilder();
    this.crash = crash;
  }

  /** The state. */
  private final LinkedList<StringBuilder> state;

  @Override
  public void setCursorPos(int pos) {
    // Do nothing
  }

  public void refresh() {

    //
    int width = getCharacterWidth();
    int height = getVisibleLines();

    //
    StringBuilder text = new StringBuilder();

    // Pad
/*
    for (int i = state.size();i < height;i++) {
      text.append('\n');
    }
*/

    //
    for (StringBuilder line : state) {
      text.append(line);
      text.append('\n');
    }

    //
    setText(text.toString());

    // Position cursor to correct place
    int pos = Math.max(0, text.length() - 1);
    super.setCursorPos(pos);
  }

  public void print(char c) {
    if (c == '\n') {
      state.addLast(new StringBuilder());
    } else {
      state.getLast().append(c);
    }
  }

  public void print(String text) {

    //
    int from = 0;
    while (true) {
      int to = text.indexOf('\n', from);
      StringBuilder last = state.getLast();
      if (to == -1) {
        last.append(text, from, text.length());
        break;
      } else {
        last.append(text, from, to);
        state.addLast(new StringBuilder());
      }
      from = to + 1;
    }

    // Need to remove garbage
  }

  @Override
  protected void onAttach() {
    super.onAttach();

    //
    refresh();

    //Display prompt
    // we should somehow make this blocking
    crash.getWelcome(new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {

      }

      public void onSuccess(String result) {
        print(result);
        refresh();
      }
    });
  }
}
