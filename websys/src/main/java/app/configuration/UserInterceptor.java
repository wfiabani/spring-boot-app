package app.configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class UserInterceptor extends HandlerInterceptorAdapter {

    private static Logger logger = LoggerFactory.getLogger(HandlerInterceptorAdapter.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object controller) throws Exception {
        logger.info("UserInterceptor.preHandle called...");
        String uri = request.getRequestURI();
        if(request.getSession().getAttribute("user") != null) {
            return true;
        }
        if(
                uri.contains("/rest") ||
                uri.contains("/front-end") ||
                uri.contains("/authenticate") ||
                uri.contains("/login-form") ||
                uri.contains("/error") ||
                uri.contains("/login")
        ){
            return true;
        }
        response.sendRedirect("/security/login-form");
        return false;
    }
}