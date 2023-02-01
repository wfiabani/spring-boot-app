package app.controller.webapp;

import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import app.helper.Utils;
import app.model.entity.MovementItem;
import app.model.repository.MovementItemRepository;
import app.model.entity.Product;
import app.model.repository.ProductRepository;
import app.model.entity.SKU;
import app.model.repository.SKURepository;
import app.model.entity.StockLog;
import app.model.repository.StockLogRepository;

@Controller
@RequestMapping(path="/skus")
public class SKUController {
	@Autowired
	private ProductRepository productRepository;
    
    @Autowired
	private SKURepository skuRepository;
    
    @Autowired
    private MovementItemRepository movementItemRepository;
    
    @Autowired
    private StockLogRepository stockLogRepository;
    
    
	@RequestMapping(value="/list/{id}", method=RequestMethod.GET)
    public String list(
    HttpServletRequest request, 
    @PathVariable(value="id", required=false) int id,
    Model model) {
        model.addAttribute("page", "skus-list");
        return Utils.getLayout(request);
    }
    
    
	@RequestMapping("/list")
    public String list(HttpServletRequest request, Model model) {
        model.addAttribute("page", "skus-list");
        return Utils.getLayout(request);
    }
    
    
    @ResponseBody
	@RequestMapping(value="/list-all", method=RequestMethod.GET)
    public String listAll(HttpServletRequest request, Model model) {
        Iterable<SKU> skus;
        skus =  skuRepository.findAll();
        JSONArray jap = new JSONArray();
        for (SKU sku : skus){
            jap.add( toJSON(sku) );
        }
        return jap.toString();
    }
    
    
    @ResponseBody
	@RequestMapping(value="/list-all/{id}", method=RequestMethod.GET)
    public String listAllByProductId(
    HttpServletRequest request, 
    @PathVariable(value="id", required=false) int id,
    Model model) {
        Iterable<SKU> skus;
        skus =  skuRepository.findByProductId(id);
        JSONArray jap = new JSONArray();
        for (SKU sku : skus){
            jap.add( toJSON(sku) );
        }
        return jap.toString();
    }
    
    
    
    
    
    @ResponseBody
	@RequestMapping(value="/deficit-stock", method=RequestMethod.POST)
    public String listSKUsByDeficitStock(
    HttpServletRequest request, 
    Model model) {
        Iterable<SKU> skus;
        skus =  skuRepository.xespa();
        JSONArray jap = new JSONArray();
        for (SKU sku : skus){
            Product product = sku.getProduct();
            if(product.getTrackStock()){
                jap.add( toJSON(sku) );
            }
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
    @RequestParam(value="productId", required=true) int productId, 
    @RequestParam(value="estoque", required=true) int estoque, 
    @RequestParam(value="estoqueMin", required=true) int estoqueMin, 
    Model model){
        SKU sku = new SKU();
        Product product = productRepository.getOne(productId);
        
        //
        StockLog stockLog = new StockLog();
        //
        
        
        if(id > 0){
            sku = skuRepository.getOne(id);
            stockLog.setDescr("Ajuste Manual de Estoque");
            stockLog.setCurrentQtd(sku.getEstoque());
        }else{
            stockLog.setDescr("Novo Produto");
            stockLog.setCurrentQtd(0);
        }
        
		sku.setName(name);
		sku.setEstoque(estoque);
		sku.setEstoqueMin(estoqueMin);
        sku.setProduct( product );
        
        skuRepository.save(sku);
        
        if(externalId.length()==0){
            sku.setExternalId( Utils.generateSkuExternalId(sku.getId(), product.getId()) );
        }else{
            sku.setExternalId( externalId );
        }
        skuRepository.save(sku);
        stockLog.setSKU(sku);
        stockLog.setDate(new Date());
        stockLog.setProduct(sku.getProduct());
        stockLog.setNewQtd(sku.getEstoque());
        stockLogRepository.save(stockLog);

        
        return toJSON(sku).toString();
    }
    
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/duplicate", method=RequestMethod.POST)
    public void duplicate(@RequestParam(value="id", required=true) Integer id){
        SKU sku = skuRepository.getOne(id);
        SKU nSKU = new SKU();
        
        //
        StockLog stockLog = new StockLog();
        //
        
        nSKU.setName(sku.getName() + " (cópia)");
		nSKU.setEstoque(sku.getEstoque());
		nSKU.setEstoqueMin(sku.getEstoqueMin());
		nSKU.setProduct( sku.getProduct() );
        skuRepository.save(nSKU);
        nSKU.setExternalId( Utils.generateSkuExternalId(nSKU.getId(), nSKU.getProduct().getId()) );
        
        
        skuRepository.save(nSKU);

        stockLog.setSKU(nSKU);
        stockLog.setDate(new Date());
        stockLog.setDescr("Item duplicado");
        stockLog.setProduct(nSKU.getProduct());
        stockLog.setCurrentQtd(0);
        stockLog.setNewQtd(nSKU.getEstoque());
        stockLogRepository.save(stockLog);
    }
    
    
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/get", method=RequestMethod.POST)
    public String get(@RequestParam(value="id", required=true) Integer id){
		SKU sku = skuRepository.getOne(id);
        return toJSON(sku).toString();
    }
    
    
    @ResponseBody
    @RequestMapping(value="/remove", method=RequestMethod.POST)
    public void remove(@RequestParam(value="id", required=true) Integer id){
        SKU sku = skuRepository.getOne(id);
        Iterable<MovementItem> items = movementItemRepository.findBySkuId(id);        
        Boolean status = ((Collection<MovementItem>)items).size() > 0;
        if(!status){
            skuRepository.delete(sku);
        }
    }
    
    
    
    
    
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/ajustar-estoque", method=RequestMethod.POST)
    public String ajustarEstoque(
    HttpServletRequest request, 
    @RequestParam(value="obj", required=true) String obj, 
    Model model) throws Exception{
        try{
            JSONParser parser = new JSONParser();
            JSONObject nfeObj = (JSONObject) parser.parse(obj);
            JSONObject infos = (JSONObject) nfeObj.get("infos");
            JSONArray items = (JSONArray) nfeObj.get("items");
            for (int i = 0; i < items.size(); i++){
                JSONObject item = ( (JSONObject) items.get(i));
                Integer action  = Integer.valueOf( ((Long) item.get("action")).toString() );
                if(action<2 ){
                    try{
                        Integer qCom  = Integer.valueOf( ((Long) item.get("qCom")).toString() );
                        Integer skuId  = Integer.valueOf( ((Long) item.get("skuId")).toString() );
                        SKU sku = skuRepository.getOne(skuId );
                        //
                        StockLog stockLog = new StockLog();
                        stockLog.setCurrentQtd(sku.getEstoque());
                        //
                        if( action==0 ){ //add
                            sku.setEstoque(sku.getEstoque() + qCom);
                        }else if( action==1 ){ //remove
                            sku.setEstoque(sku.getEstoque() - qCom);
                        }
                        skuRepository.save(sku);
                        stockLog.setSKU(sku);
                        stockLog.setDate(new Date());
                        stockLog.setDescr("Importação de NF");
                        stockLog.setNF( (String) infos.get("chNFe") );
                        stockLog.setProduct(sku.getProduct());
                        stockLog.setNewQtd(sku.getEstoque());
                        stockLogRepository.save(stockLog);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return "{}";
    }
    
    
    
    
    
    
    
    
    
    private JSONObject toJSON(SKU sku){
        Product product = sku.getProduct();
        JSONObject obj = new JSONObject();
        obj.put("id", sku.getId());
        obj.put("name", sku.getName());
        obj.put("externalId", sku.getExternalId());
        obj.put("productId", product.getId());
        obj.put("productExternalId", product.getExternalId());
        obj.put("productName", product.getName());
        obj.put("estoque", sku.getEstoque());
        obj.put("estoqueMin", sku.getEstoqueMin());
		//obj.put("productCategoryName", product.getProductCategory().getName());
		//obj.put("productCategoryId", product.getProductCategory().getId());
        return obj;
    }
    
}