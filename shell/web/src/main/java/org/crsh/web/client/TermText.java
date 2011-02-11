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

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class TermText extends FocusWidget {

  /** . */
  private final GreetingServiceAsync crash;

  /** . */
  private final StringBuilder buffer;

  /** The state. */
  private final StringBuilder state;

  /** The blinking. */
  private boolean on;

  public TermText(GreetingServiceAsync crash) {
    super(Document.get().createDivElement());

    //
    setStyleName("crash-term");

    //
//    DOM.setStyleAttribute(getElement(), "overflow", "scroll");
//    DOM.setStyleAttribute(getElement(), "height", "256px");

    //
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
          TermText.this.crash.process(buffer.toString(), new AsyncCallback<String>() {
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
    addMouseDownHandler(new MouseDownHandler() {
      public void onMouseDown(MouseDownEvent event) {
        setFocus(true);
      }
    });

    //
    this.state = new StringBuilder();
    this.on = false;
    this.buffer = new StringBuilder();
    this.crash = crash;
  }

  @Override
  protected void onAttach() {
    super.onAttach();

    // Blinking cursor
    Timer t = new Timer() {
      public void run() {
        on = !on;
        refresh();
      }
    };

    //
    t.scheduleRepeating(500);

    // Display prompt
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

  public void print(char c) {
    state.append(c);
  }

  public void print(String text) {
    state.append(text);
  }

  public void refresh() {

    _refresh();

    Object parent = getParent();
    if (parent instanceof ScrollPanel) {
      ScrollPanel sp = (ScrollPanel)parent;
      sp.scrollToBottom();
    }

  }

  private void _refresh() {

    //
    StringBuilder markup = new StringBuilder();

    // Pad

    //
    int from = 0;
    while (true) {
      int to = state.indexOf("\n", from);
      markup.append(state, from, to == -1 ? state.length() : to);
      if (to == -1) {
        break;
      } else {
        markup.append("<br/>");
        from = to + 1;
      }
    }

    // The cursor
    if (on) {
      markup.append("_");
    }

    // Update markup state
    getElement().setInnerHTML(markup.toString());

    // Adjust scroll to be at the bottom
//    String height = DOM.getStyleAttribute(getElement(), "scrollHeight");
//    DOM.setStyleAttribute(getElement(), "scrollTop", height);
  }
}
