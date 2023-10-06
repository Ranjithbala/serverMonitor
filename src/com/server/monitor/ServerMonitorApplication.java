package com.server.monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;


public class ServerMonitorApplication {
    private static final Logger LOGGER = Logger.getLogger(ServerMonitorApplication.class.getName());
	
    static {
        try {
        	
        	File logFile = new File("server-monitor.log");	
            FileHandler fileHandler = new FileHandler(logFile.getPath());
            fileHandler.setFormatter(new CustomFormatter());
            LOGGER.addHandler(fileHandler);
        } catch (Exception e) {
        	LOGGER.log(Level.SEVERE, "Error configuring logger", e);
        }
    }
	   
	public static void main (String[]Args) throws InterruptedException {
		
		File serverConfigFile = new File("serverconfig.txt");
		if(!serverConfigFile.exists()) {
			try {
				serverConfigFile.createNewFile();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		LOGGER.info("|----Server Monitor Started Successfully");
		boolean checkConnection =true ; 
		while(checkConnection) {
			//Check for internet connection
			if(isInternetConnected()) {
				checkConnection = false;
			}else {
				TimeUnit.MINUTES.sleep(1);
			}
		}
		checkAndEmailServerIP();
		LOGGER.info("\t|----Server Monitor ENDED Successfully");
	}
		
	public static void checkAndEmailServerIP() {
		LOGGER.info("Simple");
		String[] publicIpChecker = new String[] {"curl ifconfig.me","curl -4/-6 icanhazip.com","curl ipinfo.io/ip","curl api.ipify.org","curl checkip.dyndns.org"}; 
		String currentIP = "";
		for(int i=0;i<publicIpChecker.length;i++) {
			currentIP = executeCommand(publicIpChecker[i]);
			if(!currentIP.isEmpty()) {
				break;
			}
		}
		LOGGER.info("Current IP : "+currentIP);
		
		File serverConfigFile = new File("serverconfig.txt");
		
		String oldIP = readContent(serverConfigFile);
		if(!oldIP.contentEquals(currentIP)) {
			if(sendEmail(new String[]{"ranjithbala66@gmail.com"},null,"HI BOSS\n\t Your Server Address has changed from "+oldIP +" to "+currentIP)) {
				writeContent(serverConfigFile,currentIP);
				LOGGER.info("Server Config is updated with new IP : "+currentIP);
			}
		}else {
			LOGGER.info("Server IP Not Changed");
		}
		
	}
	public static boolean isInternetConnected() {
		boolean isConnected =false;
		LOGGER.info("|----Checking Internet connection....");
		try {
			final URL url = new URL("http://www.google.com");
			URLConnection connection = url.openConnection();
			connection.connect();
	        connection.getInputStream().close();
	        LOGGER.info("\t|---Internet Connection is Good !!!");
	        return true;
		}catch(Exception e) {
			LOGGER.warning("\t|---NO INTERNET CONNECTION !!!");
			e.printStackTrace();
		}
		 
		return isConnected;
	}
	
	public static void getDnsRecords(String domainName,String codeToVerify) {
		try {
			Record[] dnsRecords = new Lookup(domainName,Type.TXT).run();
			if (dnsRecords != null) {
				
				int javaDNSLen = dnsRecords.length;
				LOGGER.info("Checking DNS Records==> Result Count  "+javaDNSLen );
				for (int i = 0; i < javaDNSLen; i++) {
					System.out.printf("DNS %s/n", i, dnsRecords[i]);
					
				}

			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		 
	}
	public static String executeCommand(String command) {
		StringBuilder textBuilder = new StringBuilder();
		try {
//			String command = "curl ipinfo.io/ip";
			LOGGER.info("|---Executings "+command);
			Process process = Runtime.getRuntime().exec(command);
			InputStream inputStream = process.getInputStream();
			
		    try (Reader reader = new BufferedReader(new InputStreamReader
		      (inputStream, StandardCharsets.UTF_8))) {
		        int c = 0;
		        while ((c = reader.read()) != -1) {
		            textBuilder.append((char) c);
		        }
		    }
		    LOGGER.info("\t|----Result: "+textBuilder.toString());
			
		}catch(Exception e ) {
			System.out.print(e.getStackTrace().toString());
		}
		return textBuilder.toString();
	}
	public static boolean sendEmail(String[] to,String subject,String body) {
		boolean isSent =false;
		  LOGGER.info("|---Sending Email Message..");
		  if(subject==null) {
			  subject = "RANT SERVER ALERT";
		  }
//		  String to = "mailto:ranjithbala66@gmail.com";//change accordingly  
	  
	      String userName = "shipcrm2@gmail.com";
	      String password = "zpymyuwqbdxxydli";
	      Properties props = new Properties();  
	      props.put("mail.smtp.host","smtp.gmail.com");  
	      props.put("mail.smtp.auth", "true");
	      props.put("mail.smtp.starttls.enable", "true");
	      props.put("mail.smtp.ssl.protocols", "TLSv1.2");
//	      props.put("mail.smtp.starttls.enable", "true");
	      props.put("mail.smtp.port", "587");
	      props.put("mail.debug", "true");
	      
	      
	      
//	      props.put("mail.transport.protocol", "smtp");
//		    props.put("mail.smtp.auth", "true");
//		    props.put("mail.smtp.starttls.enable", "true");
//		    props.put("mail.debug", "true");
//		    props.put("mail.smtp.ssl.protocols", "TLSv1.2");
	      Session session = Session.getDefaultInstance(props,  
	    		    new javax.mail.Authenticator() {  
	    		      protected PasswordAuthentication getPasswordAuthentication() {  
	    		    return new PasswordAuthentication(userName,password);  
	    		      }  
	    		    });   
	  
	      try{  
	    	  	    	 
	         MimeMessage message = new MimeMessage(session);  
	         message.setFrom();  
//	         message.addRecipient(Message.RecipientType.TO,);  
	         message.addRecipients(Message.RecipientType.TO,String.join(",", to));  
	         message.setSubject(subject);  
	         message.setText(body); 
//	         System.setProperty("mail.pop3s.ssl.protocols", "TLSv1.2");
//	         System.setProperty("mail.transport.protocol", "smtp");
	         LOGGER.info("|---MESSAGE - "
	        		 + "\n\t\tSubject : "+message.getRecipients(Message.RecipientType.TO).toString()
	         		+ "\n\t\tSubject : "+message.getSubject()
	         			+"\n\t\tBody : "+message.getContent().toString()					
	        		 );
	         // Send message  
	         Transport.send(message);  
	         LOGGER.info("message sent successfully....");  
	         isSent =true;
	      }catch (Exception e) {
	    	  e.printStackTrace();
	      } 
	      return isSent;
	}
	
	public static void writeContent(File file,String content) {
//		 File path = new File("C:\\Users\\HP\\Desktop\\gfg.txt");
		if(file!=null) {
			try {
		    	   //passing file instance in filewriter
			        FileWriter wr = new FileWriter(file);
			 
			        //calling writer.write() method with the string
			        wr.write(content);
			         
			        //flushing the writer
			        wr.flush();
			         
			        //closing the writer
			        wr.close();
		       }catch(Exception e) {
		    	   e.printStackTrace();
		       }
		}
	       
	     
	}
	
	public static String readContent(File file) {
		String fileContent = "";
		LOGGER.info("Reading File ->"+file.getName()); 
		try {
		      Scanner myReader = new Scanner(file);
		      while (myReader.hasNextLine()) {
		    	  fileContent =fileContent+ myReader.nextLine();
		      }
		      LOGGER.info(fileContent);
		      myReader.close();
		    } catch (FileNotFoundException e) {
		      LOGGER.info("An error occurred.");
		      e.printStackTrace();
		    }
		 return fileContent; 
	}
}
