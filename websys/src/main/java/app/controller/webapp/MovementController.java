package app.controller.webapp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
import app.model.entity.Cadastro;
import app.model.repository.CadastroRepository;
import app.model.entity.Movement;
import app.model.entity.MovementItem;
import app.model.repository.MovementItemRepository;
import app.model.repository.MovementRepository;
import app.model.entity.MovementType;
import app.model.entity.Product;
import app.model.repository.ProductRepository;
import app.model.entity.SKU;
import app.model.repository.SKURepository;
import app.model.entity.ServiceItem;
import app.model.repository.ServiceItemRepository;
import app.model.entity.Servico;
import app.model.repository.ServicoRepository;
import app.model.entity.StockLog;
import app.model.repository.StockLogRepository;;


@Controller
@RequestMapping(path="/movements")
public class MovementController {
    
    @Autowired
    private Environment env;
    
	@Autowired
	private MovementRepository movementRepository;
    
    @Autowired
	private CadastroRepository cadastroRepository;
    
    @Autowired
	private MovementItemRepository movementItemRepository;
    
    @Autowired
	private ProductRepository productRepository;
    
    @Autowired
	private SKURepository skuRepository;
    
    @Autowired
	private ServiceItemRepository serviceItemRepository;
    
    @Autowired
	private ServicoRepository serviceRepository;  
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private StockLogRepository stockLogRepository;
    
    
    @ResponseBody
	//@RequestMapping("/list/{orc}/{mtype}")
	@RequestMapping("/list/{mtype}")
    public String list(
    HttpServletRequest request, 
    //@PathVariable(value="orc", required=true) String orc,
    @PathVariable(value="mtype", required=true) String mtype,
    Model model) {
        return "<list-movements mtype='"+mtype+"' ></list-movements>";
        //return "<list-movements mtype='"+mtype+"' orc='"+orc+"'></list-movements>";
    }
    
    
    
    
    @ResponseBody
	//@RequestMapping(value="/list-all/{orc}/{mtype}", method=RequestMethod.POST)
	@RequestMapping(value="/list-all/{mtype}", method=RequestMethod.POST)
    public String listAll(
    HttpServletRequest request, 
    //@PathVariable(value="orc", required=true) String orc,
    @PathVariable(value="mtype", required=true) String mtype,
    Model model) {
        MovementType type = mtype.equals("c") ? MovementType.C : MovementType.V;
        //Iterable<Movement> movements =  movementRepository.findByTypeAndOrcamento(type, orc.equals("s"));
        Iterable<Movement> movements =  movementRepository.findByType(type);
        JSONArray jap = new JSONArray();
        for (Movement movement : movements){
			jap.add( toJSON(movement) );
		}
		return jap.toString();
	}
    
    
    
    @ResponseBody
    @RequestMapping(value="/add", method=RequestMethod.POST)
    public String add(
    HttpServletRequest request, 
    @RequestParam(value="id", required=false) int id, 
    @RequestParam(value="name", required=true) String name, 
    @RequestParam(value="externalId", required=false) String externalId, 
    @RequestParam(value="type", required=true) String type, 
    @RequestParam(value="orcamento", required=true) Boolean orcamento, 
    @RequestParam(value="freight", required=true) Double freight, 
    @RequestParam(value="otherCosts", required=true) Double otherCosts, 
    @RequestParam(value="total", required=true) Double total, 
    @RequestParam(value="enableCosts", required=true) Boolean enableCosts, 
    @RequestParam(value="cadastroId", required=true) Integer cadastroId, 
    Model model){
        
        Movement movement = new Movement();
        if(id > 0){
            movement = movementRepository.getOne(id);
            orcamento = movement.getOrcamento();
        }else{
            /*
            movement.setNfeChave("");
            movement.setNfeXML("");
            movement.setObs("");*/
            movement.setNfeCancelada(false);
        }
        
        
        //so deixa add/editar se for orçamento
        if(orcamento){
            movement.setName(name);
            //movement.setExternalId(externalId);
            movement.setType(MovementType.valueOf(type));
            movement.setOrcamento(orcamento);
            movement.setFreight(freight);
            movement.setOtherCosts(otherCosts);
            movement.setTotal(total);
            movement.setEnableCosts(enableCosts);
            movement.setCadastro( cadastroRepository.getOne(cadastroId) );
            movementRepository.save(movement);
            //seta o externalId com id, quando o usuário não informar
            if(externalId==null){
                movement.setExternalId(Utils.generateMovementExternalId( type, movement.getId() ));
                movementRepository.save(movement);
            }
            return toJSON(movement).toString();
        }
        return "{}";
    }
    
    
    
    @ResponseBody
    @RequestMapping(value="/get-movement", method=RequestMethod.POST)
    public String getMovement(
    HttpServletRequest request, 
    @RequestParam(value="id", required=true) int id, 
    Model model){
        
        Movement movement = movementRepository.getOne(id);
        JSONObject nfObj = new JSONObject();
        
        nfObj.put("natOp", movement.getType()==MovementType.V ? "Venda de Mercadoria" : "Compra de Mercadoria");
        nfObj.put("tpNF", movement.getType()==MovementType.C ? 0 : 1);
        nfObj.put("tpImp", 1);
        nfObj.put("mod", 55);
        nfObj.put("indPres", 1);
        nfObj.put("tpEmis", 1);
        nfObj.put("modFrete", 9);
        nfObj.put("finNFe", 1);
        nfObj.put("tPag", "01");
        nfObj.put("idDest", 1);
        nfObj.put("indFinal", 1);
        nfObj.put("vFrete", movement.getFreight());
        nfObj.put("vOutro", movement.getOtherCosts());
        nfObj.put("vNF", movement.getTotal()==null ? 0.00 : movement.getTotal());
        nfObj.put("vPag", movement.getTotal()==null ? 0.00 : movement.getTotal());
        nfObj.put("vTroco",0.00);
        nfObj.put("refNFe", "");
        nfObj.put("infAdFisco", "");
        nfObj.put("infCpl", "");
        
        
        JSONObject cadastroObj = cadastroToJSON(movement.getCadastro());
        Iterable<MovementItem> items =  movementItemRepository.findByMovementId(movement.getId());
        JSONArray jap = new JSONArray();
        
        for (MovementItem item : items){
            JSONObject itemObj = new JSONObject();
            Product product = item.getProduct();
            SKU sku = item.getSKU();
            
            itemObj.put("id", item.getId().toString());
            itemObj.put("cProd", sku.getExternalId());
            itemObj.put("cEAN", product.getEAN());
            itemObj.put("xProd", product.getName() + " - " + sku.getName());
            itemObj.put("CFOP", "5101");
            itemObj.put("NCM", product.getNCM());
            itemObj.put("uCom", product.getUCom().toString());
            itemObj.put("qCom", String.valueOf(item.getQtd()));
            itemObj.put("vUnCom", item.getUnitPrice().toString());
            itemObj.put("vProd", item.getSubTotal().toString());
            //itemObj.put("vUnTrib", item.getUnitPrice().toString());
            itemObj.put("uTrib", product.getUTrib().toString());
            itemObj.put("qTrib", String.valueOf(item.getQtd()));
            itemObj.put("vUnTrib", item.getUnitPrice().toString());
            itemObj.put("cEANTrib", product.getEANTrib().toString());
            itemObj.put("grupoTrib", product.getGrupoTrib());
            itemObj.put("indTot", 1);
            itemObj.put("cBenef", "");
            
            
            jap.add( itemObj );
        }
        
        
        JSONObject result = new JSONObject();
        result.put("items", jap);
        result.put("dest", cadastroObj);
        result.put("nf", nfObj);
        return result.toString();
        
        
    }
    
    @ResponseBody
    @RequestMapping(value="/get-cadastro", method=RequestMethod.POST)
    public String getCadastro(
    HttpServletRequest request, 
    @RequestParam(value="id", required=true) int id, 
    Model model){
        Cadastro cadastro = cadastroRepository.getOne(id);
        return cadastroToJSON(cadastro).toString();
    }
    
    
    
    @ResponseBody
    @RequestMapping(value="/get-sku", method=RequestMethod.POST)
    public String getSKU(
    HttpServletRequest request, 
    @RequestParam(value="id", required=true) int id, 
    Model model){
        SKU sku = skuRepository.getOne(id);
        Product product = sku.getProduct();
        return productToJSON(product, sku).toString();
    }
    
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/send-nfe", method=RequestMethod.POST)
    public String sendNFe(
    HttpServletRequest request, 
    @RequestParam(value="obj", required=true) String obj,
    @RequestParam(value="movementId", required=false) int movementId, 
    @RequestParam(value="convert", required=true) Boolean convert, 
    Model model){
        
        if( env.getProperty("nfe.emissor.url").length()==0 ){
            return "{}";
        }
        
        try {
            URL url = new URL(env.getProperty("nfe.emissor.url")+"gera-nfe.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            obj = "obj="+obj;
            
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(obj.getBytes(StandardCharsets.UTF_8));
            }
            
            StringBuilder content;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                content = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }
            connection.disconnect();
            
            
            try{
                //verifica se foi aceita a NFe, 
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(content.toString());
                JSONObject protNFe = (JSONObject) json.get("protNFe");
                JSONObject infProt = (JSONObject) protNFe.get("infProt");
                if( infProt.get("cStat").toString().equals("100") ){
                    if(convert){
                        Movement movement = movementRepository.getOne(movementId);
                        movement.setNfeChave( infProt.get("chNFe").toString() );
                        //movement.setNfeXML( Utils.getURLContent( env.getProperty("nfe.emissor.url")+"nfe/"+infProt.get("chNFe").toString()+"/nota.xml" ));
                        movementRepository.save(movement);
                        convertToTransaction(movement);
                    }else{
                        System.out.println(convert);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return content.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }
    
    
    
    @ResponseBody
    @RequestMapping(value="/cancela-nfe", method=RequestMethod.POST)
    public String cancelNFe(
    HttpServletRequest request, 
    @RequestParam(value="protocolo", required=true) String protocolo, 
    @RequestParam(value="chave", required=true) String chave,
    @RequestParam(value="model", required=true) String model,
    @RequestParam(value="just", required=true) String just,
    Model _model){
        if( env.getProperty("nfe.emissor.url").length()==0 ){
            return "false";
        }
        StringBuilder content = new StringBuilder();
        ////
        try {
            URL url = new URL(env.getProperty("nfe.emissor.url")+"cancela-nfe.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            String params = "chave="+chave+"&protocolo="+protocolo+"&model="+model+"&just="+just;
            
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(params.getBytes(StandardCharsets.UTF_8));
            }
            
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                content = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }
            connection.disconnect();
            
            Boolean cancelada = false;
            ///
            try{
                //verifica se foi aceita a NFe, 
                //retEvento - infEvento - cStat
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(content.toString());
                JSONObject retEvento = (JSONObject) json.get("retEvento");
                JSONObject infEvento = (JSONObject) retEvento.get("infEvento");
                if( infEvento.get("cStat").toString().equals("101") || infEvento.get("cStat").toString().equals("135") || infEvento.get("cStat").toString().equals("155") ){
                    cancelada = true;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            ///
            
            
            //verifica se foi aceito o pedido de cancelamento da NFe, ajusta estoque e parcelas
            if( cancelada ){
                Iterable<Movement> movements = movementRepository.findByNfeChave(chave);
                for(Movement movement : movements){
                    movement.setNfeCancelada(true);
                    movementRepository.save(movement);
                    //ajusta o estoque
                    Iterable<MovementItem> items =  movementItemRepository.findByMovementId(movement.getId());
                    for (MovementItem item : items){
                        Product product = item.getProduct();
                        if(product.getTrackStock()){
                            SKU sku = item.getSKU();
                            //
                            StockLog stockLog = new StockLog();
                            stockLog.setCurrentQtd(sku.getEstoque());
                            //
                            if(movement.getType()==MovementType.V){
                                sku.setEstoque(sku.getEstoque()+item.getQtd());
                            }else{
                                sku.setEstoque(sku.getEstoque()-item.getQtd());
                            }
                            skuRepository.save(sku);
                            stockLog.setSKU(sku);
                            stockLog.setDate(new Date());
                            stockLog.setDescr("Cancelamento de NF");
                            stockLog.setNF( chave );
                            stockLog.setProduct(sku.getProduct());
                            stockLog.setNewQtd(sku.getEstoque());
                            stockLog.setMovement(movement);
                            stockLogRepository.save(stockLog);
                        }
                    }
                    //ajusta as parcelas, no cancelamento, não zera o que já foi pago
                    Iterable<Account> accounts =  accountRepository.findByMovementId(movement.getId());
                    for (Account item : accounts){
                        if(!item.getPaid()){
                            item.setOrcamento(true);
                            accountRepository.save(item);
                        }
                    }   
                    return content.toString();
                }
            }
            return content.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }
        ////
        return content.toString();
    }
    
    
    
    
    
    //se a transação é de produto
    private Boolean productTransaction(Integer movementId){
        Iterable<MovementItem> prod =  movementItemRepository.findByMovementId(movementId);
        Iterable<ServiceItem> serv =  serviceItemRepository.findByMovementId(movementId);
        return serv.spliterator().getExactSizeIfKnown()==0 && prod.spliterator().getExactSizeIfKnown()>0;
    }
    
    //se a transação é de produto
    private Boolean serviceTransaction(Integer movementId){
        Iterable<MovementItem> prod =  movementItemRepository.findByMovementId(movementId);
        Iterable<ServiceItem> serv =  serviceItemRepository.findByMovementId(movementId);
        return serv.spliterator().getExactSizeIfKnown()>0 && prod.spliterator().getExactSizeIfKnown()==0;
    }
    
    //se a transação ainda não possui items adicionados
    private Boolean emptyTransaction(Integer movementId){
        Iterable<MovementItem> prod =  movementItemRepository.findByMovementId(movementId);
        Iterable<ServiceItem> serv =  serviceItemRepository.findByMovementId(movementId);
        return serv.spliterator().getExactSizeIfKnown()==0 && prod.spliterator().getExactSizeIfKnown()==0;
    }
    
    
    //se pode ou não transformar orçamento em transação
    private Boolean allowConvertToTransaction( Integer movementId ){
        Movement movement = movementRepository.getOne(movementId);
        if(serviceTransaction(movementId)){
            return movement.getOrcamento();
        }
        if(movement.getType()==MovementType.V){
            return movement.getNfeChave()!=null && movement.getOrcamento();
        }else{
            return movement.getOrcamento();
        }
    }
    
    
    
    
    //se pode ou não transformar transação em orçamento
    private Boolean allowCancelTransaction( Integer movementId ){
        Movement movement = movementRepository.getOne(movementId);
        return movement.getNfeChave()==null && !movement.getOrcamento();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/convert-to-transaction", method=RequestMethod.POST)
    public String handleConvertToTransaction(
    HttpServletRequest request, 
    @RequestParam(value="id", required=false) int id, 
    Model model){
        Movement movement = movementRepository.getOne(id);
        if(allowConvertToTransaction(id)){
            convertToTransaction(movement);
            return "true";
        }
        return "false";
    }
    
    
    
    private void convertToTransaction(Movement movement){
        if(allowConvertToTransaction(movement.getId())){
            movement.setOrcamento(false);
            movementRepository.save(movement);
            //ajusta o estoque
            Iterable<MovementItem> items =  movementItemRepository.findByMovementId(movement.getId());
            JSONArray jap = new JSONArray();
            for (MovementItem item : items){
                Product product = item.getProduct();
                if(product.getTrackStock()){
                    SKU sku = item.getSKU();
                    //
                    StockLog stockLog = new StockLog();
                    stockLog.setCurrentQtd(sku.getEstoque());
                    //
                    if(movement.getType()==MovementType.V){
                        sku.setEstoque(sku.getEstoque()-item.getQtd());
                    }else{
                        sku.setEstoque(sku.getEstoque()+item.getQtd());
                    }
                    skuRepository.save(sku);
                    stockLog.setSKU(sku);
                    stockLog.setDate(new Date());
                    stockLog.setDescr("Nova Transação");
                    if(movement.getNfeChave().length()>0){
                        stockLog.setNF( movement.getNfeChave() );
                    }
                    stockLog.setMovement(movement);
                    stockLog.setProduct(sku.getProduct());
                    stockLog.setNewQtd(sku.getEstoque());
                    stockLogRepository.save(stockLog);
                    
                }
            }
            //ajusta as parcelas
            Iterable<Account> accounts =  accountRepository.findByMovementId(movement.getId());
            for (Account item : accounts){
                item.setOrcamento(false);
                accountRepository.save(item);
            }
        }
    }
    
    
    
    
    
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/cancel-transaction", method=RequestMethod.POST)
    public String handleCancelTransaction(
    HttpServletRequest request, 
    @RequestParam(value="id", required=false) int id, 
    Model model){
        Movement movement = movementRepository.getOne(id);
        if(allowCancelTransaction(movement.getId())){
            cancelTransaction(movement);
            return "true";
        }
        return "false";
    }
    
    
    
    private void cancelTransaction(Movement movement){
        if(allowCancelTransaction(movement.getId())){
            movement.setOrcamento(true);
            movementRepository.save(movement);
            //ajusta o estoque
            Iterable<MovementItem> items =  movementItemRepository.findByMovementId(movement.getId());
            JSONArray jap = new JSONArray();
            for (MovementItem item : items){
                Product product = item.getProduct();
                System.out.println("Produto: "+product.getName());
                if(product.getTrackStock()){
                    SKU sku = item.getSKU();
                    //
                    StockLog stockLog = new StockLog();
                    stockLog.setCurrentQtd(sku.getEstoque());
                    //
                    if(movement.getType()==MovementType.V){
                        sku.setEstoque(sku.getEstoque()+item.getQtd());
                    }else{
                        sku.setEstoque(sku.getEstoque()-item.getQtd());
                    }
                    skuRepository.save(sku);
                    stockLog.setSKU(sku);
                    stockLog.setDate(new Date());
                    stockLog.setDescr("Cancelamento de Transação");
                    if(movement.getNfeChave().length()>0){
                        stockLog.setNF( movement.getNfeChave() );
                    }
                    stockLog.setMovement(movement);
                    stockLog.setProduct(sku.getProduct());
                    stockLog.setNewQtd(sku.getEstoque());
                    stockLogRepository.save(stockLog);
                }
            }
            //ajusta as parcelas, no cancelamento, não zera o que já foi pago
            Iterable<Account> accounts =  accountRepository.findByMovementId(movement.getId());
            for (Account item : accounts){
                if(!item.getPaid()){
                    item.setOrcamento(true);
                    accountRepository.save(item);
                }
            }
        }
    }
    
    
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/add-item", method=RequestMethod.POST)
    public String addItem(
    HttpServletRequest request, 
    @RequestParam(value="id", required=false) int id, 
    @RequestParam(value="productId", required=true) Integer productId, 
    @RequestParam(value="skuId", required=true) Integer skuId, 
    @RequestParam(value="movementId", required=true) Integer movementId, 
    @RequestParam(value="qtd", required=true) Integer qtd, 
    @RequestParam(value="unitPrice", required=true) Double unitPrice, 
    @RequestParam(value="ajustarEstoque", required=true) Boolean ajustarEstoque, 
    @RequestParam(value="subTotal", required=true) Double subTotal, 
    Model model){
        Movement movement = movementRepository.getOne(movementId);
        if( movement.getOrcamento() && !serviceTransaction(movementId) ){
            MovementItem item = new MovementItem();
            if(id > 0){
                item = movementItemRepository.getOne(id);
            }
            item.setProduct(productRepository.getOne(productId));
            item.setMovement(movementRepository.getOne(movementId));
            item.setSKU(skuRepository.getOne(skuId));
            item.setQtd(qtd);
            item.setUnitPrice(unitPrice);
            item.setSubTotal(subTotal);
            movementItemRepository.save(item);
            if(ajustarEstoque){
                //função que ajusta o estoque
                //lembrar de validar se o cara alterou o sku antes de ajustar o estoque. se ligue aqui
            }
            return itemToJSON(item).toString();
        }
        return "";
    }
    
    
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/get-items/{movementId}", method=RequestMethod.POST)
    public String getItems(
    HttpServletRequest request, 
    @PathVariable(value="movementId", required=true) Integer movementId,
    Model model) {
        Iterable<MovementItem> items =  movementItemRepository.findByMovementId(movementId);
        JSONArray jap = new JSONArray();
        for (MovementItem item : items){
            jap.add( itemToJSON(item) );
        }
        return jap.toString();
    }
    
    
    
    @ResponseBody
    @RequestMapping(value="/add-service-item", method=RequestMethod.POST)
    public String addServiceItem(
    HttpServletRequest request, 
    @RequestParam(value="id", required=false) int id, 
    @RequestParam(value="serviceId", required=true) Integer serviceId, 
    @RequestParam(value="name", required=true) String name, 
    @RequestParam(value="movementId", required=true) Integer movementId, 
    @RequestParam(value="subTotal", required=true) Double subTotal, 
    Model model){
        Movement movement = movementRepository.getOne(movementId);
        if( movement.getOrcamento() && !productTransaction(movementId) ){
            ServiceItem item = new ServiceItem();
            if(id > 0){
                item = serviceItemRepository.getOne(id);
            }
            item.setName(name);
            item.setService(serviceRepository.getOne(serviceId));
            item.setMovement(movementRepository.getOne(movementId));
            item.setSubTotal(subTotal);
            
            serviceItemRepository.save(item);
            return serviceItemToJSON(item).toString();
        }
        return "{}";
    }
    
    
    
    @ResponseBody
    @RequestMapping(value="/get-service-items/{movementId}", method=RequestMethod.POST)
    public String getServiceItems(
    HttpServletRequest request, 
    @PathVariable(value="movementId", required=true) Integer movementId,
    Model model) {
        Iterable<ServiceItem> items =  serviceItemRepository.findByMovementId(movementId);
        JSONArray jap = new JSONArray();
        for (ServiceItem item : items){
            jap.add( serviceItemToJSON(item) );
        }
        return jap.toString();
    }
    
    
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/get", method=RequestMethod.POST)
    public String get(@RequestParam(value="id", required=true) Integer id){
        Movement movement = movementRepository.getOne(id);
        return toJSON(movement).toString();
    }
    
    
    @ResponseBody
    @RequestMapping(value="/get-item", method=RequestMethod.POST)
    public String getItem(@RequestParam(value="id", required=true) Integer id){
        MovementItem item = movementItemRepository.getOne(id);
        return itemToJSON(item).toString();
    }
    
    
    @ResponseBody
    @RequestMapping(value="/get-service-item", method=RequestMethod.POST)
    public String getServiceItem(@RequestParam(value="id", required=true) Integer id){
        ServiceItem item = serviceItemRepository.getOne(id);
        return serviceItemToJSON(item).toString();
    }
    
    
    @ResponseBody
    @RequestMapping(value="/remove", method=RequestMethod.POST)
    public void remove(@RequestParam(value="id", required=true) Integer id){
        Movement movement = movementRepository.getOne(id);
        if( movement.getOrcamento() ){
            //remove os produtos
            Iterable<MovementItem> productItems =  movementItemRepository.findByMovementId(id);
            JSONArray jap = new JSONArray();
            for (MovementItem item : productItems){
                movementItemRepository.delete(item);
            }
            //remove os servicos
            Iterable<ServiceItem> productServices =  serviceItemRepository.findByMovementId(id);
            for (ServiceItem item : productServices){
                serviceItemRepository.delete(item);
            }
            //remove as parcelas
            Iterable<Account> accounts =  accountRepository.findByMovementId(id);
            for (Account item : accounts){
                item.setOrcamento(false);
                accountRepository.save(item);
            }
            //remove o movimento
            movementRepository.delete(movement);
        }
    }
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/remove-item", method=RequestMethod.POST)
    public void removeItem(@RequestParam(value="id", required=true) Integer id){
        MovementItem item = movementItemRepository.getOne(id);
        Movement movement = movementRepository.getOne( item.getMovement().getId() );
        if( movement.getOrcamento() ){
            movementItemRepository.delete(item);
        }
    }
    
    
    @ResponseBody
    @RequestMapping(value="/remove-service-item", method=RequestMethod.POST)
    public void removeServiceItem(@RequestParam(value="id", required=true) Integer id){
        ServiceItem item = serviceItemRepository.getOne(id);
        Movement movement = movementRepository.getOne( item.getMovement().getId() );
        if( movement.getOrcamento()){
            serviceItemRepository.delete(item);
        }
    }
    
    
    private JSONObject toJSON(Movement movement){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        JSONObject obj = new JSONObject();
        obj.put("id", movement.getId());
        obj.put("name", movement.getName());
        obj.put("externalId", movement.getExternalId());
        obj.put("nfeChave", movement.getNfeChave()==null ? "" : movement.getNfeChave());
        //obj.put("obs", movement.getObs());
        obj.put("type", movement.getType().toString());
        obj.put("creationDate", formatter.format(movement.getCreationDate()));
        obj.put("lastModifiedDate", formatter.format(movement.getLastModifiedDate()));
        obj.put("orcamento", movement.getOrcamento());
        obj.put("nfeCancelada", movement.getNfeCancelada());
        obj.put("freight", movement.getFreight());
        obj.put("otherCosts", movement.getOtherCosts());
        obj.put("total", movement.getTotal()==null ? 0.00 : movement.getTotal());
        obj.put("enableCosts", movement.getEnableCosts()==null ? false : movement.getEnableCosts());
        obj.put("cadastroId", movement.getCadastro().getId());
        obj.put("cadastroName", movement.getCadastro().getName());
        
        
        String status = "";
        if(movement.getNfeCancelada()==true){
            status = "CANCELADA";
        }else if(movement.getNfeChave()!=null){
            status = "AUTORIZADA";
        }
        obj.put("status", status);
        
        //obj.put("");
        return obj;
        
    }
    
    
    
    
    private JSONObject itemToJSON(MovementItem item){
        JSONObject obj = new JSONObject();
        Movement movement = item.getMovement();
        Product product = item.getProduct();
        SKU sku = item.getSKU();
        obj.put("id", item.getId());
        obj.put("productId", product.getId());
        obj.put("productName", product.getName());
        obj.put("productPrice", product.getPrice());
        obj.put("productCost", product.getCost());
        obj.put("movementId", movement.getId());
        obj.put("skuId", sku.getId());
        obj.put("skuName", sku.getName());
        obj.put("skuEstoque", sku.getEstoque());
        obj.put("unitPrice", item.getUnitPrice());
        obj.put("subTotal", item.getSubTotal());
        obj.put("qtd", item.getQtd());
        return obj;
    }
    
    
    
    private JSONObject serviceItemToJSON(ServiceItem item){
        JSONObject obj = new JSONObject();
        Movement movement = item.getMovement();
        Servico service = item.getServico();
        obj.put("id", item.getId());
        obj.put("name", item.getName());
        obj.put("serviceId", service.getId());
        obj.put("serviceName", service.getName());
        obj.put("servicePrice", service.getPrice());
        obj.put("movementId", movement.getId());
        obj.put("subTotal", item.getSubTotal());
        return obj;
    }
    
    
    private JSONObject cadastroToJSON(Cadastro cadastro){
        JSONObject cadastroObj = new JSONObject();
        cadastroObj.put("id", cadastro.getId());
        cadastroObj.put("xNome", cadastro.getName());
        cadastroObj.put("xPais", cadastro.getPais());
        cadastroObj.put("cPais", cadastro.getCPais());
        cadastroObj.put("CEP", cadastro.getCEP());
        //cadastroObj.put("cUF", 43);
        cadastroObj.put("CPF", cadastro.getCPF());
        cadastroObj.put("CNPJ", cadastro.getCNPJ());
        cadastroObj.put("IE", cadastro.getIE());
        cadastroObj.put("xLgr", cadastro.getLgr());
        cadastroObj.put("nro", cadastro.getNum());
        cadastroObj.put("xBairro", cadastro.getBairro());
        cadastroObj.put("cMun", cadastro.getCodMun());
        cadastroObj.put("xMun", cadastro.getMunicipio());
        cadastroObj.put("UF", cadastro.getUF().toString());
        cadastroObj.put("indIEDest", cadastro.getIndIEDest());
        return cadastroObj;
    }
    
    
    
    //usado apenas para add o prod na nfe
    private JSONObject productToJSON(Product product, SKU sku){
        JSONObject itemObj = new JSONObject();
        //itemObj.put("id", item.getId().toString());
        itemObj.put("cProd", sku.getExternalId());
        itemObj.put("cEAN", product.getEAN().toString());
        itemObj.put("xProd", product.getName() + " - " + sku.getName());
        itemObj.put("CFOP", "5101");
        itemObj.put("NCM", product.getNCM().toString());
        itemObj.put("uCom", product.getUCom().toString());
        itemObj.put("qCom", "1");
        itemObj.put("vUnCom", product.getPrice().toString());
        itemObj.put("vProd", product.getPrice().toString());
        
        //itemObj.put("vUnTrib", product.getUTrib().toString());
        itemObj.put("uTrib", product.getUTrib().toString());
        
        itemObj.put("qTrib", "1");
        itemObj.put("vUnTrib", product.getPrice().toString());
        itemObj.put("cEANTrib", product.getEANTrib().toString());
        itemObj.put("grupoTrib", product.getGrupoTrib().toString());
        itemObj.put("indTot", 1);
        itemObj.put("cBenef", "");
        return itemObj;
    }
    
    
    
}