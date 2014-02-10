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
package org.crsh.mail;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.util.Utils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * The mail plugin integrates JavaMail for sending commands.
 *
 * @author Julien Viet
 */
public class MailPlugin extends CRaSHPlugin<MailPlugin> {

  /** . */
  public static PropertyDescriptor<String> SMTP_HOST = new PropertyDescriptor<String>(String.class, "mail.smtp.host", "localhost", "The mail server host") {
    @Override
    protected String doParse(String s) throws Exception {
      return s;
    }
  };

  /** . */
  public static PropertyDescriptor<Integer> SMTP_PORT = new PropertyDescriptor<Integer>(Integer.class, "mail.smtp.port", 25, "The mail server port") {
    @Override
    protected Integer doParse(String s) throws Exception {
      return Integer.parseInt(s);
    }
  };

  /** . */
  public static PropertyDescriptor<SmtpSecure> SMTP_SECURE = new PropertyDescriptor<SmtpSecure>(SmtpSecure.class, "mail.smtp.secure", SmtpSecure.NONE, "The mail server port") {
    @Override
    protected SmtpSecure doParse(String s) throws Exception {
      return SmtpSecure.valueOf(s.toUpperCase());
    }
  };

  /** . */
  public static PropertyDescriptor<String> SMTP_USERNAME = new PropertyDescriptor<String>(String.class, "mail.smtp.username", null, "The mail server user name") {
    @Override
    protected String doParse(String s) throws Exception {
      return s;
    }
  };

  /** . */
  public static PropertyDescriptor<String> SMTP_PASSWORD = new PropertyDescriptor<String>(String.class, "mail.smtp.password", null, "The mail server passord", true) {
    @Override
    protected String doParse(String s) throws Exception {
      return s;
    }
  };

  /** . */
  public static PropertyDescriptor<String> SMTP_FROM = new PropertyDescriptor<String>(String.class, "mail.smtp.from", null, "The mail sender address") {
    @Override
    protected String doParse(String s) throws Exception {
      return s;
    }
  };

  /** . */
  public static PropertyDescriptor<Boolean> DEBUG = new PropertyDescriptor<Boolean>(Boolean.class, "mail.debug", false, "The mail smtp debug mode") {
    @Override
    protected Boolean doParse(String s) throws Exception {
      return Boolean.parseBoolean(s);
    }
  };

  @Override
  protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities() {
    return Utils.<PropertyDescriptor<?>>list(SMTP_HOST, SMTP_PORT, SMTP_SECURE, SMTP_USERNAME, SMTP_PASSWORD, SMTP_FROM, DEBUG);
  }

  /** . */
  private String smtpHost;

  /** . */
  private Integer smtpPort;

  /** . */
  private SmtpSecure smtpSecure;

  /** . */
  private String smtpUsername;

  /** . */
  private String smtpPassword;

  /** . */
  private String smtpFrom;

  /** . */
  private Boolean debug;

  @Override
  public MailPlugin getImplementation() {
    return this;
  }

  @Override
  public void init() {
    smtpHost = getContext().getProperty(SMTP_HOST);
    smtpPort = getContext().getProperty(SMTP_PORT);
    smtpSecure = getContext().getProperty(SMTP_SECURE);
    smtpUsername = getContext().getProperty(SMTP_USERNAME);
    smtpPassword = getContext().getProperty(SMTP_PASSWORD);
    smtpFrom = getContext().getProperty(SMTP_FROM);
    debug = getContext().getProperty(DEBUG);
  }

  public Future<Boolean> send(
      Iterable<String> recipients,
      final String subject,
      final String body,
      final DataSource... attachments) throws MessagingException {
    return send(recipients, subject, body, null, attachments);
  }
  public Future<Boolean> send(
      Iterable<String> recipients,
      final String subject,
      final Object body,
      final String type,
      final DataSource... attachments) throws MessagingException {

    //
    final InternetAddress[] addresses = InternetAddress.parse(Utils.join(recipients, ","));

    //
    Callable<Boolean> f = (new Callable<Boolean>() {
      public Boolean call() throws Exception {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", smtpHost);
        if (smtpPort != null) {
          props.setProperty("mail.smtp.port", Integer.toString(smtpPort));
        }

        //
        final String username = smtpUsername, password = smtpPassword;
        Authenticator authenticator;
        if (username != null && password != null) {
          props.setProperty("mail.smtp.auth", "true");
          authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication(username, password);
            }
          };
        } else {
          authenticator = null;
        }

        //
        if (Boolean.TRUE.equals(debug)) {
          props.setProperty("mail.debug", "true");
        }

        //
        if (smtpSecure != null) {
          switch (smtpSecure) {
            case NONE:
              break;
            case TLS:
              props.setProperty("mail.smtp.starttls.enable", "true");
              break;
            case SSL:
              throw new UnsupportedOperationException();
          }
        }

        //
        Session session = Session.getInstance(props, authenticator);
        MimeMessage message = new MimeMessage(session);

        //
        if (smtpFrom != null) {
          message.setFrom(new InternetAddress(smtpFrom));
        }

        //
        message.setRecipients(Message.RecipientType.TO, addresses);
        if (subject != null) {
          message.setSubject(subject);
        }

        //
        MimePart bodyPart;
        if (attachments != null && attachments.length > 0) {
          Multipart multipart = new MimeMultipart();
          bodyPart = new MimeBodyPart();
          bodyPart.setContent(body, type);
          for (DataSource attachment : attachments) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setDataHandler(new DataHandler(attachment));
            attachmentPart.setFileName(attachment.getName());
            multipart.addBodyPart(attachmentPart);
          }
          message.setContent(multipart);
        } else {
          bodyPart = message;
        }

        //
        if (type != null) {
          bodyPart.setContent(body, type);
        } else {
          bodyPart.setText(body.toString());
        }

        //
        try {
          Transport.send(message);
        }
        catch (AuthenticationFailedException e) {
          return false;
        }
        return true;
      }
    });

    //
    return getContext().getExecutor().submit(f);
  }
}
