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
import app.model.entity.ServiceItem;
import app.model.repository.ServiceItemRepository;
import app.model.entity.Servico;
import app.model.repository.ServicoRepository;

import java.util.Collection;

@Controller
@RequestMapping(path="/servicos")
public class ServicoController {
	@Autowired
	private ServicoRepository servicoRepository;
    
    @Autowired
	private ServiceItemRepository serviceItemRepository;
    
    
    
	@RequestMapping("/list")
    public String list(HttpServletRequest request, Model model) {
        model.addAttribute("page", "servicos-list");
        return Utils.getLayout(request);
    }
    
    
    
    @ResponseBody
	@RequestMapping(value="/list-all", method=RequestMethod.POST)
    public String listAll(HttpServletRequest request, Model model) {
        Iterable<Servico> servicos =  servicoRepository.findAll();
		JSONArray jap = new JSONArray();
        for (Servico servico : servicos){
			jap.add( toJSON(servico) );
		}
		return jap.toString();
	}
    
    
    
    @ResponseBody
    @RequestMapping(value="/add", method=RequestMethod.POST)
    public String add(
    HttpServletRequest request, 
    @RequestParam(value="id", required=false) int id, 
    @RequestParam(value="name", required=true) String name, 
    @RequestParam(value="price", required=true) Double price, 
    Model model){
        Servico servico = new Servico();
        if(id > 0){
            servico = servicoRepository.getOne(id);
        }
		servico.setName(name);
		servico.setPrice(price);
        
        servicoRepository.save(servico);
        return toJSON(servico).toString();
    }
    
    
    
    
    @ResponseBody
    @RequestMapping(value="/get", method=RequestMethod.POST)
    public String get(@RequestParam(value="id", required=true) Integer id){
		Servico servico = servicoRepository.getOne(id);
        return toJSON(servico).toString();
    }
    
    
    @ResponseBody
    @RequestMapping(value="/remove", method=RequestMethod.POST)
    public void remove(@RequestParam(value="id", required=true) Integer id){
        Servico servico = servicoRepository.getOne(id);
        Iterable<ServiceItem> items = serviceItemRepository.findByServiceId(id);        
        Boolean status = ((Collection<ServiceItem>)items).size() > 0;
        if(!status){
            servicoRepository.delete(servico);
        }
    }
    
    
    
    
    private JSONObject toJSON(Servico servico){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        JSONObject obj = new JSONObject();
        obj.put("id", servico.getId());
        obj.put("name", servico.getName());
        obj.put("price", servico.getPrice());
        return obj;
    }
    
    
}