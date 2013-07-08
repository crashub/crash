package crash.commands.base

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Option
import org.crsh.cli.Usage
import org.crsh.command.PipeCommand
import org.crsh.text.Chunk
import org.crsh.text.Text

import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.MimeMessage


class mail {
    @Usage("mail input text")
    @Command
    PipeCommand<Chunk, String> main(
    	@Usage("mail subject")
    	@Option(names=["s","subject"])
    	String subject,
    	@Usage("mail receivers")
    	@Argument List<String> mailtoList) {

        // à adapter avec crash.properties
        String login = "monmail@gmail.com"
        String pass = "monpass"
		
		if (subject == null)
			subject = "Listing of crash threads"
		

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
                    
                    for(String mailto : mailtoList ) {
                    	msg.addRecipients(Message.RecipientType.TO, mailto)
					}

                    msg.setSubject(subject)
                    msg.setSentDate(new Date())
                    msg.setText("<pre>" + buffer.toString() + "</pre>", "utf-8", "html")
                    Transport.send(msg)

                    System.out.println("Mails sent !")

                } catch (MessagingException mex) {
                    System.out.println("Send failed !\nException: " + mex)
                }

                super.flush()
            }
        }
    }
}
