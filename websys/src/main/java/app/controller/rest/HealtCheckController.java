package app.controller.rest;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping(path="/rest")
public class HealtCheckController {

    @GetMapping({ "/healt-check" })
    public String healtCheck() {
        return "It's working!";
    }

}