package app.client;


import app.dto.Customer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CustomerClient {

    private static Logger logger = LoggerFactory.getLogger(CustomerClient.class);

    @Autowired
    private Environment env;

    public Customer getCustomer() throws Exception{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(env.getProperty("nucleus.users.endpoint"));
        CloseableHttpResponse response = httpClient.execute(request);
        ObjectMapper mapper = new ObjectMapper();
        Customer user = mapper.readValue(response.getEntity().getContent(), Customer.class);
        logger.info("User", user);
        return user;
    }

    public Boolean isValid(){
        logger.info("getUser called");
        try {
            Customer customer = this.getCustomer();
            logger.info("User", customer.name);
            logger.info("is enabled: "+ customer.enabled.toString());
            logger.info("end getUser");
            return customer.enabled;
        }catch(Exception e){
            e.printStackTrace();
            logger.info("ex", e);
            return false;
        }
    }


}
