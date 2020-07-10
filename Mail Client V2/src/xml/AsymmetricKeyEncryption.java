package xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.keys.KeyInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//Generise tajni kljuc
//Kriptije sadrzaj elementa student tajnim kljucem
//Kriptuje tajni kljuc javnim kljucem
//Kriptovani tajni kljuc se stavlja kao KeyInfo kriptovanog elementa
public class AsymmetricKeyEncryption {

	public static void testIt(String senderEmail, String recieverEmail) {
		String inFile = "./data/" + senderEmail + "_signed.xml";
		String outFile = "./data/" + senderEmail + "_enc.xml";
		
		// ucitava se dokument
		Document doc = loadDocument(inFile);
		
		// generise tajni session kljuc
		System.out.println("Generating secret key ....");
		SecretKey secretKey = generateDataEncryptionKey();
		
		// ucitava sertifikat za kriptovanje tajnog kljuca
		Certificate cert = readCertificate(recieverEmail);
		
		// kriptuje se dokument
		System.out.println("Encrypting....");
		doc = encrypt(doc, secretKey, cert);
		
		// snima se tajni kljuc
		// snima se dokument
		saveDocument(doc, outFile);
		
		System.out.println("Encryption done");
	}

	/**
	 * Kreira DOM od XML dokumenta
	 */
	public static Document loadDocument(String file) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(new File(file));

			return document;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}

	/**
	 * Ucitava sertifikat is KS fajla alias primer
	 */
	private static Certificate readCertificate(String email) {
		try {
			String keyStoreFile = "./data/" + email +".jks";
			
			// kreiramo instancu KeyStore
			KeyStore ks = KeyStore.getInstance("JKS", "SUN");
			// ucitavamo podatke
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(keyStoreFile));
			ks.load(in, "123".toCharArray());

			if (ks.isKeyEntry(email)) {
				Certificate cert = ks.getCertificate(email);
				return cert;
			} else
				return null;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}

	/**
	 * Snima DOM u XML fajl
	 */
	private static void saveDocument(Document doc, String fileName) {
		try {
			File outFile = new File(fileName);
			FileOutputStream f = new FileOutputStream(outFile);

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(f);

			transformer.transform(source, result);

			f.close();

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Generise tajni kljuc
	 */
	private static SecretKey generateDataEncryptionKey() {

		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede"); // Triple
																			// DES
			return keyGenerator.generateKey();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Kriptuje sadrzaj prvog elementa odsek
	 */
	private static Document encrypt(Document doc, SecretKey key, Certificate certificate) {

		try {

			// cipher za kriptovanje XML-a
			XMLCipher xmlCipher = XMLCipher.getInstance(XMLCipher.TRIPLEDES);
			
			// inicijalizacija za kriptovanje
			xmlCipher.init(XMLCipher.ENCRYPT_MODE, key);

			// cipher za kriptovanje tajnog kljuca,
			// Koristi se Javni RSA kljuc za kriptovanje
			XMLCipher keyCipher = XMLCipher.getInstance(XMLCipher.RSA_v1dot5);
			
			// inicijalizacija za kriptovanje tajnog kljuca javnim RSA kljucem
			keyCipher.init(XMLCipher.WRAP_MODE, certificate.getPublicKey());
			
			// kreiranje EncryptedKey objekta koji sadrzi  enkriptovan tajni (session) kljuc
			EncryptedKey encryptedKey = keyCipher.encryptKey(doc, key);
			
			// u EncryptedData element koji se kriptuje kao KeyInfo stavljamo
			// kriptovan tajni kljuc
			// ovaj element je koreni elemnt XML enkripcije
			EncryptedData encryptedData = xmlCipher.getEncryptedData();
			
			// kreira se KeyInfo element
			KeyInfo keyInfo = new KeyInfo(doc);
			
			// postavljamo naziv 
			keyInfo.addKeyName("Kriptovani tajni kljuc");
			
			// postavljamo kriptovani kljuc
			keyInfo.add(encryptedKey);
			
			// postavljamo KeyInfo za element koji se kriptuje
			encryptedData.setKeyInfo(keyInfo);

			// trazi se element ciji sadrzaj se kriptuje
			NodeList emailList = doc.getElementsByTagName("email");
			Element email = (Element) emailList.item(0);

			xmlCipher.doFinal(doc, doc.getDocumentElement(), true); // kriptuje sa sadrzaj

			return doc;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
	
}
