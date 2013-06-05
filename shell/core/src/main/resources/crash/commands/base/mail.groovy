package crash.commands.base

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Option
import org.crsh.cli.Usage
import org.crsh.command.PipeCommand
import org.crsh.text.Chunk
import org.crsh.text.Text


import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;


class mail {
    @Usage("Mails crash threads")
    @Command
    PipeCommand<Chunk, String> main(@Argument String mailto) {

        // A ADAPTER
        String login = "labaixbidouille@gmail.com"
        String pass = ""


        new PipeCommand<Chunk, String>() {
            StringBuffer buffer = new StringBuffer()

            @Override
            void provide(Chunk element) {
                if(element instanceof Text)
                    buffer.append(element.text)
            }

            @Override
            void flush() throws org.crsh.command.ScriptException, IOException {

                Properties props = new Properties()
                props.put("mail.smtp.auth", "true")
                props.put("mail.smtp.starttls.enable", "true")
                props.put("mail.smtp.host", "smtp.gmail.com")
                props.put("mail.smtp.port", "587")

                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(login, pass)
                            }})


                try {
                    MimeMessage msg = new MimeMessage(session)
                    msg.setRecipients(Message.RecipientType.TO, mailto)

                    msg.setSubject("Listing of crash threads")
                    msg.setSentDate(new Date())
                    msg.setText("<pre>" + buffer.toString() + "</pre>", "utf-8", "html")
                    Transport.send(msg)

                    System.out.println("Mail sent to "+ mailto)

                } catch (MessagingException mex) {
                    System.out.println("send failed, exception: " + mex)
                }

                super.flush()
            }
        }
    }
}