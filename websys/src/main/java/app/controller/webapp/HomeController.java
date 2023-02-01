package app.controller.webapp;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import app.helper.Utils;

@Controller
@RequestMapping(path="/home")
public class HomeController {
	
	@RequestMapping("/desktop")
    public String list(HttpServletRequest request, Model model) {
        model.addAttribute("page", "home");
        return Utils.getLayout(request);
    }


}