package com.igumnov.common.webserver;

import com.igumnov.common.JSON;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;

public class RestControllerHandler extends HttpServlet {

    private RestControllerInterface restController;
    private RestControllerSimpleInterface restControllerSimple;
    private Class postBody;

    public RestControllerHandler(RestControllerSimpleInterface i) {
        restControllerSimple = i;
    }

    public RestControllerHandler(RestControllerInterface i, Class pb) {
        restController = i;
        postBody = pb;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        Object postObject = null;
        Object responseObj = null;
        int status = HttpServletResponse.SC_OK;
        if (request.getMethod().equals("POST") || request.getMethod().equals("PUT")  ) {
            StringBuilder jb = new StringBuilder();
            String line = null;
            try {
                BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null)
                    jb.append(line);
            } catch (Exception e) {
                HashMap<String, String> err = new HashMap<>();
                err.put("message", e.getMessage());
                responseObj = err;
                status = HttpServletResponse.SC_BAD_REQUEST;
            }
            postObject = JSON.parse(jb.toString(), postBody);
        }

        try {
            if (restController != null) {
                responseObj = restController.response(request, response, postObject);
            } else {
                responseObj = restControllerSimple.response(request,response);

            }
        } catch (WebServerException e) {
            HashMap<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());

            responseObj = err;
            status = HttpServletResponse.SC_BAD_REQUEST;

        }


        String ret = JSON.toString(responseObj);

        response.setContentType("application/json; charset=utf-8");
        response.setStatus(status);

        PrintWriter out = response.getWriter();

        out.write(ret);


    }
}
