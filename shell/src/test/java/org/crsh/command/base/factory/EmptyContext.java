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
package org.crsh.command.base.factory;

import javax.naming.*;
import java.util.Hashtable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class EmptyContext implements Context {

  public Object lookup(Name name) throws NamingException {
    return null;
  }

  public Object lookup(String name) throws NamingException {
    return null;
  }

  public void bind(Name name, Object obj) throws NamingException {
  }

  public void bind(String name, Object obj) throws NamingException {
  }

  public void rebind(Name name, Object obj) throws NamingException {
  }

  public void rebind(String name, Object obj) throws NamingException {
  }

  public void unbind(Name name) throws NamingException {
  }

  public void unbind(String name) throws NamingException {
  }

  public void rename(Name oldName, Name newName) throws NamingException {
  }

  public void rename(String oldName, String newName) throws NamingException {
  }

  public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
    return null;
  }

  public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
    return null;
  }

  public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
    return null;
  }

  public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
    return null;
  }

  public void destroySubcontext(Name name) throws NamingException {
  }

  public void destroySubcontext(String name) throws NamingException {
  }

  public Context createSubcontext(Name name) throws NamingException {
    return null;
  }

  public Context createSubcontext(String name) throws NamingException {
    return null;
  }

  public Object lookupLink(Name name) throws NamingException {
    return null;
  }

  public Object lookupLink(String name) throws NamingException {
    return null;
  }

  public NameParser getNameParser(Name name) throws NamingException {
    return null;
  }

  public NameParser getNameParser(String name) throws NamingException {
    return null;
  }

  public Name composeName(Name name, Name prefix) throws NamingException {
    return null;
  }

  public String composeName(String name, String prefix) throws NamingException {
    return null;
  }

  public Object addToEnvironment(String propName, Object propVal) throws NamingException {
    return null;
  }

  public Object removeFromEnvironment(String propName) throws NamingException {
    return null;
  }

  public Hashtable<?, ?> getEnvironment() throws NamingException {
    return null;
  }

  public void close() throws NamingException {
  }

  public String getNameInNamespace() throws NamingException {
    return null;
  }
}
