package app.scheduler;

import app.WebsysApplication;
import app.client.CustomerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class AppScheduler {

    @Autowired
    CustomerClient usersClient;

    private final long SECOND = 1000;
    private final long MINUTE = SECOND * 60;
    private final long HOUR = MINUTE * 60;

    private static Logger logger = LoggerFactory.getLogger(AppScheduler.class);

    @Scheduled(fixedRate = HOUR*12)
    public void verify(){
        logger.info("Started user validation scheduler");
        if( usersClient.isValid() ){
            logger.info("User is valid");
        }else{
            logger.error("User is invalid");
            WebsysApplication.destroy();
        }
        logger.info("Completed User Validation");
    }

}
