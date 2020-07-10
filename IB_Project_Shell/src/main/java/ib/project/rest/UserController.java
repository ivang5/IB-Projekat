package ib.project.rest;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ib.project.certificate.CertificateGenerator;
import ib.project.keystore.KeyStoreWriter;
import ib.project.model.Authority;
import ib.project.model.User;
import ib.project.service.AuthorityService;
import ib.project.service.UserService;

@RestController
@RequestMapping(value="api/users")
public class UserController {

	@Autowired
	public UserService userService;
	@Autowired
	public AuthorityService authorityService;
	
	private static KeyStoreWriter keyStoreWriter = new KeyStoreWriter();
	private static CertificateGenerator certificateGenerator = new CertificateGenerator();
	
	@GetMapping(path="/")
	public ArrayList<User> findAll() {
		return userService.findAll();
	}
	
	@GetMapping(path="user/email")
	public ResponseEntity<User> userEmail(@RequestParam String email) {
		User user = userService.findByEmail(email);
		if (user != null) {
			return new ResponseEntity<User>(user,HttpStatus.OK);
		} else {
			System.out.println("User with given email doesn't exist!");
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping(path="user/jks")
	public ResponseEntity<String> userJks(@RequestParam String email) {
		User user = userService.findByEmail(email);
		if (user != null) {
			char[] password = "123".toCharArray();
			String keyStoreFile = "C:\\Users\\Pecar\\Desktop\\IB\\Mail Client V2\\data\\" + email + ".jks";
			
			// ucitavanje KeyStore fajla
			// prosledjujemo null kao prvi parametar jer fajl trenutno ne postoji
			KeyStore keyStore = keyStoreWriter.loadKeyStore(null, password);
			
			// cuvanje fajla na disku
			keyStoreWriter.saveKeyStore(keyStore, keyStoreFile, password);
			
			// generisemo par kljuceva za seritifkat koji se generise
			KeyPair keyPair = certificateGenerator.generateKeyPair();
			
			// generisemo Self-Signed sertifikat
			X509Certificate certificate = certificateGenerator.generateSelfSignedCertificate(keyPair, email);
			
			// ucitavanje KeyStore fajla
			keyStore = keyStoreWriter.loadKeyStore(keyStoreFile, password);
			
			// upisivanje u KeyStore, dodaju se kljuc i sertifikat
			keyStoreWriter.addToKeyStore(keyStore, email, keyPair.getPrivate(), password, certificate);
			
			// cuvanje izmena na disku
			keyStoreWriter.saveKeyStore(keyStore, keyStoreFile, password);
			
			// postavljanje sertifikata useru i cuvanje izmena
			user.setCertificate(keyStoreFile);
			userService.save(user);
			
			// ispisivanje sertifikata na konzoli
			certificateGenerator.printCertificate(certificate);
			
			System.out.println("Napravljena JKS datoteka");
			return new ResponseEntity<String>("", HttpStatus.OK);
		}else {
			System.out.println("User with given email doesn't exist!");
			return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
		}
	}
	
	@PostMapping(path="user/login")
	public ResponseEntity<User> loginUser(@RequestParam String email, @RequestParam String password) {
		User user = userService.findByEmailAndPassword(email, password);
		try {
			return new ResponseEntity<User>(user, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println("User with given email and password doesn't exist!");
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@PostMapping(path="user/registration")
	public ResponseEntity<User> registrationUser(@RequestParam String email, @RequestParam String password) {
		Authority auth = authorityService.findByName("Regular");
		User user = new User();
		User checkUser = userService.findByEmail(email);
		if (checkUser == null) {
			user.setActive(false);
			user.setAuthority(auth);
			user.setCertificate("");
			user.setEmail(email);
			user.setPassword(password);
			
			userService.save(user);
			return new ResponseEntity<User>(user,HttpStatus.CREATED);
		}else {
			System.out.println("Email already exists in database!");
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
	}
	
	@PostMapping(path="user/activate")
	public ResponseEntity<String> activateUser(@RequestParam String email) {
		userService.activateUser(email);
		return new ResponseEntity<String>(email,HttpStatus.OK);
	}
	
}
