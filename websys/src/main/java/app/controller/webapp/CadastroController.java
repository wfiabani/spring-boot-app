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
import app.model.entity.Cadastro;
import app.model.repository.CadastroRepository;
import app.model.entity.Movement;
import app.model.repository.MovementRepository;
import app.model.entity.UF;

import java.util.Collection;

@Controller
@RequestMapping(path="/cadastros")
public class CadastroController {
	@Autowired
	private CadastroRepository cadastroRepository;
    
    @Autowired
	private MovementRepository movementRepository;



	@RequestMapping("/list")
    public String list(HttpServletRequest request, Model model) {
        model.addAttribute("page", "cadastros-list");
        return Utils.getLayout(request);
    }



    @ResponseBody
	@RequestMapping(value="/list-all", method=RequestMethod.POST)
    public String listAll(HttpServletRequest request, Model model) {
        Iterable<Cadastro> cadastros =  cadastroRepository.findAll();
		JSONArray jap = new JSONArray();
        for (Cadastro cadastro : cadastros){
			jap.add( toJSON(cadastro) );
		}
        return jap.toString();
	}

    

    @ResponseBody
    @RequestMapping(value="/add", method=RequestMethod.POST)
    public String add(
            HttpServletRequest request, 
            @RequestParam(value="id", required=false) int id, 
            @RequestParam(value="name", required=true) String name, 
            @RequestParam(value="nameFant", required=true) String nameFant, 
            @RequestParam(value="cpf", required=true) String cpf, 
            @RequestParam(value="cnpj", required=true) String cnpj, 
            @RequestParam(value="ie", required=true) String ie, 
            @RequestParam(value="cep", required=true) String cep, 
            @RequestParam(value="pais", required=true) String pais, 
            @RequestParam(value="cPais", required=true) String cPais, 
            @RequestParam(value="indIEDest", required=true) int indIEDest, 
            @RequestParam(value="lgr", required=true) String lgr, 
            @RequestParam(value="num", required=true) String num, 
            @RequestParam(value="bairro", required=true) String bairro, 
            @RequestParam(value="codMun", required=true) String codMun, 
            @RequestParam(value="municipio", required=true) String municipio, 
            @RequestParam(value="uf", required=true) String uf, 
            @RequestParam(value="email", required=true) String email, 
            @RequestParam(value="telefone", required=true) String telefone, 
            Model model){
        Cadastro cadastro = new Cadastro();
        if(id > 0){
            cadastro = cadastroRepository.getOne(id);
        }
		cadastro.setName(name);
		cadastro.setNameFant(nameFant);
		cadastro.setCPF(cpf);
		cadastro.setCNPJ(cnpj);
		cadastro.setIE(ie);
		cadastro.setLgr(lgr);
		cadastro.setNum(num);
		cadastro.setBairro(bairro);
		cadastro.setCodMun(codMun);
		cadastro.setMunicipio(municipio);
        cadastro.setUF(UF.valueOf(uf));
        cadastro.setEmail(email);
        cadastro.setTelefone(telefone);
        cadastro.setCEP(cep);
        cadastro.setPais(pais);
        cadastro.setCPais(cPais);
        cadastro.setIndIEDest(indIEDest);
        

        //teste de stress
        /*
        for (int i = 0; i < 3500; i++) { 
            Cadastro _cadastro = new Cadastro();
            _cadastro.setName("name");
            _cadastro.setNameFant("nameFant");
            _cadastro.setCPF("cpf");
            _cadastro.setCNPJ("cnpj");
            _cadastro.setIE("ie");
            _cadastro.setLgr("lgr");
            _cadastro.setNum("num");
            _cadastro.setBairro("bairro");
            _cadastro.setCodMun("codMun");
            _cadastro.setMunicipio("municipio");
            _cadastro.setUF(UF.RJ);
            cadastroRepository.save(_cadastro);
        }
        */
        
        cadastroRepository.save(cadastro);
        return toJSON(cadastro).toString();
    }




    @ResponseBody
    @RequestMapping(value="/duplicate", method=RequestMethod.POST)
    public void duplicate(@RequestParam(value="id", required=true) Integer id){
        
        Cadastro cadastro = cadastroRepository.getOne(id);
        Cadastro nCadastro = new Cadastro();


        nCadastro.setName(cadastro.getName() + " (cópia)");
		nCadastro.setNameFant(cadastro.getNameFant());
		nCadastro.setCPF(cadastro.getCPF());
		nCadastro.setCNPJ(cadastro.getCNPJ());
		nCadastro.setIE(cadastro.getIE());
		nCadastro.setLgr(cadastro.getLgr());
		nCadastro.setNum(cadastro.getNum());
		nCadastro.setBairro(cadastro.getBairro());
		nCadastro.setCodMun(cadastro.getCodMun());
		nCadastro.setMunicipio(cadastro.getMunicipio());
        nCadastro.setUF(cadastro.getUF());
        nCadastro.setEmail(cadastro.getEmail());
        nCadastro.setTelefone(cadastro.getTelefone());
        nCadastro.setCEP(cadastro.getCEP());
        nCadastro.setPais(cadastro.getPais());
        nCadastro.setCPais(cadastro.getCPais());
        nCadastro.setIndIEDest(cadastro.getIndIEDest());
        
        cadastroRepository.save(nCadastro);
    }


    


    @ResponseBody
    @RequestMapping(value="/get", method=RequestMethod.POST)
    public String get(@RequestParam(value="id", required=true) Integer id){
		Cadastro cadastro = cadastroRepository.getOne(id);
        return toJSON(cadastro).toString();
    }


    @ResponseBody
    @RequestMapping(value="/remove", method=RequestMethod.POST)
    public void remove(@RequestParam(value="id", required=true) Integer id){
        Cadastro cadastro = cadastroRepository.getOne(id);
        Iterable<Movement> items = movementRepository.findByCadastroId(id);        
        Boolean status = ((Collection<Movement>)items).size() > 0;
        if(!status){
            cadastroRepository.delete(cadastro);
        }
    }


    

    private JSONObject toJSON(Cadastro cadastro){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        JSONObject obj = new JSONObject();
        obj.put("id", cadastro.getId());
        obj.put("name", cadastro.getName());
        obj.put("nameFant", cadastro.getNameFant());
        obj.put("cpf", cadastro.getCPF());
        obj.put("cnpj", cadastro.getCNPJ());
        obj.put("ie", cadastro.getIE());
        obj.put("lgr", cadastro.getLgr());
        obj.put("num", cadastro.getNum());
        obj.put("bairro", cadastro.getBairro());
        obj.put("codMun", cadastro.getCodMun());
        obj.put("municipio", cadastro.getMunicipio());
        obj.put("uf", cadastro.getUF().toString());
        obj.put("email", cadastro.getEmail());
        obj.put("telefone", cadastro.getTelefone());
        obj.put("cep", cadastro.getCEP());
        obj.put("pais", cadastro.getPais());
        obj.put("cPais", cadastro.getCPais());
        obj.put("indIEDest", cadastro.getIndIEDest());
        return obj;
    }


}