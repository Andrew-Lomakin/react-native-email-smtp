package com.reactlibrarymc;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableArray;

import java.util.Properties;
import android.os.AsyncTask;

import javax.mail.*;
import javax.mail.internet.*;
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

        @Override
        public void run() {
          try {
            GMailSender sender = new GMailSender();
            sender.sendMail(username, password, mailhost, port, ssl, 
              subject, body, from, recipients, attachment, format, img);
            WritableMap result = Arguments.createMap();
            result.putString("status", "SUCCESS");
            promise.resolve(result);
          } catch (Exception e) {
            promise.reject("status", e.getLocalizedMessage());
          } finally {
          }
        }
    });
  }
}

class GMailSender {
 
  public GMailSender() {
  }

  public synchronized void sendMail(
    final String username, final String password, String mailhost, String port, Boolean ssl,
    String subject, String body,
    String sender, String recipients, Boolean attachment, String format, 
    ReadableArray img ) throws Exception {
      String xml = new String("XML");
      Properties props = new Properties();
      props.put("mail.transport.protocol", "smtp");
      props.put("mail.smtp.host", mailhost);
      props.put("mail.smtp.port", port);
      props.put("mail.smtp.ssl.enable", "true");
      props.put("mail.smtp.auth", "true");
      Session session = Session.getInstance(props);
      MimeMessage msg = new MimeMessage(session);
      Multipart multipart = new MimeMultipart("mixed");
      Transport transport = session.getTransport();

      msg.setSender(new InternetAddress(sender));
      msg.setSubject(subject);

      MimeBodyPart bodyPart = new MimeBodyPart();
      if (attachment) {
        bodyPart.setText(body);
        if (format.equals(xml)) {
          bodyPart.setHeader("Content-Type", "text/xml; name=\"form.xml\"");
        } else {
          bodyPart.setHeader("Content-Type", "application/json; name=\"form.json\"");
        }
      } 
      multipart.addBodyPart(bodyPart);

      Integer size = img.size();
      for (int i = 0; i < size; i++) {
        MimeBodyPart attachmentPart = new MimeBodyPart();
        FileDataSource fileDataSource = new FileDataSource(img.getString(i));
        attachmentPart.setDataHandler(new DataHandler(fileDataSource));
        attachmentPart.setFileName(fileDataSource.getName());  
        attachmentPart.setHeader("Content-Type", "application/octet-stream"); 
        multipart.addBodyPart(attachmentPart);
      }

      msg.setContent(multipart);

      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));

      transport.connect(mailhost, username, password);
      transport.sendMessage(msg, msg.getAllRecipients());
      transport.close();
    }
}