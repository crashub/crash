package org.crsh.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/** Entry point classes define <code>onModuleLoad()</code>. */
public class Crash implements EntryPoint {
  /** The message displayed to the user when the server cannot be reached or returns an error. */
  private static final String SERVER_ERROR = "An error occurred while "
    + "attempting to contact the server. Please check your network "
    + "connection and try again.";

  /** Create a remote service proxy to talk to the server-side Greeting service. */
  private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);

  /** This is the entry point method. */
  public void onModuleLoad() {

    final Term term = new Term(greetingService, 32);

    //
    Button clear = new Button();
    clear.setText("Clear");
    clear.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        term.clear();
      }
    });

    //
    VerticalPanel vertical = new VerticalPanel();
    vertical.add(clear);
    vertical.add(term);

    //
    RootPanel.get().add(vertical);
  }
}
