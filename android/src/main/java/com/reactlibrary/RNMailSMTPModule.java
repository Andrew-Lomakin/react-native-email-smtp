package com.reactlibrarymc;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Multipart;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.util.ArrayList;

import java.security.Provider;
import java.security.AccessController;

import android.util.Log;
import android.os.StrictMode;

// String fileurl = "/sdcard/Download/1.pdf";

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
    getCurrentActivity().runOnUiThread(new Runnable() {

      String mailhost = obj.getString("mailhost");
      String port = obj.getString("port");
      String body = obj.getString("htmlBody");
      String username = obj.getString("username");
      String password = obj.getString("password");
      String subject = obj.getString("subject");
      String from = obj.getString("from");
      String to = obj.getString("to");
      Boolean attachment = obj.getBoolean("attachment");
      String format = obj.getString("format");
      
      @Override
      public void run() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); 
        try {
          GMailSender sender = new GMailSender(username, password, mailhost, port);
          sender.sendMail(subject, body, from, to, attachment, format);
          WritableMap result = Arguments.createMap();
            result.putString("status", "SUCCESS");
            promise.resolve(result);
        } catch (Exception e) {
          promise.reject("status", e.getMessage());
        }
      }
    });
  }
}

class ByteArrayDataSource implements DataSource {
  private byte[] data;
  private String type;
 
  public ByteArrayDataSource(byte[] data, String type) {
    super();
    this.data = data;
    this.type = type;
  }
 
  public ByteArrayDataSource(byte[] data) {
    super();
    this.data = data;
  }
 
  public void setType(String type) {
    this.type = type;
  }
 
  public String getContentType() {
    if (type == null)
      return "application/octet-stream";
    else
      return type;
  }
 
  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(data);
  }
 
  public String getName() {
    return "ByteArrayDataSource";
  }
 
  public OutputStream getOutputStream() throws IOException {
    throw new IOException("Not Supported");
  }
}

class GMailSender extends javax.mail.Authenticator {

  private String user;
  private String password;
  private Session session;
 
  static {
    Security.addProvider(new JSSEProvider());
  }
 
  public GMailSender(String user, String password, String mailhost, String port) {

    this.user = user;
    this.password = password;

    Properties props = new Properties();
    props.setProperty("mail.transport.protocol", "smtp");
    props.setProperty("mail.host", mailhost);
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.port", port);
    props.put("mail.smtp.socketFactory.port", port);
    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    props.put("mail.smtp.socketFactory.fallback", "false");
    props.setProperty("mail.smtp.quitwait", "false");

    session = Session.getDefaultInstance(props, this);
  }

  protected PasswordAuthentication getPasswordAuthentication() {
    return new PasswordAuthentication(user, password);
  }

  public synchronized void sendMail(String subject, String body,
    String sender, String recipients, Boolean attachment, String format) throws Exception {

    String xml = new String("XML");
    MimeMessage msg = new MimeMessage(session);
    Multipart multipart = new MimeMultipart();
    MimeBodyPart part = new MimeBodyPart();

    msg.setSender(new InternetAddress(sender));
    msg.setSubject(subject);

    if (attachment) {
      part.setText(body);
      if (format.equals(xml)) {
        part.setHeader("Content-Type", "text/xml; name=\"form.xml\"");
      } else {
        part.setHeader("Content-Type", "application/json; name=\"form.json\"");
      }
    } else {
      part.setText(body);
    }
    
    multipart.addBodyPart(part);

    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));

    
    msg.setContent(multipart);
    Transport.send(msg);
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