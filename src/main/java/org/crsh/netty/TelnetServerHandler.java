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
package org.crsh.netty;

import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.crsh.Info;
import org.crsh.display.SimpleDisplayContext;
import org.crsh.display.structure.Element;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellBuilder;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles a server-side channel.
 *
 * @author The Netty Project (netty-dev@lists.jboss.org)
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev$, $Date$
 */
@ChannelPipelineCoverage("all")
public class TelnetServerHandler extends SimpleChannelUpstreamHandler {

  /** . */
  private final ShellBuilder builder;

  public TelnetServerHandler(ShellBuilder builder) {
    this.builder = builder;
  }

  private static final Logger logger = Logger.getLogger(
    TelnetServerHandler.class.getName());

  @Override
  public void handleUpstream(
    ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    if (e instanceof ChannelStateEvent) {
      logger.info(e.toString());
    }
    super.handleUpstream(ctx, e);
  }

  @Override
  public void channelConnected(
    ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    // Build the shell
    Shell shell = builder.build();

    //
    String prompt = shell.getPrompt();

    // Send greeting for a new connection.
    Channel channel = e.getChannel();

    //
    channel.write("CRaSH " + Info.getVersion() + " (http://crsh.googlecode.com)\r\n");
    channel.write("Welcome to " + InetAddress.getLocalHost().getHostName() + "!\r\n");
    channel.write("It is " + new Date() + " now.\r\n");
    channel.write(prompt);

    //
    ctx.setAttachment(shell);

  }

  @Override
  public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    // Get shell
    Shell shell = (Shell)ctx.getAttachment();

    // Free resources
    shell.close();
  }

  @Override
  public void messageReceived(
    ChannelHandlerContext ctx, MessageEvent e) {
    // Cast to a String first.
    // We know it is a String because we put some codec in TelnetPipelineFactory.
    String request = (String)e.getMessage();

    // Get shell
    Shell shell = (Shell)ctx.getAttachment();

    //
    boolean close = false;
    String response = null;
    if ("bye".equals(request)) {
      close = true;
      response = "Have a good day!\r\n";
    }
    else {
      try {
        // Evaluate
        List<Element> elements = shell.evaluate(request);

        //
        String result = null;
        if (elements != null) {
          SimpleDisplayContext context = new SimpleDisplayContext("\r\n");
          for (Element element : elements) {
            element.print(context);
          }
          result = context.getText();
        }

        // Format response if any
        if (result != null) {
          response = "" + String.valueOf(result) + "\r\n";
        }
      }
      catch (Throwable t) {
        if (t instanceof InvokerInvocationException) {
          t = t.getCause();
        }
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        printer.print("ERROR: ");
        t.printStackTrace(printer);
        printer.println();
        printer.close();
        response = writer.toString();
      }
    }

    //
    if (!close) {
      String prompt = shell.getPrompt();

      //
      response += prompt;
    }


    // We do not need to write a ChannelBuffer here.
    // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
    if (response != null) {
      ChannelFuture future = e.getChannel().write(response);

      // Close the connection after sending 'Have a good day!'
      // if the client has sent 'bye'.
      if (close) {
        future.addListener(ChannelFutureListener.CLOSE);
      }
    }
  }

  @Override
  public void exceptionCaught(
    ChannelHandlerContext ctx, ExceptionEvent e) {
    logger.log(
      Level.WARNING,
      "Unexpected exception from downstream.",
      e.getCause());
    e.getChannel().close();
  }
}