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
package org.crsh.cron;

import org.crsh.AbstractTestCase;
import test.plugin.TestPluginLifeCycle;
import org.crsh.lang.impl.groovy.GroovyLanguageProxy;
import org.crsh.shell.impl.command.CRaSHShellFactory;
import org.crsh.vfs.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author Julien Viet */
public class CrontabTestCase extends AbstractTestCase {

  public static class Support {

    /** . */
    final TestPluginLifeCycle lifecycle;

    public Support(final String crontab) throws Exception {
      lifecycle = new TestPluginLifeCycle(new CronPlugin() {
        @Override
        protected Resource getConfig() {
          return new Resource("contrab", crontab.getBytes(), 0);
        }
      }, new GroovyLanguageProxy(), new CRaSHShellFactory());
      lifecycle.start();
    }
  }

  /** . */
  private static final List<String> queue = Collections.synchronizedList(new ArrayList<String>());

  /** . */
  private static final CountDownLatch latch = new CountDownLatch(2);

  public static synchronized void countDown(String arg) {
    queue.add(arg);
    latch.countDown();
  }

  public void testCron() throws Exception {
    Support support = new Support("* * * * * foobar toto");
    support.lifecycle.bindGroovy("foobar", "" +
        "public class foobar {\n" +
        "@Command\n" +
        "public void main(@Argument String arg) {\n" +
        CrontabTestCase.class.getName() + ".countDown(arg);\n" +
        "}\n" +
        "}\n"
    );
    CronPlugin plugin = support.lifecycle.getContext().getPlugin(CronPlugin.class);
    assertNotNull(plugin);
    assertTrue(plugin.spawn());
    while (latch.getCount() == 2) {
      // Do nothing
    }
    Logger.getLogger(CrontabTestCase.class.getName()).log(Level.FINE, "Checking cron history");
    assertEquals(1, plugin.getHistory().size());
    CRaSHTaskProcess process = plugin.getHistory().peek();
    assertEquals("foobar toto", process.getLine());
    assertEquals("* * * * *", process.getSchedulingPattern().toString());
    assertTrue(plugin.spawn());
    assertTrue(latch.await(10, TimeUnit.SECONDS));
    assertEquals(Arrays.asList("toto", "toto"), queue);
    assertEquals(2, plugin.getHistory().size());
  }
}
