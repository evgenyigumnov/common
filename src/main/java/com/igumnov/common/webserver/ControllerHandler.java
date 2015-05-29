package com.igumnov.common.webserver;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class ControllerHandler extends HttpServlet {

    private TemplateEngine templateEngine;
    private ControllerInterface controller;

    public ControllerHandler(TemplateEngine t, ControllerInterface c) {
        templateEngine = t;
        controller = c;
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> model = new HashMap<>();
        String templateName = null;
        int status = HttpServletResponse.SC_OK;

        try {
            templateName = controller.process(request, model);
        } catch (WebServerException e) {

            status = HttpServletResponse.SC_BAD_REQUEST;
        }

        IContext context = new ControllerContext(model, request.getServletContext());
        String ret = templateEngine.process(templateName, context);

        response.setContentType("text/html; charset=utf-8");
        response.setStatus(status);

        PrintWriter out = response.getWriter();

        out.write(ret);


    }
}
