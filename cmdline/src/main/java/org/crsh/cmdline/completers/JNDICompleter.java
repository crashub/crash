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
package org.crsh.cmdline.completers;

import java.util.HashSet;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.spi.Completer;
import org.crsh.cmdline.spi.ValueCompletion;

public class JNDICompleter implements Completer {
	
	/**
	 * Create a list of context name to search in JNDI, e.g :contextName ["java:jboss/datasources", "",...]
   * This list contains standard names to help search.
	 */
	private static HashSet<String> initJndicontextNames() {
    HashSet<String> contextNames = new HashSet<String>();
		contextNames.add("java:jboss/");
		contextNames.add("java:comp/env/jdbc");
		contextNames.add("java:/");
		contextNames.add("");
    return contextNames;
	}	
	
	
	public ValueCompletion complete(ParameterDescriptor<?> parameter,
			String prefix) throws Exception {
		if (null == prefix) {
			prefix = "";
		}
		return getJndiList(prefix);
	}

	/**
	 * Search JNDI Path in Context and put the result in ValueCompletion.
	 * 
	 * @return ValueCompletion
	 */
	ValueCompletion getJndiList(String prefix) {
    HashSet<String> contextNames = initJndicontextNames();
    if (prefix != null) {
      contextNames.add(prefix);
    }
    ValueCompletion completions = new ValueCompletion();
    for (String contextName : contextNames) {

			if (!contextName.endsWith("/")) {
				contextName = contextName + "/";
			}

			try {
				InitialContext ctx = new InitialContext();
				NamingEnumeration<NameClassPair> list = ctx.list(contextName);
				while (list.hasMore()) {
					NameClassPair nc = list.next();
					if (null == nc) {
						continue;
					}
					String jndiPath = getJndiPath(contextName, nc.getName(), prefix);
					if (jndiPath != null) {
						completions.put(jndiPath, false);
					}

				}
			} catch (NamingException e1) {
				// We display nothing if there is no data source
			}
		}
		return completions;
	}

	/**
	 * Make JndiPath String.
	 * 
	 * @param contextName String (e.g:java:jboss/)
	 * @param ncName (e.g:datasources)
	 * @return String (e.g:java:jboss/datasources)
	 */
	public String getJndiPath(String contextName, String ncName, String prefix) {
		String result;
		if ((null == contextName) || (null == ncName) || (null == prefix)) {
			throw new IllegalArgumentException("Argument(s) must not be null");
		}

		if (ncName.isEmpty()) {
			result = contextName;
		} else {
			result = contextName + ncName;
		}

		if (!prefix.isEmpty()) {
			final int prefixSize = prefix.length();
			final int resultSize = result.length();
			String str = null;

			if (result.startsWith(prefix)) {
				if ((prefixSize > 0) && (resultSize > prefixSize)) {
					str = result.substring(prefixSize);
				}
			} else if (prefix.startsWith(result)) {
				if ((resultSize > 0) && (prefixSize < resultSize)) {
					str = prefix.substring(resultSize);
				}
			} else {
				str = null;
			}
			result = str;
		}
		return result;
	}	
}
