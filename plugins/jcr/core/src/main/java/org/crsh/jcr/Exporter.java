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
package org.crsh.jcr;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Exporter extends DefaultHandler {

  /** . */
  private final Map<String, String> mappings;

  /** . */
  private FileSystem fs;

  public Exporter(FileSystem fs) {
    this.mappings = new HashMap<String, String>();
    this.fs = fs;
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    mappings.put(prefix, uri);
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {
    mappings.remove(prefix);
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    try {
      String fileName = XML.fileName(qName);
      fs.beginDirectory(fileName);

      //
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      StreamResult streamResult = new StreamResult(out);

      //
      SAXTransformerFactory tf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

      //
      TransformerHandler hd = tf.newTransformerHandler();
      Transformer serializer = hd.getTransformer();
      serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
      serializer.setOutputProperty(OutputKeys.INDENT,"yes");
      hd.setResult(streamResult);

      //
      hd.startDocument();

      //
      for (Map.Entry<String, String> mapping : mappings.entrySet()) {
        String prefix = mapping.getKey();
        hd.startPrefixMapping(prefix, mapping.getValue());
      }

      //
      hd.startElement(uri, localName, qName, attributes);
      hd.endElement(uri, localName, qName);

      //
      for (String prefix : mappings.keySet()) {
        hd.endPrefixMapping(prefix);
      }

      //
      hd.endDocument();

      //
      out.close();
      byte[] content = out.toByteArray();
      fs.file("crash__content.xml", content.length, new ByteArrayInputStream(content));
    }
    catch (Exception e) {
      throw new SAXException(e);
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    try {
      String fileName = XML.fileName(qName);
      fs.endDirectory(fileName);
    }
    catch (IOException e) {
      throw new SAXException(e);
    }
  }
}
