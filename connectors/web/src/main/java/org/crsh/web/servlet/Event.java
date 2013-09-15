package org.crsh.web.servlet;

import com.google.gson.Gson;

/** @author Julien Viet */
class Event {

  /** . */
  static final Gson gson = new Gson();

  /** . */
  final String type;

  /** . */
  final Object data;

  Event(String type) {
    this(type, null);
  }

  Event(String type, Object data) {
    this.type = type;
    this.data = data;
  }

  public String toJSON() {
    return gson.toJson(this);
  }
}
