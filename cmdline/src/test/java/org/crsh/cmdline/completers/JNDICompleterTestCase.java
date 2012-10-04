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

import junit.framework.TestCase;

import org.crsh.cmdline.spi.ValueCompletion;

public class JNDICompleterTestCase extends TestCase {

	private JNDICompleter completer;

	@Override
	protected void setUp() throws Exception {
		completer = new JNDICompleter();
	}

	private ValueCompletion getValueCompletionComplete(String prefix) {
		ValueCompletion valueCompletion = null;
		try {
			valueCompletion = completer.complete(null, prefix);
		} catch (Exception e) {
			e.printStackTrace();
			fail("complete() method shouldn't throw an exeption");
		}
		return valueCompletion;
	}

	public void testGetDatasourceListWithNullPrefix() {
		String prefix = null;
		ValueCompletion valueCompletion = getValueCompletionComplete(prefix);
		assertNotNull(valueCompletion);
		assertTrue(
				"valueCompletion must be empty because no datasources is available",
				valueCompletion.isEmpty());
	}

	public void testGetDatasourceListWithEmptyPrefix() {
		String prefix = "";
		ValueCompletion valueCompletion = getValueCompletionComplete(prefix);
		assertNotNull(valueCompletion);
		assertTrue(
				"valueCompletion must be empty because no datasources is available",
				valueCompletion.isEmpty());
	}

	public void testGetJndiPathWithNullArgs() {
		try {
			assertNull(completer.getJndiPath(null, "toto", null));
			fail("An exception should be throw because of null contextName");
		} catch (IllegalArgumentException e) {
			// OK
		}

		try {
			assertNull(completer.getJndiPath("toto", null, null));
			fail("An exception should be throw because of null contextName");
		} catch (IllegalArgumentException e) {
			// OK
		}

		try {
			assertNull(completer.getJndiPath("toto", "t", null));
			fail("An exception should be throw because of null contextName");
		} catch (IllegalArgumentException e) {
			// OK
		}

	}

	public void testgetJndiWithValue() {
		assertNull(completer.getJndiPath("java:jboss/", "datasources", "jdbc"));
		assertNotNull(completer.getJndiPath("java:jboss/", "datasources",
				"java"));
	}

	public void testGetJndiPathWithEmptyPrefix() {
		assertEquals("java:jboss/datasources",
				completer.getJndiPath("java:jboss/", "datasources", ""));
		assertEquals("java:jboss/datasources",
				completer.getJndiPath("java:jboss", "/datasources", ""));
	}

	public void testGetJndiPathWithPrefixltSuffix() {

		assertEquals("va:jboss/datasources",
				completer.getJndiPath("java:jboss/", "datasources", "ja"));
		assertEquals("ava:jboss/datasources",
				completer.getJndiPath("java:jboss/", "datasources", "j"));
		assertEquals("java:jboss/datasources",
				completer.getJndiPath("java:jboss/", "datasources", ""));
		assertEquals(null, completer.getJndiPath("java:jboss/",
				"TransactionManager", "java:ja"));
	}

	public void testGetJndiPathWithPrefixgtSuffix() {

		assertEquals("atasources", completer.getJndiPath("java:jboss/",
				"datasources", "java:jboss/d"));
		assertEquals(null, completer.getJndiPath("java:jboss/", "datasources",
				"java:jboss/datasources"));
		assertEquals(null, completer.getJndiPath("java:jboss/", "datasources",
				"java:jboss/datasourcesAAA"));
		assertEquals(null, completer.getJndiPath("java:jboss/",
				"TransactionManager", "java:jboss/datasources"));

	}

}
