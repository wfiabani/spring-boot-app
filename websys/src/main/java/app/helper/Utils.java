package app.helper;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class Utils {

    @Autowired
    private Environment env;

    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest"
        .equals(request.getHeader("X-Requested-With"));
    }
    public static String getLayout(HttpServletRequest request) {
        return isAjaxRequest(request) ?  "ajax-layout" : "layout";
    }
    
    public void sendMail(String to, String subject, String message, String[] files) throws MessagingException {
        logger.info("MAIL -> Sending new mail...");
        logger.info("To: " + to);
        logger.info("Subject: " + subject);
        logger.info("Message: " + message);
        logger.info("Files: " + files.length);
        Properties mailServerProperties;
        Session getMailSession;
        MimeMessage generateMailMessage;
        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", env.getProperty("nucleus.mail.smtp.port"));
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
        getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        generateMailMessage = new MimeMessage(getMailSession);
        if(to.indexOf(",")>-1){
            String[] address = to.split(",");
            for(int i =0 ; i<address.length ; i++){
                generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(address[i].trim()));
            }
        }else{
            generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        }
        generateMailMessage.setSubject(subject);
        Multipart multipart = new MimeMultipart();
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(message, "text/html");
        for(int i=0 ; i<files.length ; i++){
            
            MimeBodyPart attachPart = new MimeBodyPart();
            String attachFile = files[i];
            try{
                attachPart.attachFile(attachFile);
            }catch(Exception e){
                logger.error("Error attaching file");
                e.printStackTrace();
            }
            multipart.addBodyPart(attachPart);
            
        }
        multipart.addBodyPart(messageBodyPart);
        generateMailMessage.setContent(multipart);
        Transport transport = getMailSession.getTransport("smtp");
        transport.connect(env.getProperty("nucleus.mail.host"), env.getProperty("nucleus.mail.from.address"), env.getProperty("nucleus.mail.from.password"));
        transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
        transport.close();
        logger.info("Finish sending mail...");
    }

    
    public void sendTelegramMessage(String msg){
        HttpClient httpClient = HttpClientBuilder.create().build();
        msg = StringEscapeUtils.escapeJava(msg);
        logger.info("Sending Telegram message...");
        try {
            HttpPost request = new HttpPost("https://api.telegram.org/bot"+env.getProperty("nucleus.telegram.token")+"/sendMessage");
            StringEntity params = new StringEntity("{  \"parse_mode\":\"markdown\", \"chat_id\": \""+env.getProperty("nucleus.telegram.chat_id")+"\", \"text\": \""+msg+"\", \"disable_notification\": false}");
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            logger.info(response.toString());
            logger.info("Telegram message sent successfully");
        } catch (Exception ex) {
            logger.error("Telegram message cannot be sent");
            ex.printStackTrace();
        }
    }
    
//    
//    public void sendTelegramFile(String filePath){
//        HttpClient httpClient = HttpClientBuilder.create().build();
//        logger.info("Sending Telegram file...");
//        try {
//            HttpPost request = new HttpPost("https://api.telegram.org/bot"+env.getProperty("nucleus.telegram.token")+"/sendMessage");
//            StringEntity params = new StringEntity("{  \"parse_mode\":\"markdown\", \"chat_id\": \""+env.getProperty("nucleus.telegram.chat_id")+"\", \"text\": \""+msg+"\", \"disable_notification\": false}");
//            request.addHeader("content-type", "application/json");
//            request.setEntity(params);
//            HttpResponse response = httpClient.execute(request);
//            logger.info(response.toString());
//            logger.info("Telegram message sent successfully");
//        } catch (Exception ex) {
//            logger.error("Telegram message cannot be sent");
//            ex.printStackTrace();
//        }
//    }

    
    public static JSONArray sortJSONArrayByDate(JSONArray array, String fieldName){
        
        List<JSONObject> myJsonArrayAsList = new ArrayList<JSONObject>();
        for (int i = 0; i < array.size(); i++){
            myJsonArrayAsList.add( (JSONObject) array.get(i));
        }
        
        Collections.sort(myJsonArrayAsList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject jsonObjectA, JSONObject jsonObjectB) {
                int compare = 0;
                try{
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    Date date1 = format.parse(jsonObjectA.get(fieldName).toString());
                    Date date2 = format.parse(jsonObjectB.get(fieldName).toString());
                    compare = date1.compareTo(date2);
                }catch(Exception e){
                    e.printStackTrace();
                }
                return compare;
            }
        });
        
        JSONArray results = new JSONArray();
        for (JSONObject obj : myJsonArrayAsList){
            results.add(obj);
		}
		return results;
    }
    
    public static String getURLContent(String url) throws IOException{
        URL urlToRead = new URL(url);
        BufferedReader in = new BufferedReader(
        new InputStreamReader(urlToRead.openStream()));
        StringBuilder sbuilder = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null){
            sbuilder.append(inputLine);
        }
        in.close();
        return sbuilder.toString();
    }

    public static String generateMovementExternalId(String type, Integer id){
        return type+String.format("%05d", id);
    }
    
    public static String generateProductExternalId(Integer id){
        return "P"+String.format("%03d", id);
    }
    
    public static String generateSkuExternalId(Integer id, Integer productId){
        return generateProductExternalId(productId) + "M"+String.format("%03d", id);
    }

    public static String getTmpDirectory(){
        if( System.getProperty("os.name").contains("Windows") ){
            return "C:/tmp/";
        }else{
            return "/srv/";
        }
    }

}
