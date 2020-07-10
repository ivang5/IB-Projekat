package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.xml.transform.TransformerException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import signature.VerifySignatureEnveloped;
import support.MailHelper;
import support.MailReader;
import xml.AsymmetricKeyDecryption;
import xml.AsymmetricKeyEncryption;
import xml.CreateXMLDOM;

public class ReadMailClient extends MailClient {

	public static long PAGE_SIZE = 3;
	public static boolean ONLY_FIRST_PAGE = true;
	
	static {
		// staticka inicijalizacija
		Security.addProvider(new BouncyCastleProvider());
		org.apache.xml.security.Init.init();
	}
	
	public static void main(String[] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, MessagingException, NoSuchPaddingException, InvalidAlgorithmParameterException, KeyStoreException, CertificateException, UnrecoverableKeyException, TransformerException {
        // Build a new authorized API client service.
        Gmail service = getGmailService();
        ArrayList<MimeMessage> mimeMessages = new ArrayList<MimeMessage>();
        
        String sender = "";
        String reciever = "";
        
        String user = "me";
        String query = "is:unread label:INBOX";
        
        List<Message> messages = MailReader.listMessagesMatchingQuery(service, user, query, PAGE_SIZE, ONLY_FIRST_PAGE);
        for(int i=0; i<messages.size(); i++) {
        	Message fullM = MailReader.getMessage(service, user, messages.get(i).getId());
        	
        	MimeMessage mimeMessage;
			try {
				
				mimeMessage = MailReader.getMimeMessage(service, user, fullM.getId());
				
				sender = mimeMessage.getHeader("From", null);
				reciever = mimeMessage.getHeader("To", null);
				
				System.out.println("\nMessage number " + i);
				System.out.println("From: " + sender);
				System.out.println("Subject: " + mimeMessage.getSubject());
				System.out.println("Body: " + MailHelper.getText(mimeMessage));
				System.out.println("\n");
				
				mimeMessages.add(mimeMessage);
	        
			} catch (MessagingException e) {
				e.printStackTrace();
			}	
        }
        
        System.out.println("Select a message to decrypt:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	        
	    String answerStr = reader.readLine();
	    Integer answer = Integer.parseInt(answerStr);
	    
		MimeMessage chosenMessage = mimeMessages.get(answer);
		
		// Preuzimanje attachment-a iz mail-a
		MailHelper.getAttachment(chosenMessage);
		
		// Dekriptovanje
		AsymmetricKeyDecryption.testIt(sender, reciever);
		
		// Verifikovanje digitalnog potpisa
		VerifySignatureEnveloped.testIt(sender);
		
		// Ispis mail-a
		System.out.println("<--- EMAIL --->");
		System.out.println("From: " + sender);
		Document doc = AsymmetricKeyEncryption.loadDocument("./data/" + sender + "_dec.xml");
		CreateXMLDOM.printEmail(doc);
		
		
		// -----> KOD KOJI JE ISPOD POD KOMENTAROM SE KORISTIO ZA KONTROLNU TACKU <-----
	    
        /*//TODO: Decrypt a message and decompress it. The private key is stored in a file.
		Cipher aesCipherDec = Cipher.getInstance("AES/CBC/PKCS5Padding");
		//SecretKey secretKey = new SecretKeySpec(JavaUtils.getBytesFromFile(KEY_FILE), "AES");
		
		//Izvlacenje enkriptovane poruke, tajnog kljuca i inicijalizacionih vektora
		MailBody mailBody = new MailBody(MailHelper.getText(chosenMessage));
		IvParameterSpec ivParameterSpec1 = new IvParameterSpec(mailBody.getIV1Bytes());
		IvParameterSpec ivParameterSpec2 = new IvParameterSpec(mailBody.getIV2Bytes());
		byte[] secretKeyEnc = mailBody.getEncKeyBytes();
		String text = mailBody.getEncMessage();
		
		//Keystore
		char[] pwdArrayB = "userb".toCharArray();
		KeyStore ks = KeyStore.getInstance("JKS");
		
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(USERB_KEYSTORE));
		ks.load(in, pwdArrayB);
		PrivateKey pk = (PrivateKey) ks.getKey("userb", pwdArrayB);
		
		Cipher rsaCipherDec = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		rsaCipherDec.init(Cipher.DECRYPT_MODE, pk);
		byte[] decryptedKey = rsaCipherDec.doFinal(secretKeyEnc);
		
		SecretKey secretKey = new SecretKeySpec(decryptedKey, "AES");
		System.out.println("Dekriptovan kljuc: " + secretKey.hashCode());
		
		//inicijalizacija za dekriptovanje
		aesCipherDec.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec1);
		
		//dekompresovanje i dekriptovanje teksta
		String receivedBodyTxt = new String(aesCipherDec.doFinal(Base64.decode(text)));
		String decompressedBodyText = GzipUtil.decompress(Base64.decode(receivedBodyTxt));
		System.out.println("Body text: " + decompressedBodyText);
		
		//inicijalizacija za dekriptovanje
		aesCipherDec.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec2);
		
		//dekompresovanje i dekriptovanje subject-a
		String decryptedSubjectTxt = new String(aesCipherDec.doFinal(Base64.decode(chosenMessage.getSubject())));
		String decompressedSubjectTxt = GzipUtil.decompress(Base64.decode(decryptedSubjectTxt));
		System.out.println("Subject text: " + new String(decompressedSubjectTxt));*/
	}
}
