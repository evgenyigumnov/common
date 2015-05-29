package com.igumnov.common.webserver;

import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.VariablesMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Map;

public class ControllerContext implements IWebContext {

    private Map<String, Object> model;
    private ServletContext servletContext;
    public ControllerContext(Map<String, Object> m, ServletContext sc) {

        model = m;
        servletContext =sc;
    }
    @Override
    public VariablesMap<String, Object> getVariables() {
        VariablesMap<String, Object> ret = new VariablesMap<>();
        ret.putAll(model);
        return ret;
    }

    @Override
    public Locale getLocale() {
        return Locale.ENGLISH;
    }

    @Override
    public void addContextExecutionInfo(String templateName) {

    }

    @Override
    public HttpServletRequest getHttpServletRequest() {
        return null;
    }

    @Override
    public HttpServletResponse getHttpServletResponse() {
        return null;
    }

    @Override
    public HttpSession getHttpSession() {
        return null;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public VariablesMap<String, String[]> getRequestParameters() {
        return null;
    }

    @Override
    public VariablesMap<String, Object> getRequestAttributes() {
        return null;
    }

    @Override
    public VariablesMap<String, Object> getSessionAttributes() {
        return null;
    }

    @Override
    public VariablesMap<String, Object> getApplicationAttributes() {
        return null;
    }
}
