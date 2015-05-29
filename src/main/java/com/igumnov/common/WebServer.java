package com.igumnov.common;


import com.igumnov.common.webserver.*;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.eclipse.jetty.util.security.Password;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class WebServer {


    private static TemplateEngine templateEngine;


    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";


    private static Server server;
    private static ArrayList<ContextHandler> handlers = new ArrayList<>();
    private static String templateFolder;
    private static ServerConnector connector;
    private static ServerConnector https;
    private static ConstraintSecurityHandler securityHandler;
    private static ServletContextHandler servletContext;
    private static HashLoginService loginService = new HashLoginService();

    private WebServer() {

    }


    public static void init(String hostName, int port) {

        server = new Server();

        connector = new ServerConnector(server);
        connector.setHost(hostName);
        connector.setPort(port);


    }

    public static void https(int port, String keystoreFile, String storePassword, String managerPassword) {
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(port);

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(keystoreFile);
        sslContextFactory.setKeyStorePassword(storePassword);
        sslContextFactory.setKeyManagerPassword(managerPassword);

        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());

        https = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(https_config));
        https.setPort(port);
    }

    public static void start() throws Exception {
        if (https == null) {
            server.setConnectors(new Connector[]{connector});
        } else {
            server.setConnectors(new Connector[]{connector, https});
        }
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        Handler list[] = new Handler[handlers.size()];
        list = handlers.toArray(list);
        contexts.setHandlers(list);
        server.setHandler(contexts);

        server.start();
    }

    public static void stop() throws Exception {
        server.stop();
    }

    public static void addHandler(String name, StringInterface i) {


        addServlet(new StringHandler(i), name);
    }

    public static void addRestController(String name, Class c, RestControllerInterface i) {


        addServlet(new RestControllerHandler(i, c), name);

    }

    public static void addRestController(String name, RestControllerSimpleInterface i) {


        addServlet(new RestControllerHandler(i), name);

    }


    public static void addStaticContentHandler(String name, String folder) {
        ContextHandler context = new ContextHandler();
        context.setContextPath(name);
        ResourceHandler rh = new ResourceHandler();
        rh.setDirectoriesListed(true);
        rh.setResourceBase(folder);
        context.setHandler(rh);
        handlers.add(context);
    }

    public static void addClassPathHandler(String name, String classPath) {
        ContextHandler context = new ContextHandler();
        context.setContextPath(name);

        ResourceHandler rh = new ResourceHandler() {
            @Override
            public Resource getResource(String path)
                    throws MalformedURLException {
                Resource resource = Resource.newClassPathResource(path);
                if (resource == null || !resource.exists()) {
                    resource = Resource.newClassPathResource(classPath + path);
                }
                return resource;
            }
        };

        rh.setDirectoriesListed(true);
        rh.setResourceBase("/");
        context.setHandler(rh);
        handlers.add(context);
    }


    public static void addTemplates(String folder, long cacheTTL) {
        templateFolder = folder;
        ServletContextTemplateResolver templateResolver =
                new ServletContextTemplateResolver();
        templateResolver.setTemplateMode("LEGACYHTML5");
        templateResolver.setPrefix("/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(cacheTTL);
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());

    }

    public static void addController(String name, ControllerInterface i) {

        addServlet(new ControllerHandler(templateEngine, i), name);

    }

    public static void addRestrictRule(String path, String[] roles) {
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__FORM_AUTH);

        constraint.setRoles(roles);
        constraint.setAuthenticate(true);

        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec(path);

        securityHandler.addConstraintMapping(constraintMapping);
    }

    public static void addAllowRule(String path) {
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__FORM_AUTH);

        constraint.setAuthenticate(false);

        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec(path);

        securityHandler.addConstraintMapping(constraintMapping);
    }

    public static void security(String loginPage, String loginErrorPage, String logoutUrl) {


        securityHandler = new ConstraintSecurityHandler();


        securityHandler.setLoginService(loginService);

        FormAuthenticator authenticator = new FormAuthenticator(loginPage, loginErrorPage, false);
        securityHandler.setAuthenticator(authenticator);

        servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS | ServletContextHandler.SECURITY);
        servletContext.setSecurityHandler(securityHandler);


        servletContext.addServlet(new ServletHolder(new DefaultServlet() {
            @Override
            protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                request.getSession().invalidate();
                response.sendRedirect(response.encodeRedirectURL(loginPage));
            }
        }), logoutUrl);

        handlers.add(servletContext);
    }

    static private void addServlet(HttpServlet s, String name) {
        if (servletContext == null) {
            servletContext = new ServletContextHandler();
        }
        if (templateFolder != null) {
            servletContext.setResourceBase(templateFolder);
        }
        servletContext.addServlet(new ServletHolder(s), name);

    }

    public static void addUser(String username, String password, String[] groups) {
        loginService.putUser(username, new Password(password), groups);
    }
}