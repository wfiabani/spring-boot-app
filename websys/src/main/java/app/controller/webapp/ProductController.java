package app.controller.webapp;

import java.text.SimpleDateFormat;

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
import app.model.entity.MovementItem;
import app.model.repository.MovementItemRepository;
import app.model.entity.Product;
import app.model.repository.ProductCategoryRepository;
import app.model.repository.ProductRepository;
import app.model.entity.SKU;
import app.model.repository.SKURepository;
import app.model.entity.UnidadeComercial;


import java.util.Collection;

@Controller
@RequestMapping(path="/products")
public class ProductController {
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductCategoryRepository productCategoryRepository;
    
    @Autowired
	private MovementItemRepository movementItemRepository;
    
    @Autowired
	private SKURepository skuRepository;
	

	@RequestMapping("/list")
    public String list(HttpServletRequest request, Model model) {
        model.addAttribute("page", "products-list");
        return Utils.getLayout(request);
    }



    @ResponseBody
	@RequestMapping(value="/list-all", method=RequestMethod.POST)
    public String listAll(HttpServletRequest request, Model model) {
        Iterable<Product> products =  productRepository.findAll();
		JSONArray jap = new JSONArray();
        for (Product product : products){
			jap.add( toJSON(product) );
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
            @RequestParam(value="ean", required=true) String ean, 
            @RequestParam(value="eanTrib", required=true) String eanTrib, 
            @RequestParam(value="ncm", required=false) String ncm, 
            @RequestParam(value="cest", required=false) String cest, 
            @RequestParam(value="uCom", required=true) String uCom, 
            @RequestParam(value="uTrib", required=true) String uTrib, 
            @RequestParam(value="grupoTrib", required=true) String grupoTrib, 
            @RequestParam(value="productCategoryId", required=true) int productCategoryId, 
            @RequestParam(value="price", required=true) Double price, 
            @RequestParam(value="priceUnTrib", required=true) Double priceUnTrib, 
            @RequestParam(value="cost", required=true) Double cost, 
            @RequestParam(value="trackStock", required=true) Boolean trackStock, 
            Model model){
		Product product = new Product();
		if(id > 0){
            product = productRepository.getOne(id);
        }
		product.setName(name);
		product.setPrice(price);
		product.setPriceUnTrib(priceUnTrib);
		product.setEAN(ean);
		product.setEANTrib(eanTrib);
		product.setNCM(ncm);
		product.setCEST(cest);
		product.setUCom(UnidadeComercial.valueOf(uCom));
		product.setCost(cost);
		product.setTrackStock(trackStock);
		product.setGrupoTrib(grupoTrib);
		try{
			product.setUTrib(UnidadeComercial.valueOf(uTrib));
		}catch(Exception e){
			product.setUTrib(null);
		}
		product.setProductCategory( productCategoryRepository.getOne(productCategoryId) );
        productRepository.save(product);
        
        if(externalId.length()==0){
            product.setExternalId( Utils.generateProductExternalId(product.getId()) );
        }else{
            product.setExternalId( externalId );
        }
        productRepository.save(product);

        return toJSON(product).toString();
    
    }






    @ResponseBody
    @RequestMapping(value="/duplicate", method=RequestMethod.POST)
    public void duplicate(@RequestParam(value="id", required=true) Integer id){
        
        Product product = productRepository.getOne(id);
        Product nProduct = new Product();

        nProduct.setName(product.getName() + " (cópia)");
		nProduct.setPrice(product.getPrice());
		nProduct.setPriceUnTrib(product.getPriceUnTrib());
		nProduct.setEAN(product.getEAN());
		nProduct.setEANTrib(product.getEANTrib());
		nProduct.setNCM(product.getNCM());
		nProduct.setCEST(product.getCEST());
		nProduct.setUCom(product.getUCom());
		nProduct.setCost(product.getCost());
		nProduct.setTrackStock(product.getTrackStock());
		nProduct.setGrupoTrib(product.getGrupoTrib());
        nProduct.setUTrib(product.getUTrib());
        nProduct.setProductCategory( product.getProductCategory() );
        
        productRepository.save(nProduct);
        nProduct.setExternalId( Utils.generateProductExternalId(nProduct.getId()) );
        productRepository.save(nProduct);

    }






    @ResponseBody
    @RequestMapping(value="/get", method=RequestMethod.POST)
    public String get(@RequestParam(value="id", required=true) Integer id){
		Product product = productRepository.getOne(id);
        return toJSON(product).toString();
    }


    @ResponseBody
    @RequestMapping(value="/remove", method=RequestMethod.POST)
    public void remove(@RequestParam(value="id", required=true) Integer id){
        Product product = productRepository.getOne(id);
        Iterable<MovementItem> items = movementItemRepository.findByProductId(id);        
        Iterable<SKU> skus = skuRepository.findByProductId(id);        
        Boolean status = ((Collection<MovementItem>)items).size() > 0 || ((Collection<SKU>)skus).size() > 0;
        if(!status){
            productRepository.delete(product);
        }
    }




    private JSONObject toJSON(Product product){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        JSONObject obj = new JSONObject();
        obj.put("id", product.getId());
        obj.put("name", product.getName());
		obj.put("externalId", product.getExternalId());
		obj.put("ean", product.getEAN());
		obj.put("eanTrib", product.getEANTrib());
		obj.put("ncm", product.getNCM());
		obj.put("cest", product.getCEST());
		obj.put("uCom", product.getUCom().toString());
		obj.put("uTrib", product.getUTrib()==null ? null : product.getUTrib().toString());
		obj.put("productCategoryName", product.getProductCategory().getName());
		obj.put("productCategoryId", product.getProductCategory().getId());
		obj.put("price", product.getPrice());
		obj.put("priceUnTrib", product.getPriceUnTrib());
		obj.put("cost", product.getCost());
		obj.put("trackStock", product.getTrackStock());
		obj.put("grupoTrib", product.getGrupoTrib());
        return obj;
    }


}