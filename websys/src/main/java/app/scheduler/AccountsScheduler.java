package app.scheduler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import app.client.CustomerClient;
import app.dto.Customer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import app.helper.Utils;
import app.model.entity.Account;
import app.model.repository.AccountRepository;
import app.model.entity.AccountType;
import app.model.entity.FixedAccount;
import app.model.repository.FixedAccountRepository;
import app.model.entity.Payment;
import app.model.repository.PaymentRepository;

@Component
@EnableScheduling
public class AccountsScheduler {
    
    private final long SECOND = 1000;
    private final long MINUTE = SECOND * 60;
    private final long HOUR = MINUTE * 60;

    private static Logger logger = LoggerFactory.getLogger(AccountsScheduler.class);

    @Autowired
    private CustomerClient usersClient;
    
    @Autowired
	private FixedAccountRepository fixedAccountRepository;
    
    @Autowired
	private AccountRepository accountRepository;
    
    @Autowired
	private PaymentRepository paymentRepository;

    @Autowired
    private Utils utils;
    
    
    @Scheduled(fixedRate = HOUR *12)
    public void accounts5days() {
        sendNotifications(this.getAccounts(5), 5);
    }

    @Scheduled(fixedRate = HOUR *12)
    public void accountsToday() {
        sendNotifications(this.getAccounts(0), 0);
    }
    
    
    private String formatAccount(JSONObject obj){
        String str = "";
        str += "\n\n*Descrição:* "+obj.get("name");
        str += "\n*Código:* "+obj.get("id");
        str += "\n*Tipo:* "+obj.get("freq");
        str += "\n*Total:* R$ "+obj.get("total");
        str += "\n*Vencimento:* "+obj.get("expirationDate");
        str += "\n\n";
        return str;
    }
    
    private void sendNotifications(JSONObject accounts, int interval){

//        String[] fls = {};
//
//        try{
//            utils.sendMail(usersClient.getUser().mail, "Testing mail sending...", "Mensagem...", fls);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
        logger.info("sendNotifications called...");
        try{
            logger.info("Getting user object...");
            Customer user = usersClient.getCustomer();
            logger.info("User selected is [id:"+user.id+" name:"+user.name+" mail:"+user.mail+"]");
            Boolean contasPagar =  ((JSONArray)accounts.get("contasPagar")).size() > 0;
            Boolean contasReceber =  ((JSONArray)accounts.get("contasReceber")).size() > 0;
            if(contasPagar || contasReceber){
                try{
                    String[] files = {};
                    String msg = "";
                    String mailTitle = "";
                    if(interval > 0){
                        msg = "As seguintes contas vencem em *"+(String.valueOf(interval) )+" dia(s)* e estão em aberto:\n\n";
                        //mailTitle = "Algumas contas vencem em *"+(String.valueOf(interval) )+" dia(s)*";
                    }else{
                        msg = "As seguintes contas vencem *hoje* e estão em aberto:\n\n";
                        // = "Contas com vencimento para *HOJE*";
                    }

                    if(contasPagar){
                        msg += "*Contas a pagar*";
                        JSONArray contasPagarArray = (JSONArray) accounts.get("contasPagar");
                        for (int i = 0; i < contasPagarArray.size(); i++){
                            JSONObject objAccount = (JSONObject) contasPagarArray.get(i);
                            msg += formatAccount(objAccount);
                        }
                    }
                    if(contasReceber){
                        msg += "*Contas a receber*";
                        JSONArray contasReceberArray = (JSONArray) accounts.get("contasReceber");
                        for (int i = 0; i < contasReceberArray.size(); i++){
                            JSONObject objAccount = (JSONObject) contasReceberArray.get(i);
                            msg += formatAccount(objAccount);
                        }
                    }
                    msg += "Nucleus Finances";
                    //utils.sendMail(user.mail, mailTitle, msg, files);
                    logger.info("Calling sendTelegramMessage...");
                    utils.sendTelegramMessage(msg);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }catch(Exception e){
            logger.error("Error trying get user");
            e.printStackTrace();
        }
    }
    
    
    
    private JSONObject getAccounts(int interval){
        
        NumberFormat formatter = new DecimalFormat("#0.00");
        SimpleDateFormat dtFormatter = new SimpleDateFormat("dd/MM/yyyy");
        
        Calendar c = Calendar.getInstance(); 
        Date date = new Date();
        c.setTime(date); 
        c.add(Calendar.DAY_OF_MONTH, interval);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        date = c.getTime();
        
        //Date date = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/yyyy");
        String scope = dateFormatter.format(date);
        
        //contas recorrentes
        List<FixedAccount> contasFixas = new ArrayList<FixedAccount>();
        Iterable<FixedAccount> fixedAccounts =  fixedAccountRepository.findByScope(scope);
        for (FixedAccount fixedAccount : fixedAccounts){
            String month = scope.split("/")[0];
            if(
            (month.equals("01") && fixedAccount.getRepeatJAN()) ||
            (month.equals("02") && fixedAccount.getRepeatFEV()) ||
            (month.equals("03") && fixedAccount.getRepeatMAR()) ||
            (month.equals("04") && fixedAccount.getRepeatAPR()) ||
            (month.equals("05") && fixedAccount.getRepeatMAY()) ||
            (month.equals("06") && fixedAccount.getRepeatJUN()) ||
            (month.equals("07") && fixedAccount.getRepeatJUL()) ||
            (month.equals("08") && fixedAccount.getRepeatAGO()) ||
            (month.equals("09") && fixedAccount.getRepeatSET()) ||
            (month.equals("10") && fixedAccount.getRepeatOCT()) ||
            (month.equals("11") && fixedAccount.getRepeatNOV()) ||
            (month.equals("12") && fixedAccount.getRepeatDEC())
            ){
                if(fixedAccount.getExpirationDay()==c.get(Calendar.DAY_OF_MONTH)){
                    
                    Iterable<Payment> payments = paymentRepository.findByScopeAndFixedAccountId(scope, fixedAccount.getId());
                    Payment payment = new Payment();
                    for(Payment pgto : payments){
                        payment = pgto;
                        break;
                    }
                    Boolean status = ((Collection<Payment>)payments).size() > 0;
                    
                    if(!status){
                        contasFixas.add(fixedAccount);
                    }
                }
            }
        }
        
        //contas unicas
        List<Account> contasUnicas = new ArrayList<Account>();
        Iterable<Account> accounts =  accountRepository.findByExpirationDate(date);
        for (Account account : accounts){
            if(!account.getOrcamento()){
                if(!account.getPaid()){
                    contasUnicas.add(account);
                }
            }
        }
        
        JSONArray contasPagar = new JSONArray();
        JSONArray contasReceber = new JSONArray();
        
        for(FixedAccount account : contasFixas){
            JSONObject obj = new JSONObject();
            
            obj.put("id", account.getId());
            obj.put("name", account.getName());
            obj.put("type", String.valueOf(account.getType()));
            obj.put("freq", "Conta recorrente");
            obj.put("total", formatter.format(account.getValue()));
            //obj.put("expirationDate", String.format("%02d", account.getExpirationDay()+"/"+scope));
            obj.put("expirationDate", account.getExpirationDay()+"/"+scope);
            if(account.getType() == AccountType.P){
                contasPagar.add(obj);
            }else{
                contasReceber.add(obj);
            }
        }
        
        for(Account account : contasUnicas){
            JSONObject obj = new JSONObject();
            obj.put("id", account.getId());
            obj.put("name", account.getName());
            obj.put("type", String.valueOf(account.getType()));
            obj.put("freq", "Conta única");
            obj.put("total", formatter.format(account.getValue()));
            obj.put("expirationDate", dtFormatter.format(account.getExpirationDate()));
            if(account.getType() == AccountType.P){
                contasPagar.add(obj);
            }else{
                contasReceber.add(obj);
            }
        }
        
        JSONObject obj = new JSONObject();
        obj.put("contasPagar", Utils.sortJSONArrayByDate( contasPagar, "expirationDate")) ;
        obj.put("contasReceber", Utils.sortJSONArrayByDate( contasReceber, "expirationDate"));
        
        return obj;
    }
    
    
    
    
}