package app.scheduler;

import app.client.CustomerClient;
import app.dto.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import app.helper.Utils;

@Component
@EnableScheduling
public class BackupScheduler {

  private Customer user;

  @Autowired
  private CustomerClient usersClient;

  @Autowired
  private Utils utils;
  
  private final long SECOND = 1000;
  private final long MINUTE = SECOND * 60;
  private final long HOUR = MINUTE * 60;

  private static Logger logger = LoggerFactory.getLogger(BackupScheduler.class);

  @Scheduled(cron = "0 00 02 * * ?")
  public void backup() {
    logger.info("Backup Scheduler start");
    try{
      logger.info("Getting user data...");
      user = usersClient.getCustomer();
      try{
        logger.info("Mail sending starting...");
        String[] files = {Utils.getTmpDirectory()+"database.sql"};
        String msg = "Em anexo." + "<br><br> Nucleus Finances";
        utils.sendMail(user.mail, "Backup Nucleus Finances", msg, files);
      }catch(Exception e){
        logger.error("An error ocurred while sending mail with database backup file");
        e.printStackTrace();
      }
    }catch(Exception e){
      logger.error("An error occurred while retrieving user data");
      e.printStackTrace();
    }
  }
  
}