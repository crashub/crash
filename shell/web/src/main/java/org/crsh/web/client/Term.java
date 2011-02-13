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

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
  private final ShellServiceAsync remote;

  /** . */
  private final DecoratedPopupPanel popup;

  public Term(ShellServiceAsync remote, int height) {

    //
    TermText text = new TermText(height);
    text.addKeyPressHandler(pressHandler);
    text.addKeyDownHandler(downHandler);

    //
    ScrollPanel scroll = new ScrollPanel(text);
    scroll.setSize("800px", "600px");

    //
    DecoratedPopupPanel popup = new DecoratedPopupPanel(true, false);
    popup.addCloseHandler(closePopup);

    //
    initWidget(scroll);

    //
    this.text = text;
    this.scroll = scroll;
    this.remote = remote;
    this.popup = popup;
  }

  private final KeyPressHandler pressHandler = new KeyPressHandler() {
    public void onKeyPress(KeyPressEvent event) {
      int code = event.getNativeEvent().getKeyCode();
      boolean handled = false;
      if (code == KeyCodes.KEY_BACKSPACE) {
        text.bufferDrop();
        handled = true;
      } else if (code == KeyCodes.KEY_ENTER) {
        String s = text.bufferSubmit();
        remote.process(s, new AsyncCallback<String>() {
          public void onFailure(Throwable caught) {
            // to handle
          }
          public void onSuccess(String result) {
            print(result);
          }
        });
        handled = true;
      } else {
        char c = event.getCharCode();
        if (Character.isLetterOrDigit(c) || c == ' ' || c == '-') {
          text.bufferAppend(c);
          handled = true;
        }
      }

      //
      if (handled) {
        repaint();
        event.preventDefault();
        event.stopPropagation();
      }
    }
  };

  // Update state when the popup is closed
  private final CloseHandler<PopupPanel> closePopup = new CloseHandler<PopupPanel>() {
    public void onClose(CloseEvent<PopupPanel> event) {
      event.getTarget().setWidget(null);
      text.setFocus(true);
      repaint();
    }
  };

  private final KeyDownHandler downHandler = new KeyDownHandler() {
    public void onKeyDown(final KeyDownEvent event) {
      int code = event.getNativeKeyCode();
      if (KeyCodes.KEY_TAB == code) {
        Scheduler.get().scheduleDeferred(new Completer());
        event.preventDefault();
        event.stopPropagation();
      }
    }
  };

  private class Completer implements Scheduler.ScheduledCommand, AsyncCallback<Map<String, String>> {

    public void execute() {
      String prefix = text.getBuffer();
      remote.complete(prefix, this);
    }

    public void onFailure(Throwable caught) {
      // Do nothing for now
    }

    public void onSuccess(final Map<String, String> result) {
      if (result.size() == 1) {
        text.bufferAppend(result.keySet().iterator().next());
        repaint();
      } else if (result.size() > 1) {
        // Get the cursor for positionning the popup
        Element elt = DOM.getElementById("crash-cursor");

        // Compute the list of strings from the result
        final List<String> strings = new ArrayList<String>(result.keySet());

        // Build the data provider
        ListDataProvider<String> a = new ListDataProvider<String>(strings);

        // I did not find something simpler for styling the cell
        AbstractCell<String> cell = new AbstractCell<String>() {
          @Override
          public void render(String value, Object key, SafeHtmlBuilder sb) {
            if (value != null) {
              sb.appendHtmlConstant("<span class=\"crash-autocomplete\">");
              sb.appendEscaped(value);
              sb.appendHtmlConstant("</span>");
            }
          }
        };

        // This will update the state to
        // 1/ update the current text buffer
        // 2/ hide the popup
        class SelectCommand implements Scheduler.ScheduledCommand {

          /** . */
          private final String value;

          SelectCommand(String value) {
            this.value = value;
          }

          public void execute() {
            text.bufferAppend(value);
            popup.hide();
          }
        }

        // Our selection model
        final SingleSelectionModel<String> model = new SingleSelectionModel<String>();
        model.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
          public void onSelectionChange(SelectionChangeEvent event) {
            String selected = model.getSelectedObject();
            String value = result.get(selected);
            Scheduler.get().scheduleDeferred(new SelectCommand(selected + value));
          }
        });

        // We need to extend the CellList because
        // 1/ need to customize the enter and escape key
        // 2/ make the setKeyboardSelected method available
        class CompleteList extends CellList<String> {
          CompleteList(Cell<String> stringCell) {
            super(stringCell);
          }

          @Override
          protected void onBrowserEvent2(Event event) {
            if ("keydown".equals(event.getType())) {
              int code = event.getKeyCode();
              if (code == KeyCodes.KEY_ENTER) {
                int index = getKeyboardSelectedRow();
                final String selected = strings.get(index);
                String rest = result.get(selected);
                String value = selected + rest;
                Scheduler.get().scheduleDeferred(new SelectCommand(value));
                return;
              } else if (code == KeyCodes.KEY_ESCAPE) {
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                  public void execute() {
                    popup.hide();
                    repaint();
                  }
                });
              }
            }
            super.onBrowserEvent2(event);
          }

          @Override
          protected void setKeyboardSelected(int index, boolean selected, boolean stealFocus) {
            super.setKeyboardSelected(index, selected, stealFocus);
          }
        }

        // Build the cell list now and select the first entry
        CompleteList list = new CompleteList(cell);
        a.addDataDisplay(list);
        list.setSelectionModel(model);
        list.setKeyboardSelected(0, true, true);

        // Show popup
        popup.setWidget(list);
        popup.setPopupPosition(elt.getAbsoluteLeft(), elt.getAbsoluteTop());
        popup.show();

        // Need to give focus explicitely here
        list.setFocus(true);
      }
    }
  }

  @Override
  protected void onAttach() {
    super.onAttach();

    // Display prompt
    // we should somehow make this blocking
    remote.getWelcome(new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(String result) {
        print(result);
      }
    });
  }

  private void repaint() {
    text.repaint();
    scroll.scrollToBottom();
  }

  /**
   * Clear all but the last line to preserve current edition.
   */
  public void clear() {
    text.clear();
    repaint();
  }

  public void print(char c) {
    text.print(c);
    repaint();
  }

  public void print(CharSequence s) {
    text.print(s);
    repaint();
  }
}
