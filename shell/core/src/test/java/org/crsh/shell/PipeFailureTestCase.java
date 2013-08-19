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

import org.crsh.command.ScriptException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PipeFailureTestCase extends AbstractCommandTestCase {

  public void testFailDuringOpen1() {
    lifeCycle.bindClass("FailDuringOpen1", Commands.FailDuringOpen1.class);
    Commands.FailDuringOpen1.reset();
    assertError("FailDuringOpen1", ErrorType.EVALUATION, ScriptException.class);
    assertEquals(1, Commands.FailDuringOpen1.openCount.get());
    assertEquals(0, Commands.FailDuringOpen1.provideCound.get());
    assertEquals(0, Commands.FailDuringOpen1.flushCount.get());
    assertEquals(0, Commands.FailDuringOpen1.closeCount.get());
  }

  public void testFailDuringOpen2() {
    lifeCycle.bindClass("FailDuringOpen1", Commands.FailDuringOpen1.class);
    lifeCycle.bindClass("FailDuringOpen2", Commands.FailDuringOpen2.class);
    Commands.FailDuringOpen1.reset();
    Commands.FailDuringOpen2.reset();
    assertError("FailDuringOpen1 | FailDuringOpen2", ErrorType.EVALUATION, ScriptException.class);
    assertEquals(1, Commands.FailDuringOpen1.openCount.get());
    assertEquals(0, Commands.FailDuringOpen1.provideCound.get());
    assertEquals(0, Commands.FailDuringOpen1.flushCount.get());
    assertEquals(0, Commands.FailDuringOpen1.closeCount.get());
    assertEquals(1, Commands.FailDuringOpen2.openCount.get());
    assertEquals(0, Commands.FailDuringOpen2.provideCound.get());
    assertEquals(1, Commands.FailDuringOpen2.flushCount.get());
    assertEquals(1, Commands.FailDuringOpen2.closeCount.get());
  }

  public void testFailDuringOpen3() {
    lifeCycle.bindClass("FailDuringOpen1", Commands.FailDuringOpen1.class);
    lifeCycle.bindClass("FailDuringOpen2", Commands.FailDuringOpen2.class);
    Commands.FailDuringOpen1.reset();
    Commands.FailDuringOpen2.reset();
    assertError("FailDuringOpen2 | FailDuringOpen1", ErrorType.EVALUATION, ScriptException.class);
    assertEquals(1, Commands.FailDuringOpen1.openCount.get());
    assertEquals(0, Commands.FailDuringOpen1.provideCound.get());
    assertEquals(0, Commands.FailDuringOpen1.flushCount.get());
    assertEquals(0, Commands.FailDuringOpen1.closeCount.get());
    assertEquals(0, Commands.FailDuringOpen2.openCount.get());
    assertEquals(0, Commands.FailDuringOpen2.provideCound.get());
    assertEquals(0, Commands.FailDuringOpen2.flushCount.get());
    assertEquals(0, Commands.FailDuringOpen2.closeCount.get());
  }
}
