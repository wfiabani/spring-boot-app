package app.controller.webapp;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
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
import app.model.entity.Cadastro;
import app.model.repository.CadastroRepository;
import app.model.entity.FixedAccount;
import app.model.repository.FixedAccountRepository;
import app.model.repository.MovementRepository;
import app.model.entity.Payment;
import app.model.repository.PaymentRepository;
import app.model.repository.ProductCategory;
import app.model.repository.ProductCategoryRepository;
import app.model.repository.ProductRepository;
import app.model.entity.SKU;
import app.model.repository.SKURepository;





@Controller
@RequestMapping(path="/service")
public class ServiceController {
    
    
    @Autowired
	private FixedAccountRepository fixedAccountRepository;
    
    @Autowired
	private AccountRepository accountRepository;
    
    @Autowired
	private MovementRepository movementRepository;
    
    @Autowired
	private PaymentRepository paymentRepository;
    
	@Autowired
	private ProductCategoryRepository productCategoryRepository;
    
	@Autowired
    private CadastroRepository cadastroRepository;
    
    @Autowired
	private ProductRepository productRepository;
    
    @Autowired
	private SKURepository skuRepository;
    
    
    
    private int numLines = 0;
    private PDDocument doc;
    private PDPage page;
    private PDPageContentStream cont;
    
    
    
    
    @ResponseBody
	@RequestMapping(value="/contas-a-vencer/{startDate}/{daysInterval}", method=RequestMethod.GET)
    public String contasAVencer(
    HttpServletRequest request,
    @PathVariable(value="startDate", required=true) String startDate,
    @PathVariable(value="daysInterval", required=true) Integer daysInterval,
    Model model) {
        
        //
        
        try {
            DateFormat formater = new SimpleDateFormat("dd/MM/yyyy");
            
            Calendar beginCalendar = Calendar.getInstance();
            Calendar finishCalendar = Calendar.getInstance();
            
            Date dataIni = formater.parse(startDate.replaceAll("-", "/"));
            beginCalendar.setTime(dataIni);
            finishCalendar.setTime(dataIni);
            finishCalendar.add(Calendar.DAY_OF_MONTH, daysInterval);
            
            List<String> days = new ArrayList<String>();
            while (!beginCalendar.after(finishCalendar)) {
                String date = formater.format(beginCalendar.getTime());
                days.add(date);
                beginCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            for(String date : days){
                
                String day = date.split("/")[0];
                String month = date.split("/")[1];
                String year = date.split("/")[2];
                String scope = month+"/"+year;
                Iterable<FixedAccount> fixedAccounts =  fixedAccountRepository.findByScope(scope);
                for (FixedAccount fixedAccount : fixedAccounts){
                    if( Integer.valueOf(day) == fixedAccount.getExpirationDay() ){
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
                            
                            Iterable<Payment> payments = paymentRepository.findByScopeAndFixedAccountId(scope, fixedAccount.getId());
                            Boolean status = ((Collection<Payment>)payments).size() > 0;
                            if(!status){
                                //if(fixedAccount.getType() == AccountType.P){
                                    String format = "%1$-40s %2$-14s %3$-14s %4$14s";
                                    System.out.println(String.format(format,  fixedAccount.getName(), fixedAccount.getValue(), fixedAccount.getType(), date));
                                    //}
                                }
                            }
                        }
                    }
                }
            }catch(Exception e){
                
            }
            
            return "***";
            
        }
        
        
        
        
        @ResponseBody
        @RequestMapping(value="/get-finances-report", method=RequestMethod.POST)
        public String getFinancesReport(
        HttpServletRequest request,
        @RequestParam(value="scope", required=true) String scope,
        Model model) {
            
            
            String content = "";
            
            String fileName = "relatorio-financeiro-"+scope.replace("/", "-");
            
            
            try{
                numLines = 0;
                doc = new PDDocument();
                page = new PDPage();
                doc.addPage(page);
                cont = new PDPageContentStream(doc, page);
                cont.beginText();
                cont.setFont(PDType1Font.COURIER, 11);
                cont.setLeading(14.5f);
                cont.newLineAtOffset(35, 760);
                
                this.lineBreakVerify("RELATÓRIO FINANCEIRO");
                this.lineBreakVerify("");
                this.lineBreakVerify("MÊS: "+scope);
                
                
                
                String format = "%1$-40s %2$-14s %3$10s %4$14s";
                
                
                this.lineBreakVerify("");
                content = ("CONTAS A PAGAR");
                this.lineBreakVerify(content);
                
                content = String.format(format, "DESCR", "PAGO", "VALOR R$", "VENCIMENTO");
                this.lineBreakVerify(content);
                
                content = ("---------------------------------------------------------------------------------");
                this.lineBreakVerify(content);
                
                JSONObject accounts = getAccounts(scope);
                JSONArray accounts_ = (JSONArray) accounts.get("contasPagar");
                for (int i = 0; i < accounts_.size(); i++){
                    JSONObject account = (JSONObject) accounts_.get(i);
                    String nameStr = (String) (account.get("name"));
                    try{
                        nameStr = nameStr.substring(0, 34);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    content = String.format(format, nameStr, (Boolean) account.get("paid") ? "PAGO" : "EM ABERTO", account.get("total"), account.get("expirationDate"));
                    this.lineBreakVerify(content);
                }
                
                this.lineBreakVerify("");
                content = ("CONTAS A RECEBER");
                this.lineBreakVerify(content);
                
                content = String.format(format, "DESCR", "PAGO", "VALOR R$", "VENCIMENTO");
                this.lineBreakVerify(content);
                
                content = ("---------------------------------------------------------------------------------");
                this.lineBreakVerify(content);
                
                accounts_ = (JSONArray) accounts.get("contasReceber");
                for (int i = 0; i < accounts_.size(); i++){
                    JSONObject account = (JSONObject) accounts_.get(i);
                    String nameStr = (String) (account.get("name"));
                    try{
                        nameStr = nameStr.substring(0, 34);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    content = String.format(format, nameStr, (Boolean) account.get("paid") ? "PAGO" : "EM ABERTO", account.get("total"), account.get("expirationDate"));
                    this.lineBreakVerify(content);
                }
                
                
                
                String rformat = "%1$-40s %2$40s";
                
                this.lineBreakVerify("");
                this.lineBreakVerify(String.format(rformat,"RESUMO "+scope, "R$"));
                this.lineBreakVerify("---------------------------------------------------------------------------------");
                this.lineBreakVerify(String.format(rformat,"TOTAL A PAGAR:", ((JSONObject) accounts.get("resumo")).get("totalPagar")));
                this.lineBreakVerify(String.format(rformat,"TOTAL PAGO:",  ((JSONObject) accounts.get("resumo")).get("totalPago")));
                this.lineBreakVerify(String.format(rformat,"TOTAL A RECEBER:",  ((JSONObject) accounts.get("resumo")).get("totalReceber")));
                this.lineBreakVerify(String.format(rformat,"TOTAL RECEBIDO:",  ((JSONObject) accounts.get("resumo")).get("totalRecebido")));
                this.lineBreakVerify(String.format(rformat,"LUCO ESTIMADO:",  ((JSONObject) accounts.get("resumo")).get("lucroEstimado")));
                this.lineBreakVerify(String.format(rformat,"LUCRO EFETIVO:",  ((JSONObject) accounts.get("resumo")).get("lucroEfetivo")));
                this.lineBreakVerify("");
                
                cont.endText();
                cont.close();
                doc.save("/tmp/"+fileName+".pdf");
                doc.close();
                
                
            }catch(Exception e){
                e.printStackTrace();
            }
            
            JSONObject obj = new JSONObject();
            obj.put("fileName", fileName);
            return obj.toString();
            
            
        }
        
        
        
        @ResponseBody
        @RequestMapping(value="/get-skus-report", method=RequestMethod.POST)
        public String getSKUSReport(
        HttpServletRequest request,
        @RequestParam(value="obj", required=true) String obj,
        Model model) {
            
            String content = "";
            
            String fileName = "relatorio-estoque";
            
            
            
            
            try{
                JSONParser parser = new JSONParser();
                JSONArray items = (JSONArray) parser.parse(obj);
                numLines = 0;
                doc = new PDDocument();
                page = new PDPage();
                doc.addPage(page);
                cont = new PDPageContentStream(doc, page);
                cont.beginText();
                cont.setFont(PDType1Font.COURIER, 11);
                cont.setLeading(14.5f);
                cont.newLineAtOffset(35, 760);
                
                this.lineBreakVerify("RELATÓRIO DE ESTOQUE");
                this.lineBreakVerify("");
                
                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
                this.lineBreakVerify(formatter.format(date));
                this.lineBreakVerify("");
                
                String format = "%1$-12s %2$-34s %3$10s %4$10s %5$11s";
                
                this.lineBreakVerify(content);
                
                content = String.format(format, "Código", "Produto", "Qtd", "Qtd Min", "");
                this.lineBreakVerify(content);
                content = ("---------------------------------------------------------------------------------");
                this.lineBreakVerify(content);
                for (int i = 0; i < items.size(); i++){
                    JSONObject item = ( (JSONObject) items.get(i));
                    
                    
                    Integer skuId  = Integer.valueOf( ((Long) item.get("id")).toString() );
                    SKU sku = skuRepository.getOne(skuId);
                    

                    String nameStr = sku.getProduct().getName() + " - " + sku.getName();
                    try{
                        nameStr = nameStr.substring(0, 10)+"..."+nameStr.substring(nameStr.length()-20);
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    content = String.format(format, sku.getExternalId(), nameStr, sku.getEstoque(), sku.getEstoqueMin(), sku.getEstoque() <= sku.getEstoqueMin() ? "comprar" : "");
                    this.lineBreakVerify(content);
                }
                
                
                /*
                
                try{
                    numLines = 0;
                    doc = new PDDocument();
                    page = new PDPage();
                    doc.addPage(page);
                    cont = new PDPageContentStream(doc, page);
                    cont.beginText();
                    cont.setFont(PDType1Font.COURIER, 11);
                    cont.setLeading(14.5f);
                    cont.newLineAtOffset(35, 760);
                    
                    this.lineBreakVerify("RELATÓRIO FINANCEIRO");
                    this.lineBreakVerify("");
                    this.lineBreakVerify("MÊS: "+scope);
                    
                    
                    
                    String format = "%1$-40s %2$-14s %3$10s %4$14s";
                    
                    
                    this.lineBreakVerify("");
                    content = ("CONTAS A PAGAR");
                    this.lineBreakVerify(content);
                    
                    content = String.format(format, "DESCR", "PAGO", "VALOR R$", "VENCIMENTO");
                    this.lineBreakVerify(content);
                    
                    content = ("---------------------------------------------------------------------------------");
                    this.lineBreakVerify(content);
                    
                    JSONObject accounts = getAccounts(scope);
                    JSONArray accounts_ = (JSONArray) accounts.get("contasPagar");
                    for (int i = 0; i < accounts_.size(); i++){
                        JSONObject account = (JSONObject) accounts_.get(i);
                        content = String.format(format, account.get("name"), (Boolean) account.get("paid") ? "PAGO" : "EM ABERTO", account.get("total"), account.get("expirationDate"));
                        this.lineBreakVerify(content);
                    }
                    
                    this.lineBreakVerify("");
                    content = ("CONTAS A RECEBER");
                    this.lineBreakVerify(content);
                    
                    content = String.format(format, "DESCR", "PAGO", "VALOR R$", "VENCIMENTO");
                    this.lineBreakVerify(content);
                    
                    content = ("---------------------------------------------------------------------------------");
                    this.lineBreakVerify(content);
                    
                    accounts_ = (JSONArray) accounts.get("contasReceber");
                    for (int i = 0; i < accounts_.size(); i++){
                        JSONObject account = (JSONObject) accounts_.get(i);
                        content = String.format(format, account.get("name"), (Boolean) account.get("paid") ? "PAGO" : "EM ABERTO", account.get("total"), account.get("expirationDate"));
                        this.lineBreakVerify(content);
                    }
                    
                    
                    
                    String rformat = "%1$-40s %2$40s";
                    
                    this.lineBreakVerify("");
                    this.lineBreakVerify(String.format(rformat,"RESUMO "+scope, "R$"));
                    this.lineBreakVerify("---------------------------------------------------------------------------------");
                    this.lineBreakVerify(String.format(rformat,"TOTAL A PAGAR:", ((JSONObject) accounts.get("resumo")).get("totalPagar")));
                    this.lineBreakVerify(String.format(rformat,"TOTAL PAGO:",  ((JSONObject) accounts.get("resumo")).get("totalPago")));
                    this.lineBreakVerify(String.format(rformat,"TOTAL A RECEBER:",  ((JSONObject) accounts.get("resumo")).get("totalReceber")));
                    this.lineBreakVerify(String.format(rformat,"TOTAL RECEBIDO:",  ((JSONObject) accounts.get("resumo")).get("totalRecebido")));
                    this.lineBreakVerify(String.format(rformat,"LUCO ESTIMADO:",  ((JSONObject) accounts.get("resumo")).get("lucroEstimado")));
                    this.lineBreakVerify(String.format(rformat,"LUCRO EFETIVO:",  ((JSONObject) accounts.get("resumo")).get("lucroEfetivo")));
                    this.lineBreakVerify("");
                    */
                    cont.endText();
                    cont.close();
                    doc.save("/tmp/"+fileName+".pdf");
                    doc.close();
                    
                    
                }catch(Exception e){
                    e.printStackTrace();
                }
                
                JSONObject _obj = new JSONObject();
                _obj.put("fileName", fileName);
                return _obj.toString();
                
                
            }
            
            
            
            @RequestMapping(value = "/download-report/{fileName}", method = RequestMethod.GET )
            @ResponseBody
            public FileSystemResource downloadReport(
            @PathVariable("fileName") String fileName, 
            HttpServletResponse response) {
                
                fileName += ".pdf";
                response.setContentType("application/pdf"); 
                response.setHeader("Content-Disposition", "attachment; filename="+fileName);
                FileSystemResource stream = new FileSystemResource(new File("/tmp/"+fileName)); 
                return stream;
                
            }
            
            
            
            
            
            //retorna as contas de um escopo ordenadas por data
            private JSONObject getAccounts(String date){
                
                Double totalPagar = 0.0;
                Double totalReceber = 0.0;
                Double totalPago = 0.0;
                Double totalRecebido = 0.0;
                
                
                NumberFormat formatter = new DecimalFormat("#0.00");
                
                SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/yyyy");
                String scope = date.length()==0 ? dateFormatter.format(new Date()) : date;
                
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
                        contasFixas.add(fixedAccount);
                    }
                }
                
                
                
                //contas unicas
                List<Account> contasUnicas = new ArrayList<Account>();
                Iterable<Account> accounts =  accountRepository.findByScope(scope);
                for (Account account : accounts){
                    if(!account.getOrcamento()){
                        contasUnicas.add(account);
                    }
                }
                
                
                
                JSONArray contasPagar = new JSONArray();
                JSONArray contasReceber = new JSONArray();
                
                
                
                for(FixedAccount account : contasFixas){
                    JSONObject obj = new JSONObject();
                    
                    Iterable<Payment> payments = paymentRepository.findByScopeAndFixedAccountId(scope, account.getId());
                    Payment payment = new Payment();
                    for(Payment pgto : payments){
                        payment = pgto;
                        break;
                    }
                    Boolean status = ((Collection<Payment>)payments).size() > 0;
                    
                    obj.put("id", account.getId());
                    obj.put("name", account.getName());
                    obj.put("type", String.valueOf(account.getType()));
                    obj.put("fixed", true);
                    obj.put("paid", status);
                    obj.put("total", formatter.format(account.getValue()));
                    obj.put("totalPago", status ? formatter.format(payment.getValue()) : formatter.format(0.0) );
                    obj.put("expirationDate", String.format("%02d", account.getExpirationDay())+"/"+scope);
                    if(account.getType() == AccountType.P){
                        contasPagar.add(obj);
                        totalPagar += account.getValue();
                        totalPago += status ? payment.getValue() : 0;
                    }else{
                        contasReceber.add(obj);
                        totalReceber += account.getValue();
                        totalRecebido += status ? payment.getValue() : 0;
                    }
                }
                
                
                SimpleDateFormat dtFormatter = new SimpleDateFormat("dd/MM/yyyy");
                
                
                for(Account account : contasUnicas){
                    JSONObject obj = new JSONObject();
                    obj.put("id", account.getId());
                    obj.put("name", account.getName());
                    obj.put("type", String.valueOf(account.getType()));
                    obj.put("fixed", false);
                    obj.put("paid", account.getPaid());
                    obj.put("total", formatter.format(account.getValue()));
                    obj.put("totalPago", account.getPaid() ? formatter.format(account.getValue()) : formatter.format(0.0));
                    obj.put("expirationDate", dtFormatter.format(account.getExpirationDate()));
                    if(account.getType() == AccountType.P){
                        contasPagar.add(obj);
                        totalPagar += account.getValue();
                        totalPago += account.getPaid() ? account.getValue() : 0;
                    }else{
                        contasReceber.add(obj);
                        totalReceber += account.getValue();
                        totalRecebido += account.getPaid() ? account.getValue() : 0;
                    }
                }
                
                
                JSONObject resumo = new JSONObject();
                resumo.put("totalPagar", formatter.format(totalPagar));
                resumo.put("totalReceber", formatter.format(totalReceber));
                resumo.put("totalRecebido", formatter.format(totalRecebido));
                resumo.put("totalPago", formatter.format(totalPago));
                resumo.put("lucroEstimado", formatter.format(totalReceber-totalPagar));
                resumo.put("lucroEfetivo", formatter.format(totalRecebido-totalPago));
                
                
                JSONObject obj = new JSONObject();
                obj.put("contasPagar", Utils.sortJSONArrayByDate( contasPagar, "expirationDate")) ;
                obj.put("contasReceber", Utils.sortJSONArrayByDate( contasReceber, "expirationDate"));
                obj.put("resumo", resumo);
                obj.put("date", scope);
                
                return obj;
            }
            
            
            
            
            @ResponseBody
            @RequestMapping(value="/accounts", method=RequestMethod.POST)
            public String accounts(
            HttpServletRequest request,
            @RequestParam(value="date", required=true) String date,
            Model model) {
                return getAccounts(date).toString();
            }
            
            
            
            
            
            
            
            
            
            
            
            
            
            @ResponseBody
            @RequestMapping(value="/financeiro/{startDate}/{endDate}", method=RequestMethod.GET)
            public String financeiro(
            HttpServletRequest request,
            @PathVariable(value="startDate", required=true) String startDate,
            @PathVariable(value="endDate", required=true) String endDate,
            Model model) {
                
                
                
                try{
                    
                    String content = "";
                    
                    
                    try{
                        doc = new PDDocument();
                        page = new PDPage();
                        doc.addPage(page);
                        cont = new PDPageContentStream(doc, page);
                        cont.beginText();
                        cont.setFont(PDType1Font.COURIER, 11);
                        cont.setLeading(14.5f);
                        cont.newLineAtOffset(35, 760);
                        
                        
                        
                        List<String> months = getMonths(startDate.replace("-", "/"), endDate.replace("-", "/"));
                        
                        for (String scope : months){
                            
                            Double totalPagar = 0.0;
                            Double totalReceber = 0.0;
                            Double totalPago = 0.0;
                            Double totalRecebido = 0.0;
                            NumberFormat formatter = new DecimalFormat("#0.00");
                            
                            content = ("---------------------------------------------------------------------------------");
                            this.lineBreakVerify(content);
                            content = ("MÊS: "+scope);
                            this.lineBreakVerify(content);
                            
                            //contas recorrentes
                            List<FixedAccount> pagar = new ArrayList<FixedAccount>();
                            List<FixedAccount> receber = new ArrayList<FixedAccount>();
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
                                    if(fixedAccount.getType() == AccountType.P){
                                        pagar.add(fixedAccount);
                                    }else{
                                        receber.add(fixedAccount);
                                    }
                                }
                            }
                            
                            
                            
                            //contas unicas
                            List<Account> _pagar = new ArrayList<Account>();
                            List<Account> _receber = new ArrayList<Account>();
                            Iterable<Account> accounts =  accountRepository.findByScope(scope);
                            for (Account account : accounts){
                                if(account.getType() == AccountType.P){
                                    _pagar.add(account);
                                }else{
                                    _receber.add(account);
                                }
                            }
                            
                            String format = "%1$-40s %2$-14s %3$10s %4$14s";
                            
                            
                            this.lineBreakVerify("");
                            content = ("CONTAS À PAGAR");
                            this.lineBreakVerify(content);
                            
                            content = String.format(format, "DESCR", "PAGO", "VALOR R$", "VENCIMENTO");
                            this.lineBreakVerify(content);
                            
                            content = ("---------------------------------------------------------------------------------");
                            this.lineBreakVerify(content);
                            
                            //contas recorrentes a pagar
                            for (FixedAccount account : pagar){
                                Iterable<Payment> payments = paymentRepository.findByScopeAndFixedAccountId(scope, account.getId());
                                Boolean status = ((Collection<Payment>)payments).size() > 0;
                                
                                content = String.format(format, account.getName(), ( status ? "PAGO  " : "ABERTO"), account.getValue(), account.getExpirationDay()+"/"+scope);
                                this.lineBreakVerify(content);
                                totalPagar += account.getValue();
                                totalPago += status ? account.getValue() : 0;
                            }
                            
                            //contas unicas a pagar
                            for (Account account : _pagar){
                                content = String.format(format, account.getName(), ( account.getPaid() ? "PAGO  " : "ABERTO"), account.getValue(), account.getExpirationDate().getDay()+"/"+scope) ;
                                this.lineBreakVerify(content);
                                totalPagar += account.getValue();
                                totalPago += account.getPaid() ? account.getValue() : 0;
                            }
                            
                            this.lineBreakVerify("");
                            content = ("CONTAS À RECEBER");
                            this.lineBreakVerify(content);
                            
                            content = String.format(format, "DESCR", "PAGO", "VALOR R$", "VENCIMENTO");
                            this.lineBreakVerify(content);
                            content = ("---------------------------------------------------------------------------------");
                            this.lineBreakVerify(content);
                            
                            //contas recorrentes a receber
                            for (FixedAccount account : receber){
                                Iterable<Payment> payments = paymentRepository.findByScopeAndFixedAccountId(scope, account.getId());
                                Boolean status = ((Collection<Payment>)payments).size() > 0;
                                content = String.format(format, account.getName(), ( status ? "PAGO  " : "ABERTO"), account.getValue(), account.getExpirationDay()+"/"+scope);
                                this.lineBreakVerify(content);
                                totalReceber += account.getValue();
                                totalRecebido += status ? account.getValue() : 0;
                            }
                            
                            //contas unicas a receber
                            for (Account account : _receber){
                                content = String.format(format, account.getName(), ( account.getPaid() ? "PAGO  " : "ABERTO"), account.getValue(), account.getExpirationDate().getDay()+"/"+scope) ;
                                this.lineBreakVerify(content);
                                totalReceber += account.getValue();
                                totalRecebido += account.getPaid() ? account.getValue() : 0;
                            }
                            
                            
                            String rformat = "%1$-40s %2$40s";
                            
                            this.lineBreakVerify("");
                            this.lineBreakVerify(String.format(rformat,"RESUMO "+scope, "R$"));
                            this.lineBreakVerify("---------------------------------------------------------------------------------");
                            this.lineBreakVerify(String.format(rformat,"TOTAL À PAGAR:", formatter.format( totalPagar)));
                            this.lineBreakVerify(String.format(rformat,"TOTAL JÁ PAGO:", formatter.format( totalPago)));
                            this.lineBreakVerify(String.format(rformat,"TOTAL À RECEBER:", formatter.format( totalReceber)));
                            this.lineBreakVerify(String.format(rformat,"TOTAL JÀ RECEBIDO:", formatter.format( totalRecebido)));
                            this.lineBreakVerify(String.format(rformat,"RESULTADO ESPERADO:", formatter.format( (totalReceber - totalPagar))));
                            this.lineBreakVerify(String.format(rformat,"RESULTADO EFETIVO:", formatter.format( (totalRecebido - totalPago))));
                            this.lineBreakVerify("");
                            
                            
                        }
                        
                        cont.endText();
                        cont.close();
                        doc.save("relatorio-financeiro.pdf");
                        doc.close();
                        
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                
                
                return "---";
            }
            
            
            
            private void lineBreakVerify(String line){
                System.out.println(numLines +" - "+ line);
                try{
                    cont.showText(line);
                    cont.newLine();
                    if(numLines==50){
                        cont.endText();
                        cont.close();
                        //
                        page = new PDPage();
                        doc.addPage(page);
                        cont = new PDPageContentStream(doc, page);
                        cont.beginText();
                        cont.setFont(PDType1Font.COURIER, 11);
                        cont.setLeading(14.5f);
                        cont.newLineAtOffset(35, 760);
                        numLines = 0;
                    }else{
                        numLines++;
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                
            }
            
            
            
            @ResponseBody
            @RequestMapping(value="/categories", method=RequestMethod.POST)
            public String getCategories(HttpServletRequest request, Model model) {
                Iterable<ProductCategory> productCategories =  productCategoryRepository.findAll();
                JSONArray japc = new JSONArray();
                for (ProductCategory productCategory : productCategories){
                    JSONObject obj = new JSONObject();
                    obj.put("id", productCategory.getId());
                    obj.put("name", productCategory.getName());
                    japc.add(obj);
                }
                return japc.toString();
            }
            
            
            
            @ResponseBody
            @RequestMapping(value="/cadastros", method=RequestMethod.POST)
            public String getCadastros(HttpServletRequest request, Model model) {
                Iterable<Cadastro> cadastros =  cadastroRepository.findAll();
                JSONArray japc = new JSONArray();
                for (Cadastro cadastro : cadastros){
                    JSONObject obj = new JSONObject();
                    obj.put("id", cadastro.getId());
                    obj.put("name", cadastro.getName());
                    obj.put("nameFant", cadastro.getNameFant());
                    obj.put("cpf", cadastro.getCPF());
                    obj.put("cnpj", cadastro.getCNPJ());
                    japc.add(obj);
                }
                return japc.toString();
            }
            
            
            
            
            @ResponseBody
            @RequestMapping(value="/estados", method=RequestMethod.POST)
            public String getEstados(HttpServletRequest request, Model model) {
                
                JSONArray estados = new JSONArray();
                
                JSONObject obj = new JSONObject();
                obj.put("value", "DF");
                obj.put("text", "Distrito Federal");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "ES");
                obj.put("text", "Espírito Santo");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "GO");
                obj.put("text", "Goiás");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "MA");
                obj.put("text", "Maranhão");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "MG");
                obj.put("text", "Minas Gerais");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "MS");
                obj.put("text", "Mato Grosso do Sul");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "MT");
                obj.put("text", "Mato Grosso");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "PA");
                obj.put("text", "Pará");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "PB");
                obj.put("text", "Paraíba");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "PE");
                obj.put("text", "Pernambuco");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "PI");
                obj.put("text", "Piauí");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "PR");
                obj.put("text", "Paraná");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "RJ");
                obj.put("text", "Rio de Janeiro");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "RN");
                obj.put("text", "Rio Grande do Norte");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "RO");
                obj.put("text", "Rondônia");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "RR");
                obj.put("text", "Roraima");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "RS");
                obj.put("text", "Rio Grande do Sul");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "SC");
                obj.put("text", "Santa Catarina");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "SE");
                obj.put("text", "Sergipe");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "AC");
                obj.put("text", "Acre");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "AL");
                obj.put("text", "Alagoas");
                estados.add(obj);
                
                
                obj = new JSONObject();
                obj.put("value", "AM");
                obj.put("text", "Amazonas");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "AP");
                obj.put("text", "Amapá");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "BA");
                obj.put("text", "Bahia");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "CE");
                obj.put("text", "Ceará");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "SP");
                obj.put("text", "São Paulo");
                estados.add(obj);
                
                obj = new JSONObject();
                obj.put("value", "TO");
                obj.put("text", "Tocantins");
                estados.add(obj);
                
                
                return estados.toString();
            }
            
            
            private List<String> getMonths(String startDate, String endDate){
                List<String> months = new ArrayList<String>();
                
                DateFormat formater = new SimpleDateFormat("MM/yyyy");
                
                Calendar beginCalendar = Calendar.getInstance();
                Calendar finishCalendar = Calendar.getInstance();
                
                try {
                    beginCalendar.setTime(formater.parse(startDate));
                    finishCalendar.setTime(formater.parse(endDate));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                while (!beginCalendar.after(finishCalendar)) {
                    String date = formater.format(beginCalendar.getTime());
                    months.add(date);
                    beginCalendar.add(Calendar.MONTH, 1);
                }
                
                
                return months;
            }
            
            
        }