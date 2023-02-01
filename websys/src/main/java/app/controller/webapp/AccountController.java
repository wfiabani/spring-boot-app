package app.controller.webapp;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import app.helper.Utils;
import app.model.entity.Account;
import app.model.repository.AccountRepository;
import app.model.entity.AccountType;

@Controller
@RequestMapping(path="/accounts")
public class AccountController {


	@Autowired
	private AccountRepository accountRepository;


	@RequestMapping("/list")
    public String list(HttpServletRequest request, Model model) {
        model.addAttribute("page", "accounts-list");
        return Utils.getLayout(request);
    }



    @ResponseBody
	@RequestMapping(value="/list-all", method=RequestMethod.POST)
    public String listAll(HttpServletRequest request, Model model) {
        Iterable<Account> accounts =  accountRepository.findAll();
		JSONArray jap = new JSONArray();
        for (Account account : accounts){
            if(!account.getOrcamento()){
                jap.add( toJSON(account) );
            }
		}
		return jap.toString();// Utils.sortJSONArrayByDate( jap, "expirationDate").toString();
    }
    



    @ResponseBody
	@RequestMapping(value="/get-accounts/{movementId}", method=RequestMethod.POST)
    public String getAccounts(
    HttpServletRequest request, 
    @PathVariable(value="movementId", required=true) Integer movementId,
    Model model) {
        Iterable<Account> items =  accountRepository.findByMovementId(movementId);
        JSONArray jap = new JSONArray();
        for (Account item : items){
			jap.add( toJSON(item) );
		}
		return jap.toString();
	}





    @ResponseBody
    @RequestMapping(value="/add", method=RequestMethod.POST)
    public String add(
            HttpServletRequest request, 
            @RequestParam(value="id", required=false) int id, 
            @RequestParam(value="movementId", required=true) int movementId, 
            @RequestParam(value="orcamento", required=true) Boolean orcamento, 
            @RequestParam(value="name", required=true) String name, 
            @RequestParam(value="externalId", required=true) String externalId, 
            @RequestParam(value="value", required=true) Double value, 
            @RequestParam(value="expirationDate", required=true) String expirationDate, 
            @RequestParam(value="type", required=true) String type, 
            @RequestParam(value="paid", required=true) Boolean paid,
            Model model){

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date _expirationDate;
        try{
            _expirationDate = formatter.parse(expirationDate);
        }catch(Exception e){
            _expirationDate = new Date();
        }

        Account account = new Account();
        if(id > 0){
            account = accountRepository.getOne(id);
        }

        account.setOrcamento(orcamento);
        account.setMovementId(movementId);
		account.setName(name);
		account.setExternalId(externalId);
		account.setValue(value);
		account.setExpirationDate(_expirationDate);
		account.setType(AccountType.valueOf(type));
		account.setPaid(paid);
        
        accountRepository.save(account);
        return toJSON(account).toString();
    }


    @ResponseBody
    @RequestMapping(value="/get", method=RequestMethod.POST)
    public String get(@RequestParam(value="id", required=true) Integer id){
		Account account = accountRepository.getOne(id);
        return toJSON(account).toString();
    }


    @ResponseBody
    @RequestMapping(value="/remove", method=RequestMethod.POST)
    public void remove(@RequestParam(value="id", required=true) Integer id){
		Account account = accountRepository.getOne(id);
        accountRepository.delete(account);
    }
    
    
    @ResponseBody
    @RequestMapping(value="/duplicate", method=RequestMethod.POST)
    public void duplicate(@RequestParam(value="id", required=true) Integer id){
        
        Account account = accountRepository.getOne(id);
        Account nAccount = new Account();

        nAccount.setOrcamento(account.getOrcamento());
        nAccount.setMovementId(account.getMovementId());
		nAccount.setName(account.getName());
		nAccount.setExternalId(account.getExternalId());
		nAccount.setValue(account.getValue());
		nAccount.setExpirationDate(account.getExpirationDate());
		nAccount.setType(account.getType());
		nAccount.setPaid(account.getPaid());
        
        accountRepository.save(nAccount);
    }




    private JSONObject toJSON(Account account){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        JSONObject obj = new JSONObject();
        obj.put("id", account.getId());
        obj.put("movementId", account.getMovementId());
        obj.put("orcamento", account.getOrcamento());
        obj.put("name", account.getName());
        obj.put("externalId", account.getExternalId());
        obj.put("value", account.getValue());
        obj.put("type", account.getType().toString());
        obj.put("paid", account.getPaid());
        obj.put("expirationDate", formatter.format(account.getExpirationDate()));
        obj.put("creationDate", formatter.format(account.getCreationDate()));
        obj.put("lastModifiedDate", formatter.format(account.getLastModifiedDate()));
        return obj;
    }

}