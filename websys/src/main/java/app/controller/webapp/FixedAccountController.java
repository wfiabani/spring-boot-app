package app.controller.webapp;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import app.helper.Utils;
import app.model.entity.AccountType;
import app.model.entity.FixedAccount;
import app.model.repository.FixedAccountRepository;
import app.model.entity.Payment;
import app.model.repository.PaymentRepository;

@Controller
@RequestMapping(path="/fixed-accounts")
public class FixedAccountController {
	@Autowired
	private FixedAccountRepository fixedAccountRepository;
    
    @Autowired
	private PaymentRepository paymentRepository;
    
    
	@RequestMapping("/list")
    public String list(HttpServletRequest request, Model model) {
        model.addAttribute("page", "fixed-accounts-list");
        return Utils.getLayout(request);
    }
    
    
    
    @ResponseBody
	@RequestMapping(value="/list-all", method=RequestMethod.POST)
    public String listAll(HttpServletRequest request, Model model) {
        Iterable<FixedAccount> accounts =  fixedAccountRepository.findAll();
		JSONArray jap = new JSONArray();
        for (FixedAccount account : accounts){
			jap.add( toJSON(account) );
		}
		return jap.toString();
	}
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/add", method=RequestMethod.POST)
    public String add(
    HttpServletRequest request, 
    @RequestParam(value="id", required=false) int id, 
    @RequestParam(value="name", required=true) String name, 
    @RequestParam(value="externalId", required=true) String externalId, 
    @RequestParam(value="value", required=true) Double value, 
    @RequestParam(value="expirationDay", required=true) Integer expirationDay, 
    @RequestParam(value="enabledDate", required=true) String enabledDate, 
    @RequestParam(value="disabledDate", required=false) String disabledDate, 
    @RequestParam(value="type", required=true) String type, 
    @RequestParam(value="repeatJAN", required=true) Boolean repeatJAN, 
    @RequestParam(value="repeatFEV", required=true) Boolean repeatFEV, 
    @RequestParam(value="repeatMAR", required=true) Boolean repeatMAR, 
    @RequestParam(value="repeatAPR", required=true) Boolean repeatAPR, 
    @RequestParam(value="repeatMAY", required=true) Boolean repeatMAY, 
    @RequestParam(value="repeatJUN", required=true) Boolean repeatJUN, 
    @RequestParam(value="repeatJUL", required=true) Boolean repeatJUL, 
    @RequestParam(value="repeatAGO", required=true) Boolean repeatAGO, 
    @RequestParam(value="repeatSET", required=true) Boolean repeatSET, 
    @RequestParam(value="repeatOCT", required=true) Boolean repeatOCT, 
    @RequestParam(value="repeatNOV", required=true) Boolean repeatNOV, 
    @RequestParam(value="repeatDEC", required=true) Boolean repeatDEC, 
    Model model){
        
        
        
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        
        Date _enabledDate;
        try{
            _enabledDate = formatter.parse(enabledDate);
        }catch(Exception e){
            _enabledDate = new Date();
        }
        
        Date _disabledDate;
        try{
            _disabledDate = formatter.parse(disabledDate);
        }catch(Exception e){
            _disabledDate = null;
        }
        
        FixedAccount account = new FixedAccount();
        if(id > 0){
            account = fixedAccountRepository.getOne(id);
        }
		account.setName(name);
		account.setExternalId(externalId);
		account.setValue(value);
		account.setType(AccountType.valueOf(type));
		account.setExpirationDay(expirationDay);
        
        account.setEnabledDate(_enabledDate);
        account.setDisabledDate(_disabledDate);
        
        account.setRepeatJAN(repeatJAN);
        account.setRepeatFEV(repeatFEV);
        account.setRepeatMAR(repeatMAR);
        account.setRepeatAPR(repeatAPR);
        account.setRepeatMAY(repeatMAY);
        account.setRepeatJUN(repeatJUN);
        account.setRepeatJUL(repeatJUL);
        account.setRepeatAGO(repeatAGO);
        account.setRepeatSET(repeatSET);
        account.setRepeatOCT(repeatOCT);
        account.setRepeatNOV(repeatNOV);
        account.setRepeatDEC(repeatDEC);
        
        fixedAccountRepository.save(account);
        return toJSON(account).toString();
    }



    @ResponseBody
    @RequestMapping(value="/duplicate", method=RequestMethod.POST)
    public void duplicate(@RequestParam(value="id", required=true) Integer id){
        
        FixedAccount account = fixedAccountRepository.getOne(id);
        FixedAccount nAccount = new FixedAccount();

        nAccount.setName(account.getName());
		nAccount.setExternalId(account.getExternalId());
		nAccount.setValue(account.getValue());
		nAccount.setType(account.getType());
		nAccount.setExpirationDay(account.getExpirationDay());
        
        nAccount.setEnabledDate(account.getEnabledDate());
        nAccount.setDisabledDate(account.getDisabledDate());
        
        nAccount.setRepeatJAN(account.getRepeatJAN());
        nAccount.setRepeatFEV(account.getRepeatFEV());
        nAccount.setRepeatMAR(account.getRepeatMAR());
        nAccount.setRepeatAPR(account.getRepeatAPR());
        nAccount.setRepeatMAY(account.getRepeatMAY());
        nAccount.setRepeatJUN(account.getRepeatJUN());
        nAccount.setRepeatJUL(account.getRepeatJUL());
        nAccount.setRepeatAGO(account.getRepeatAGO());
        nAccount.setRepeatSET(account.getRepeatSET());
        nAccount.setRepeatOCT(account.getRepeatOCT());
        nAccount.setRepeatNOV(account.getRepeatNOV());
        nAccount.setRepeatDEC(account.getRepeatDEC());
        
        fixedAccountRepository.save(nAccount);
    }
    
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/add-payment", method=RequestMethod.POST)
    public String addPayment(
    @RequestParam(value="id", required=true) Integer id,
    @RequestParam(value="value", required=true) Double value,
    @RequestParam(value="fixedAccountId", required=true) Integer fixedAccountId,
    @RequestParam(value="scope", required=true) String scope
    ){
        Payment payment =new Payment();
        if(id > 0){
            payment = paymentRepository.getOne(id);
        }
        payment.setValue(value);
        payment.setFixedAcccountId(fixedAccountId);;
        payment.setScope(scope);
        
        //mais adiante melhorar isso
        payment.setCreationDate(new Date());
        payment.setPaymentDate(new Date());
        
        paymentRepository.save(payment);
        return paymentToJSON(payment).toString();
        
    }
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/verify-payment", method=RequestMethod.POST)
    public String addPayment(
    @RequestParam(value="fixedAccountId", required=true) Integer fixedAccountId,
    @RequestParam(value="scope", required=true) String scope
    ){
        Collection<Payment> _payments =  paymentRepository.findByScopeAndFixedAccountId(scope, fixedAccountId);
        Iterable<Payment> payments =  paymentRepository.findByScopeAndFixedAccountId(scope, fixedAccountId);
        
        JSONObject obj = new JSONObject();
        if(_payments.size()==0){
            obj.put("paid", "false");
        }else if(_payments.size()==1){
            Payment payment = payments.iterator().next();
            FixedAccount account = fixedAccountRepository.getOne(payment.getFixedAcccountId());
            obj = paymentToJSON(payment);obj.put("paid", "true");
        }else if(_payments.size()>1){
            obj.put("paid", "error");
            obj.put("size", _payments.size());
        }
        return obj.toString();
        
    }
    
    
    
    @ResponseBody
    @RequestMapping(value="/remove-payment", method=RequestMethod.POST)
    public void removePayment(@RequestParam(value="id", required=true) Integer id){
		Payment payment = paymentRepository.getOne(id);
        paymentRepository.delete(payment);
    }
    
    
    
    
    
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/get", method=RequestMethod.POST)
    public String get(@RequestParam(value="id", required=true) Integer id){
		FixedAccount account = fixedAccountRepository.getOne(id);
        return toJSON(account).toString();
    }
    
    
    @ResponseBody
    @RequestMapping(value="/remove", method=RequestMethod.POST)
    public void remove(@RequestParam(value="id", required=true) Integer id){
		FixedAccount account = fixedAccountRepository.getOne(id);
        fixedAccountRepository.delete(account);
    }
    
    
    
    
    private JSONObject toJSON(FixedAccount account){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        JSONObject obj = new JSONObject();
        obj.put("id", account.getId());
        obj.put("name", account.getName());
        obj.put("externalId", account.getExternalId());
        obj.put("expirationDay", account.getExpirationDay());
        obj.put("value", account.getValue());
        obj.put("type", account.getType().toString());
        obj.put("creationDate", formatter.format(account.getCreationDate()));
        obj.put("lastModifiedDate", formatter.format(account.getLastModifiedDate()));
        obj.put("enabledDate", formatter.format(account.getEnabledDate()));
        obj.put("repeatJAN", account.getRepeatJAN());
        obj.put("repeatFEV", account.getRepeatFEV());
        obj.put("repeatMAR", account.getRepeatMAR());
        obj.put("repeatAPR", account.getRepeatAPR());
        obj.put("repeatMAY", account.getRepeatMAY());
        obj.put("repeatJUN", account.getRepeatJUN());
        obj.put("repeatJUL", account.getRepeatJUL());
        obj.put("repeatAGO", account.getRepeatAGO());
        obj.put("repeatSET", account.getRepeatSET());
        obj.put("repeatOCT", account.getRepeatOCT());
        obj.put("repeatNOV", account.getRepeatNOV());
        obj.put("repeatDEC", account.getRepeatDEC());
        
        if( account.getDisabledDate()==null ){
            obj.put("disabledDate", "");
        }else{
            obj.put("disabledDate", formatter.format(account.getDisabledDate()));
        }
        
        return obj;
    }
    
    
    private JSONObject paymentToJSON(Payment payment){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        JSONObject obj = new JSONObject();
        obj.put("id", payment.getId());
        obj.put("value", payment.getValue());
        obj.put("scope", payment.getScope());
        obj.put("fixedAccountId", payment.getFixedAcccountId());
        obj.put("creationDate", formatter.format(payment.getCreationDate()));
        obj.put("paymentDate", formatter.format(payment.getPaymentDate()));
        return obj;
    }
    
    
    
}