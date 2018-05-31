package com.reactlibrarymc;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableArray;

import java.security.Security;
import java.security.Provider;
import java.security.AccessController;
import java.util.Properties;
import java.io.File;
import java.io.ByteArrayOutputStream;

import android.util.Base64;
import android.os.AsyncTask;
import android.util.Log;

import javax.mail.Multipart;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.Authenticator;
import javax.activation.*;

public class RNMailSMTPModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNMailSMTPModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNMailSMTP";
  }

  @ReactMethod
  public void sendMail(final ReadableMap obj, final Promise promise){
      AsyncTask.execute(new Runnable() {

      String mailhost = obj.getString("mailhost");
      String port = obj.getString("port");
      String body = obj.getString("htmlBody");
      String username = obj.getString("username");
      String password = obj.getString("password");
      String subject = obj.getString("subject");
      String from = obj.getString("from");
      String recipients = obj.getString("recipients");
      Boolean attachment = obj.getBoolean("attachment");
      Boolean ssl = obj.getBoolean("ssl");
      String format = obj.getString("format");
      ReadableArray img = obj.getArray("img");
      Boolean imgInAttach = obj.getBoolean("imgInAttach");

        @Override
        public void run() {
          try {
            GMailSender sender = new GMailSender(username, password, mailhost, port, ssl);
            sender.sendMail(subject, body, from, recipients, attachment, format, img, imgInAttach);
            WritableMap result = Arguments.createMap();
            result.putString("status", "SUCCESS");
            promise.resolve(result);
          } catch (Exception e) {
            promise.reject("status", e.getMessage());
          } finally {
          }
        }
    });
  }
}

// class Transfer

class GMailSender {

  private String user;
  private String password;
  private Session session;
 
  static {
    Security.addProvider(new JSSEProvider());
  }
 
  public GMailSender(String user, String password, String mailhost, String port, Boolean ssl) {

    this.user = user;
    this.password = password;

    Properties props = new Properties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.host", mailhost);
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.port", port);
    props.put("mail.smtp.socketFactory.port", port);
    props.put("mail.smtp.socketFactory.fallback", "false");
    props.put("mail.smtp.quitwait", "false");
    if (ssl) {
      props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
    } else {
      props.put("mail.smtp.starttls.enable", "true");
    }

    Authenticator auth = new SMTPAuthenticator();

    session = Session.getInstance(props, auth);
  }

  private class SMTPAuthenticator extends javax.mail.Authenticator {
    public PasswordAuthentication getPasswordAuthentication() {
       return new PasswordAuthentication(user, password);
    }
  }

  public synchronized void sendMail(String subject, String body,
    String sender, String recipients, Boolean attachment, String format, 
    ReadableArray img, Boolean imgInAttach) throws Exception {

    String xml = new String("XML");
    MimeMessage msg = new MimeMessage(session);
    Multipart multipart = new MimeMultipart();
    MimeBodyPart part = new MimeBodyPart();

    Transport transport = session.getTransport();

    msg.setSender(new InternetAddress(sender));
    msg.setSubject(subject);

    if (attachment) {
      if (format.equals(xml)) {
        part.setHeader("Content-Type", "text/xml; name=\"form.xml\"");
      } else {
        part.setHeader("Content-Type", "application/json; name=\"form.json\"");
      }
    } else {
      part.setText(body);
    }
    multipart.addBodyPart(part);
    
    // Integer size = img.size();
    // for (int i = 0; i < size; i++) {
    //   Log.e("sendMail", img.getString(i));
    // }
    String filePath = "/sdcard/DCIM/1.jpg";
    
    part = new MimeBodyPart();
    // DataSource fds = new FileDataSource(filePath);
    // part.setDataHandler(new DataHandler(fds));

    // Transport tr = session.getTransport("smtp");
    // tr.connect(smtphost, username, password);
    // msg.saveChanges();	// don't forget this
    // tr.sendMessage(msg, msg.getAllRecipients());
    // tr.close();

    multipart.addBodyPart(part);

    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));

    msg.setContent(multipart);

    msg.saveChanges();

    transport.send(msg);
    transport.close();
    }
}

class JSSEProvider extends Provider { 
  public JSSEProvider() {
    super("HarmonyJSSE", 1.0, "Harmony JSSE Provider");
    AccessController.doPrivileged(new java.security.PrivilegedAction<Void>() {
      public Void run() {
        put("SSLContext.TLS", "org.apache.harmony.xnet.provider.jsse.SSLContextImpl");
        put("Alg.Alias.SSLContext.TLSv1", "TLS");
        put("KeyManagerFactory.X509", "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl");
        put("TrustManagerFactory.X509", "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl");
        return null;
      }
    });
  }
}
