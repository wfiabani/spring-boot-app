package app.controller.rest;

import app.model.entity.User;
import app.model.repository.UserRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;


    @ResponseBody
    @RequestMapping(value="/rest/user/{login}/update-password", method= RequestMethod.PUT)
    public String updatePassword(
            @PathVariable(value="login", required=true) String login,
            @RequestBody Map<String, Object> object
    ){
        User usr = getUser(login);
        if( usr!=null && !isRoot(usr)){
            usr.setPassword(object.get("password").toString());
            userRepository.save(usr);
        }
        return toJSON(usr).toString();
    }


    @ResponseBody
    @RequestMapping(value="/rest/user/{login}/delete", method= RequestMethod.DELETE)
    public String updatePassword(
            @PathVariable(value="login", required=true) String login
    ){
        User usr = getUser(login);
        if( usr!=null && !isRoot(usr)){
            userRepository.delete(usr);
        }
        List<User> users = userRepository.findAll();
        JSONArray result = new JSONArray();
        for(User user : users){
            result.add(toJSON(user));
        }
        return result.toString();
    }


    @ResponseBody
    @RequestMapping(value="/rest/user/add", method= RequestMethod.POST)
    public String addUser(
            @RequestBody Map<String, Object> object
    ){
        User usr = getUser(object.get("login").toString());
        if(usr == null){
            usr = new User();
            usr.setLogin(object.get("login").toString());
            usr.setPassword(object.get("password").toString());
            userRepository.save(usr);
        }
        return toJSON(usr).toString();
    }


    @ResponseBody
    @RequestMapping(value="/rest/user/list", method= RequestMethod.GET)
    public String addUser(){
        List<User> users = userRepository.findAll();
        JSONArray result = new JSONArray();
        for(User user : users){
            result.add(toJSON(user));
        }
        return result.toString();
    }

    private boolean isRoot(User user){
        return user.getLogin().equals("root");
    }

    private User getUser(String login){
        List<User> users = userRepository.findByLogin(login);
        if( users.size()==1 ){
            return users.get(0);
        }
        return null;
    }

    private JSONObject toJSON(User user){
        JSONObject obj = new JSONObject();
        obj.put("id", user.getId());
        obj.put("login", user.getLogin());
        obj.put("password", user.getPassword());
        return obj;
    }

}
