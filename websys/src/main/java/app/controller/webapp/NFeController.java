package app.controller.webapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import app.model.entity.Product;
import app.model.repository.ProductRepository;
import app.model.entity.SKU;
import app.model.repository.SKURepository;

import app.helper.Utils;

@Controller
@RequestMapping(path="/nfe")
public class NFeController {
    
    
    @Autowired
    private Environment env;

    @Autowired
    private Utils utils;
    
    @Autowired
    private SKURepository skuRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    
	@RequestMapping("/list")
    public String list(HttpServletRequest request, Model model) {
        if( env.getProperty("nfe.emissor.url").length()==0 ){
            model.addAttribute("page", "nfe-disabled");
        }else{
            model.addAttribute("page", "nfe-list");
        }
        return Utils.getLayout(request);
    }
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/send-mail", method=RequestMethod.POST)
    public String sendMail(
    HttpServletRequest request, 
    @RequestParam(value="to", required=true) String to, 
    @RequestParam(value="chave", required=true) String chave,
    Model model){
        
        if( env.getProperty("nfe.emissor.url").length()==0 ){
            return "false";
        }
        
        String danfe = "/tmp/"+chave+"-danfe.pdf";
        String nota = "/tmp/"+chave+"-nota.xml";
        this.getFile(env.getProperty("nfe.emissor.url")+"nfe/"+chave+"/danfe.pdf", danfe);
        this.getFile(env.getProperty("nfe.emissor.url")+"nfe/"+chave+"/nota.xml", nota);
        
        try{
            String[] files = {danfe, nota};
            utils.sendMail(to, "NFe em anexo", "Em anexo DANFE e NFe", files);
        }catch(Exception e){
            return "false";
        }
        
        return "true";
    }
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/list-all", method=RequestMethod.POST)
    public String listAll(
    HttpServletRequest request,
    Model model){
        if( env.getProperty("nfe.emissor.url").length()==0 ){
            return "false";
        }
        return this.jsonGetRequest(env.getProperty("nfe.emissor.url")+"lista-nfe.php");
    }
    
    
    
    @ResponseBody
    @RequestMapping(value="/get-nfe", method=RequestMethod.POST)
    public String getNFe(
    HttpServletRequest request, 
    @RequestParam(value="chave", required=true) String chave,
    Model model){
        if( env.getProperty("nfe.emissor.url").length()==0 ){
            return "false";
        }
        return this.jsonGetRequest(env.getProperty("nfe.emissor.url")+"get-nfe.php?chave="+chave);
    }
    
    
    
    
    @RequestMapping(value = "/download/{chave}/{file_name}", method = RequestMethod.GET )
    @ResponseBody
    public FileSystemResource downloadFile(
    @PathVariable("file_name") String fileName, 
    @PathVariable("chave") String chave, 
    HttpServletResponse response) {
        
        
        if( env.getProperty("nfe.emissor.url").length()>0 ){
            //
        }
        
        if(fileName.indexOf("danfe")>-1){
            fileName += ".pdf";
            response.setContentType("application/pdf"); 
        }else{ //se não for pdf, é sempre xml
            fileName += ".xml";
            response.setContentType("application/xml");    
        }
        
        String file = "/tmp/"+chave+"-"+fileName;
        this.getFile(env.getProperty("nfe.emissor.url")+"nfe/"+chave+"/"+fileName, file);
        
        
        response.setHeader("Content-Disposition", "attachment; filename="+fileName);
        
        FileSystemResource stream = new FileSystemResource(new File(file)); 
        return stream;
        
    }
    
    
    private void getFile(String urlToDownload, String file){
        try{
            URL url = new URL(urlToDownload);
            Path targetPath = new File(file).toPath();
            Files.copy(url.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
    private static String streamToString(InputStream inputStream) {
        String text = new Scanner(inputStream, "UTF-8").useDelimiter("\\Z").next();
        return text;
    }
    
    public static String jsonGetRequest(String urlQueryString) {
        String json = null;
        try {
            URL url = new URL(urlQueryString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.connect();
            InputStream inStream = connection.getInputStream();
            json = streamToString(inStream); // input stream to string
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }
    
    
    
    @ResponseBody
    @RequestMapping(value="/parse-xml", method=RequestMethod.POST)
    public String readFromXml(
    HttpServletRequest request, 
    @RequestParam("file") MultipartFile file,
    Model model) throws Exception{
        if( env.getProperty("nfe.emissor.url").length()==0 ){
            return "false";
        }
        if( file.isEmpty() ){
            return "false";
        }

        JSONObject objNFe = new JSONObject();
        JSONObject infosNFe = new JSONObject();
        JSONArray items = new JSONArray();
        try{

            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get("/tmp/" + "file" + ".xml");
            Files.write(path, bytes);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse( path.toString() );
            Integer tpNF = Integer.valueOf(doc.getElementsByTagName("tpNF").item(0).getTextContent());
            infosNFe.put("chNFe", doc.getElementsByTagName("chNFe").item(0).getTextContent());
            infosNFe.put("tpNF", doc.getElementsByTagName("tpNF").item(0).getTextContent());
            infosNFe.put("natOp", doc.getElementsByTagName("natOp").item(0).getTextContent());
            infosNFe.put("dhEmi", doc.getElementsByTagName("dhEmi").item(0).getTextContent());
            
            NodeList nList = doc.getElementsByTagName("prod");
            
            
            Node emit = doc.getElementsByTagName("emit").item(0);
            Element elEmit = (Element) emit;
            infosNFe.put("emitCNPJ", elEmit.getElementsByTagName("CNPJ").item(0).getTextContent());
            infosNFe.put("emitXNome", elEmit.getElementsByTagName("xNome").item(0).getTextContent());
            
            try{

                Node dest = doc.getElementsByTagName("dest").item(0);
                Element elDest = (Element) dest;
                infosNFe.put("destCNPJ", elDest.getElementsByTagName("CNPJ").item(0).getTextContent());
                infosNFe.put("destXNome", elDest.getElementsByTagName("xNome").item(0).getTextContent());
            }catch(Exception e){
                infosNFe.put("destCNPJ", "");
                infosNFe.put("destXNome", "Consumidor não identificado");
            }

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                Element eElement = (Element) nNode;
                
                String xProd = eElement.getElementsByTagName("xProd").item(0).getTextContent();
                String cProd = eElement.getElementsByTagName("cProd").item(0).getTextContent();
                String cEAN = eElement.getElementsByTagName("cEAN").item(0).getTextContent();
                Integer qCom = Integer.valueOf( eElement.getElementsByTagName("qCom").item(0).getTextContent() );
                
                List<SKU> skusByExternalId = skuRepository.findByExternalId(cProd);


                List<Product> prodsByEan = productRepository.findByEan(cEAN);

                JSONObject item = new JSONObject();
                item.put("xProd", xProd); 
                item.put("cProd", cProd); 
                item.put("qCom", qCom); 

                
                if(((Collection<SKU>)skusByExternalId).size() == 1){
                    item.put("action", 0); 
                    SKU sku = skusByExternalId.get(0);
                    Integer stockLevel = sku.getEstoque();
                    item.put("skuExternalId", sku.getExternalId()); 
                    item.put("skuId", sku.getId()); 
                    item.put("skuName", sku.getName()); 
                    item.put("productName", sku.getProduct().getName()); 
                }else if(((Collection<Product>)prodsByEan).size() == 1){
                    List<SKU> skusByCEAN = skuRepository.findByProductId( prodsByEan.get(0).getId() );
                    if(((Collection<SKU>)skusByCEAN).size() == 1){
                        item.put("action", 0); 
                        SKU sku = skusByCEAN.get(0);
                        Integer stockLevel = sku.getEstoque();
                        item.put("skuExternalId", sku.getExternalId()); 
                        item.put("skuId", sku.getId()); 
                        item.put("skuName", sku.getName()); 
                        item.put("productName", sku.getProduct().getName()); 
                    }
                }else{
                    item.put("action", 2);
                    item.put("skuExternalId", null); 
                    item.put("skuId", null); 
                    item.put("skuName", null); 
                    item.put("productName", null); 
                }
                items.add(item);


            }
        }catch(Exception e){
            e.printStackTrace();
        }


        objNFe.put("infos", infosNFe);
        objNFe.put("items", items);


        return objNFe.toJSONString();
        
    }
    
    
    
    
    
    
    
}