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
package org.crsh.stream;

import org.crsh.AbstractTestCase;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

/** @author Julien Viet */
public class ProducerTestCase extends AbstractTestCase {


  static class ConsumerImpl<C> extends LinkedList<C> implements Consumer<C> {

    /** . */
    private final Class<C> type;

    ConsumerImpl(Class<C> type) {
      this.type = type;
    }

    public Class<C> getConsumedType() {
      return type;
    }

    public void provide(C element) throws IOException {
      add(element);
    }

    public void flush() throws IOException {
      throw new UnsupportedOperationException();
    }
  }

  static class ProducerImpl<P, C extends Consumer<? super P>> implements Producer<P, C> {

    /** . */
    private final Class<P> type;

    /** . */
    private C consumer;

    ProducerImpl(Class<P> type) {
      this.type = type;
      this.consumer = null;
    }

    public Class<P> getProducedType() {
      return type;
    }

    public void open(C consumer) {
      this.consumer = consumer;
    }

    public void provide(P product) throws Exception {
      consumer.provide(product);
    }

    public void close() throws IOException {
      throw new UnsupportedOperationException();
    }
  }

  public void testSuperType() throws Exception {
    Consumer<Object> consumer = new ConsumerImpl<Object>(Object.class);
    ProducerImpl<String, Consumer<Object>> producer = new ProducerImpl<String, Consumer<Object>>(String.class);
    producer.open(consumer);
    producer.provide("foo");
    assertEquals(Arrays.<Object>asList("foo"), consumer);
  }

  public void testSameType() throws Exception {
    Consumer<String> consumer = new ConsumerImpl<String>(String.class);
    ProducerImpl<String, Consumer<String>> producer = new ProducerImpl<String, Consumer<String>>(String.class);
    producer.open(consumer);
    producer.provide("foo");
    assertEquals(Arrays.<Object>asList("foo"), consumer);
  }
}
