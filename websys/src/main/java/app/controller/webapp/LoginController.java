package app.controller.webapp;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import app.model.repository.UserRepository;
import app.model.entity.User;

@RequestMapping("/security")
@Controller
public class LoginController {
    
    
    @Autowired
	private UserRepository userRepository;
    
    
	@RequestMapping("/login-form")
    public String loginForm(HttpServletRequest request, Model model) {
        return "login-form";
    }
    
    
    @ResponseBody
	@RequestMapping(value="/login", method=RequestMethod.POST)
    public String login(
    HttpServletRequest request, 
    HttpSession session,
    @RequestParam(value="login", required=true) String login, 
    @RequestParam(value="password", required=true) String password, 
    Model model){
        List<User> users = userRepository.findByLogin(login);
        if( users.size()==1 && password.equals( users.get(0).getPassword() )){
            session.setAttribute("user", users.get(0));
            return "true";
        }
        return "false";
    }
    
    
    
	@RequestMapping(value="/logout")
    public String logout(
    HttpServletRequest request, 
    HttpSession session,
    Model model){
        session.removeAttribute("user");
        return "login-form";
    }
    
    
	
}