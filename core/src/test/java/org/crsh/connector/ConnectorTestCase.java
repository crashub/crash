/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

package org.crsh.connector;

import org.crsh.AbstractRepositoryTestCase;
import org.crsh.TestShellContext;
import org.crsh.shell.ShellBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ConnectorTestCase extends AbstractRepositoryTestCase {

  /** . */
  protected ShellBuilder builder;

  /** . */
  protected TestShellContext context;

  /** . */
  protected ExecutorService executor;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    //
    executor = Executors.newSingleThreadExecutor();
    context = new TestShellContext();
    builder = new ShellBuilder(context, executor);
  }

  public void _testCancelEvaluation() {
    ShellConnector connector = new ShellConnector(builder.build());
    connector.open();
    status = 0;
    connector.submitEvaluation("invoke " + ConnectorTestCase.class.getName() + " bilta");
    assertEquals(ConnectorStatus.EVALUATING, connector.getStatus());
    while (status == 0) {
      // Wait
    }
    assertEquals(1, status);
    assertEquals(ConnectorStatus.EVALUATING, connector.getStatus());
    connector.cancelEvalutation();
    assertEquals(ConnectorStatus.AVAILABLE, connector.getStatus());
    status = 2;
    while (status == 2) {
      // Wait
    }
    assertEquals(3, status);
  }

  public static void bilta() {
    if (status == 0) {
      status = 1;
      while (status == 1) {
        // Wait
      }
      if (status == 2) {
        status = 3;
      } else {
        status = -1;
      }
    } else {
      status = -1;
    }
  }

  public void testFoo() {
    
  }

  public void _testAsyncEvaluation() {
    ShellConnector connector = new ShellConnector(builder.build());
    connector.open();
    status = 0;
    connector.submitEvaluation("invoke " + ConnectorTestCase.class.getName() + " bilto");
    while (status == 0) {
      // Do nothing
    }
    assertEquals(1, status);
    connector.popResponse();
  }

  private static int status;

  public static void bilto() {
    if (status == 0) {
      status = 1;
    } else {
      status = -1;
    }
  }
}
