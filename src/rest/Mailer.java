package rest;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class Mailer {

    public static final String PRESET_MAIL = "${fullname}, please verify your email address. \nYour verification code is ${code}.";

    public static void sendHTMLMail(String to, String name, String fromName, String subject, String html) throws EmailException, MalformedURLException {
        // Create the email message
        HtmlEmail email = new HtmlEmail();
        email.setHostName("smtp.gmail.com");
        email.setSmtpPort(465);
        email.setAuthenticator(new DefaultAuthenticator("arpansri98@gmail.com", "Arpan@IITyes"));
        email.setSSLOnConnect(true);
        email.addTo(to, name);
        email.setFrom("arpansri98@gmail.com", fromName);
        email.setSubject(subject);

        // embed the image and get the content id

        // set the html message
        email.setHtmlMsg(html);

        // set the alternative message
        email.setTextMsg("Your email client does not support HTML messages");

        // send the email
        email.send();
        System.out.println("Mail Sent bro.");
    }

    public static void sendMail(String to, String subject, String content) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("arpansri98@gmail.com", "Arpan@IITyes");
            }
        });

        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(content);

            Transport.send(mimeMessage);
            System.out.println("Mail sent.");

        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }


}
