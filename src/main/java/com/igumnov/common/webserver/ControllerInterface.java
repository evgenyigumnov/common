package com.igumnov.common.webserver;


import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface ControllerInterface {

    public String process(HttpServletRequest request, Map<String, Object> model) throws WebServerException;

}
