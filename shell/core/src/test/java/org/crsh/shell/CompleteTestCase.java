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

package org.crsh.shell;

import org.crsh.cmdline.CommandCompletion;
import org.crsh.cmdline.spi.Completion;

import java.util.Collections;

public class CompleteTestCase extends AbstractCommandTestCase {

  public void testCommandImplementingCompleter() {
    lifeCycle.bind("complete", Commands.Complete.class);
    CommandCompletion commandCompletion = assertComplete("complete foo");
    Completion completion = commandCompletion.getValue();
    assertEquals("foo", completion.getPrefix());
    assertEquals(Collections.singleton("bar"), completion.getValues());
    assertTrue(completion.get("bar"));
  }

  public void testSessionAccess() {
    lifeCycle.bind("complete", Commands.CompleteWithSession.class);
    shell.getSession().put("juu", "juu_value");
    CommandCompletion commandCompletion = assertComplete("complete foo");
    Completion completion = commandCompletion.getValue();
    assertEquals("foo", completion.getPrefix());
    assertEquals(Collections.singleton("juu_value"), completion.getValues());
    assertTrue(completion.get("juu_value"));
  }
}
