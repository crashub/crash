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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class Term extends Composite {

  /** . */
  private final TermText text;

  /** . */
  private final ScrollPanel scroll;

  /** . */
  private final GreetingServiceAsync crash;

  public Term(GreetingServiceAsync crash, int height) {

    //
    TermText text = new TermText(height);
    text.addKeyPressHandler(pressHandler);
    text.addKeyDownHandler(downHandler);

    //
    ScrollPanel scroll = new ScrollPanel(text);
    scroll.setSize("800px", "600px");

    //
    initWidget(scroll);

    //
    this.text = text;
    this.scroll = scroll;
    this.crash = crash;

  }

  private final KeyPressHandler pressHandler = new KeyPressHandler() {
    public void onKeyPress(KeyPressEvent event) {
      char c = event.getCharCode();
      if (Character.isLetterOrDigit(c) || c == ' ') {
        text.buffer.append(c);
        print(c);
      }
      event.preventDefault();
      event.stopPropagation();
    }
  };

  private final KeyDownHandler downHandler = new KeyDownHandler() {
    public void onKeyDown(KeyDownEvent event) {
      int a = event.getNativeKeyCode();
      if (KeyCodes.KEY_ENTER == a) {

        // Make call
        crash.process(text.buffer.toString(), new AsyncCallback<String>() {
          public void onFailure(Throwable caught) {
          }
          public void onSuccess(String result) {
            print(result);
          }
        });

        // Clear buffer
        text.buffer.setLength(0);

        // Do we need that ?
        print('\n');

        //
        event.preventDefault();
        event.stopPropagation();
      }
    }
  };

  @Override
  protected void onAttach() {
    super.onAttach();

    // Display prompt
    // we should somehow make this blocking
    crash.getWelcome(new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(String result) {
        print(result);
      }
    });
  }

  /**
   * Clear all but the last line to preserve current edition.
   */
  public void clear() {
    text.clear();
    text.repaint();
    scroll.scrollToBottom();
  }

  public void print(char c) {
    text.print(c);
    text.repaint();
    scroll.scrollToBottom();
  }

  public void print(CharSequence s) {
    text.print(s);
    text.repaint();
    scroll.scrollToBottom();
  }
}
