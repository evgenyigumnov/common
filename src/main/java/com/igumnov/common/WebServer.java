package com.igumnov.common;


import com.igumnov.common.reflection.ReflectionException;
import com.igumnov.common.webserver.*;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.JDBCSessionIdManager;
import org.eclipse.jetty.server.session.JDBCSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

public class WebServer {


    private static SessionHandler sessionHandler;
    private static HashMap<String, TemplateEngine> templateEngines = new HashMap<>();
    private static LocaleInterceptorInterface localeInterceptor;
    private static HashMap<String, MessageResolver> resolvers = new HashMap<>();

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
    private static LoginService loginService = new HashLoginService();
    private static QueuedThreadPool threadPool = null;

    private WebServer() {

    }


    public static void setPoolSize(int min, int max) {
        threadPool = new QueuedThreadPool(max, min);
    }

    public static void init(String hostName, int port) {

        if (server == null) {
            if (threadPool != null) {
                server = new Server(threadPool);
            } else {
                server = new Server();
            }
        }

        connector = new ServerConnector(server);
        connector.setHost(hostName);
        connector.setPort(port);


    }

    public static void https(String hostName, int port, String keystoreFile, String storePassword, String managerPassword) {
        if (server == null) {
            if (threadPool != null) {
                server = new Server(threadPool);
            } else {
                server = new Server();
            }
        }

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
        https.setHost(hostName);
    }

    public static void start() throws Exception {
        if (https == null) {
            Log.debug("https not enabled");
            server.setConnectors(new Connector[]{connector});
        } else {
            Log.debug("https enabled");
            if (connector == null) {
                server.setConnectors(new Connector[]{https});
            } else {
                server.setConnectors(new Connector[]{connector, https});

            }
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

    public static void addBinaryHandler(String name, BinaryInterface i) {


        addServlet(new BinaryHandler(i), name);
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


    public static void addTemplates(String folder, long cacheTTL, String localeFile) throws IOException {

        HashMap<String, String> langs = new HashMap<>();
        langs.put("en", localeFile);
        addTemplates(folder, cacheTTL, langs, (request, response) -> {
            return "en";
        });
    }


    public static void addTemplates(String folder, long cacheTTL, HashMap<String, String> langs, LocaleInterceptorInterface interceptor) throws IOException {
        templateFolder = folder;
        localeInterceptor = interceptor;

        for (String lang : langs.keySet()) {
            ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
            templateResolver.setTemplateMode("LEGACYHTML5");
            templateResolver.setPrefix("/");
            templateResolver.setSuffix(".html");
            templateResolver.setCacheTTLMs(cacheTTL);
            templateResolver.setCharacterEncoding("UTF-8");
            TemplateEngine templateEngine = new TemplateEngine();
            templateEngine.setTemplateResolver(templateResolver);
            String localeFile = langs.get(lang);
            if (localeFile != null) {
                MessageResolver resolver = new MessageResolver(localeFile);
                templateEngine.addMessageResolver(resolver);
                resolvers.put(lang, resolver);
            }
            templateEngine.addDialect(new LayoutDialect());
            templateEngines.put(lang, templateEngine);
        }

    }

    public static void addController(String name, ControllerInterface i) {

        addServlet(new ControllerHandler(templateEngines, localeInterceptor, i), name);

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

    public static void setLoginService(LoginServiceInterface loginServiceInterface) {
        loginService = new LoginServiceHandler(loginServiceInterface);
    }

    public static void security(String loginPage, String loginErrorPage, String logoutUrl) {


        securityHandler = new ConstraintSecurityHandler();


        securityHandler.setLoginService(loginService);

        FormAuthenticator authenticator = new FormAuthenticator(loginPage, loginErrorPage, false);
        securityHandler.setAuthenticator(authenticator);

        servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS | ServletContextHandler.SECURITY);
        servletContext.setSecurityHandler(securityHandler);
        if (sessionHandler != null) {
            servletContext.setSessionHandler(sessionHandler);
        }

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


    public static void addUserWithEncryptedPassword(String username, String password, String[] groups) throws ReflectionException, IllegalAccessException {
        ((HashLoginService) loginService).putUser(username, Credential.Crypt.getCredential(password), groups);
    }

    public static void addUser(String username, String password, String[] groups) {
        ((HashLoginService) loginService).putUser(username, Credential.Crypt.getCredential(Credential.Crypt.crypt(username, password)), groups);
    }

    public static Server getServerJettyObject() {
        return server;
    }

    public static void addJDBCSessionManager(String jdbcDriver, String jdbcUrl, int scavengeInterval, String workerName) {

        // Specify the Session ID Manager
        JDBCSessionIdManager jdbcSessionIdManager = new JDBCSessionIdManager(server);
        jdbcSessionIdManager.setWorkerName(workerName);
        jdbcSessionIdManager.setDriverInfo(jdbcDriver, jdbcUrl);
        jdbcSessionIdManager.setScavengeInterval(scavengeInterval);
        server.setSessionIdManager(jdbcSessionIdManager);

        // Sessions are bound to a context.
        //ContextHandler contextHandler = new ContextHandler("/");
        //server.setHandler(contextHandler);

        // Create the SessionHandler (wrapper) to handle the sessions
        JDBCSessionManager jdbcSessionManager = new JDBCSessionManager();
        jdbcSessionManager.setSessionIdManager(server.getSessionIdManager());
        sessionHandler = new SessionHandler(jdbcSessionManager);
        ///contextHandler.setHandler(sessionHandler);

        //handlers.add(contextHandler);
    }

    public static String getMessage(HttpServletRequest request, HttpServletResponse response, String messageKey, Object[] messageParameters) throws WebServerException {
        MessageResolver resolver = resolvers.get(localeInterceptor.locale(request, response));
        return resolver.resolveMessage(null, messageKey, messageParameters).getResolvedMessage();
    }

}