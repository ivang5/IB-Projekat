package support;
import java.io.IOException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailHelper {
	/**
     * Return the primary text content of the message.
     * Source: https://gist.github.com/nutanc/c0d3fe354a8fdb75e1ef
     */
	// Ova metoda se koristila za kontrolnu tacku
	// Izvlacenje teksta iz poruke
    public static String getText(Part p) throws
            MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String) p.getContent();
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer HTML text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null) {
                        text = getText(bp);
                    }
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null) {
                        return s;
                    }
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null) {
                    return s;
                }
            }
        }

        return null;
    }
    
    
    // Ova metoda se koristila za kontrolnu tacku
    // Kreiranje poruke
    public static MimeMessage createMimeMessage(String reciever,String subject, String  body) throws MessagingException {
    	
    	Properties props = new Properties();
	    Session session = Session.getDefaultInstance(props, null);
    	MimeMessage message = new MimeMessage(session);
    	
    	message.setRecipient(Message.RecipientType.TO, new InternetAddress(reciever));
    	message.setSubject(subject);
    	message.setText(body);
    	
    	return message;
    }
    
    // Preuzimanje attachment-a iz poruke
    public static void getAttachment (MimeMessage chosenMessage) throws MessagingException, IOException {
    	
    	String contentType = chosenMessage.getContentType();
    	
    	if (contentType.contains("multipart")) {
    		// content may contain attachments
    		Multipart multiPart = (Multipart) chosenMessage.getContent();
            int numberOfParts = multiPart.getCount();
            for (int partCount = 0; partCount < numberOfParts; partCount++) {
                MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    // this part is attachment
                    String fileName = part.getFileName();
                    part.saveFile(fileName);
                } else {
                    // this part may be the message content
                    continue;
                }
            }
    	}    	
    }
    
    // Kreiranje poruke
    public static MimeMessage createMimeMessage(String reciever, String filename) throws MessagingException {
    	
    	Properties props = new Properties();
	    Session session = Session.getDefaultInstance(props, null);
    	MimeMessage message = new MimeMessage(session);

    	BodyPart messageBodyPart1 = new MimeBodyPart();  
        messageBodyPart1.setText("This message is encrypted");
    	
    	BodyPart messageBodyPart = new MimeBodyPart();
    	DataSource source = new FileDataSource(filename);
    	messageBodyPart.setDataHandler(new DataHandler(source));
    	messageBodyPart.setFileName(filename);
    	
    	Multipart multipart = new MimeMultipart();
    	multipart.addBodyPart(messageBodyPart1);
    	multipart.addBodyPart(messageBodyPart);
    	
    	message.setSubject("Encrypted message");
    	message.setRecipient(Message.RecipientType.TO, new InternetAddress(reciever));
    	message.setContent(multipart);
    	
    	return message;
    }
}
