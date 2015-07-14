# common

Why?

A lot of questions on Stack Overflow have answer on 3-5 lines code. This library collect
typical cases which developers copy-past in their projects. All functions in this library covered
by JUnit test. Simply use this small library to have clean and more stable code in your project.
This library could work like Small Enterprise Server with Dependency Injection, Embedded WebServer, ORM Frameworks.


Common Java library functions:

* Sleep/Delay/Pause
* Timer for Benchmark
* Random range generator
* File operations
* Tasks/Threads/Scheduler
* Reflection
* JSON
* URL
* Logging
* Strings
* Crypt/Decrypt operations
* Memory Cache (Key-Value and tags)
* WebServer (Static Content/CGI/Rest support)
* MVC Framework with Template Engine
* Dependency Injection Framework
* ORM Framework

Dependencies from other projects:
* Jetty - WebServer
* Jackson - JSON parser
* Thymeleaf - HTML template engine
* NekoHTML - HTML parser

Usage:

    import com.igumnov.common.*;

Maven:

    <dependency>
      <groupId>com.igumnov</groupId>
      <artifactId>common</artifactId>
      <version>9.1</version>
    </dependency>


Example project with using this library

Githab: https://github.com/evgenyigumnov/example
Online version: http://java.igumnov.com:8181/
login: demo password: demo



Memory Cache (Key-Value and tags)

    Cache.init(maxKeySize, defaultTTLInSeconds);
    Cache.put("keyStr", valueObject);
    Cache.put("keyStr", valueObject, TTLInSeconds, "tag1"...);
    Cache.put("keyStr", valueObject, "tag1"...);
    Cache.remove("keyStr");
    Cache.removeByTag("tag1");
    Object valueObject = Cache.get("keyStr");


Cache with Redis

    Cache.initWithRedis(defaultTTLInSeconds, redisHost, redisPort);

Embedded WebServer

    WebServer.setPoolSize(minPoolSize,maxPoolSize);
    WebServer.init("localhost", 8080); // Init HTTP
    WebServer.https(8443, "src/test/resources/key.jks", "storepwd", "keypwd"); // Init HTTPS

Security in memory

    WebServer.security("/loginPage", "/loginPageError", "/logoutLink");
    WebServer.addUser("username", "password", new String[]{"user_role","admin_role"}); // Add users in memory
    WebServer.addAllowRule("/*");
    WebServer.addRestrictRule("/script", new String[]{"user"});
    WebServer.addHandler("/loginPage", (request, response) -> {
        return "<form method='POST' action='/j_security_check'>"
        + "<input type='text' name='j_username'/>"
        + "<input type='password' name='j_password'/>"
        + "<input type='submit' value='Login'/></form>";
    });

Security user list from custom source

    WebServer.setLoginService((userName) -> {
        if(userName.equals("admin1")) {
            return new WebUser("admin1", "CRYPT:adpexzg3FUZAk", new String[]{"user"});
        }
        return null;
    });



CGI/Static

    WebServer.addStaticContentHandler("/static", "/path_to_static_content");
    WebServer.addHandler("/script", (request, response) -> {
        return "Bla-Bla script result";
    });


Rest controller

    WebServer.addRestController("/rest/get", (request, response) -> {
        if (request.getMethod().equals(WebServer.METHOD_GET)) {
                ObjectDTO objectDTO = new ObjectDTO();
                objectDTO.setVal(1);
                ...
                return objectDTO;
        }
        throw new WebServerException("Unsupported method");
    });


    WebServer.addRestController("/rest/put", ObjectDTO.class, (request, response, putOrPostObjectDTO) -> {
        if (request.getMethod().equals(WebServer.METHOD_PUT)) {
                putOrPostObjectDTO.getAttr();
                ...
                return resultObjectDTO;
        }
        throw new WebServerException("Unsupported method");
    });



Model-View-Controller with TemplateEngine Framework

    WebServer.addTemplates("/path_to_templates");
    WebServer.addController("/", (request, response, model) -> {
        model.put("varName", new Integer("123"));
        return "home";
    });


    home.html in "/path_to_templates" folder

    <html><body><span th:text="${varName}"></span></body><html>

WebServer start/stop

    WebServer.start();
    WebServer.stop();


Dependency Injection Framework

Declaration instance

    @Named("nameInstance")
    public class SomeClass {
        ...
    }


Dependency injection in field

    public class OtherClass {

        @Inject("nameInstance")
        private SomeClass someClass;

        ...

    }

Get instance from context

    SomeClass objInstance = (SomeClass) Dependency.find("newInstance");

Initialization of the Dependency Injection Framework

    // scans classes of the specified package recursively for Annotations:
    Dependency.scan("com.yourpackage.app");

    // performing the injection of dependencies of a class (transitively):
    OtherClass otherClassInstance = new OtherClass();
    Dependency.inject(otherClassInstance);

    // bind instance by name to context
    Dependency.bind("instanceName", otherClassInstance);

    // create and put instance to context
    Dependency.createInstance("otherInstanse", SomeClass.class);


ORM Framework


    ORM.connectionPool("DriverClassName", "databaseUrl", "name", "password", minPoolSize, maxPoolSize);

    File.appendLine("CREATE TABLE ObjectDTO (id BIGINT AUTO_INCREMENT PRIMARY KEY)", "tmp/sql_folder/1.sql");
    File.appendLine("ALTER TABLE ObjectDTO ADD name VARCHAR(255)", "tmp/sql_folder/1.sql");
    ORM.applyDDL("tmp/sql_folder"); // In table DDLHISTORY store information about already applied .sql files

    File.appendLine("ALTER TABLE ObjectDTO ADD salary INT", "tmp/sql_folder/2.sql");
    ORM.applyDDL("tmp/sql_folder"); // In table DDLHISTORY store information about already applied .sql files

    public class ObjectDTO {
        @Id(autoIncremental=true)
        private Long id;
        private String name;
        private Integer salary;
        ....
    }

Autocommit mode

    ObjectDTO obj = new ObjectDTO();
    obj.setName("a");
    obj.setSalary(1);
    obj = (ObjectDTO) ORM.insert(obj);
    obj = (ObjectDTO) ORM.findOne(ObjectDTO.class, new Long(1));
    ArrayList<Object> = ORM.findBy("id > ? and salary = ? order by id limit 1", ObjectDTO.class, new Long(0), new Integer(1))
    obj.setName("b");
    obj = (ObjectDTO) ORM.update(obj);
    ORM.delete(obj);

Transactional mode

    Transaction tx = ORM.beginTransaction();
    ObjectDTO obj = new ObjectDTO();
    obj.setName("a");
    obj.setSalary(1);
    obj = (ObjectDTO) tx.insert(obj);
    obj = (ObjectDTO) tx.findOne(ObjectDTO.class, new Long(1));
    ArrayList<Object> = tx.findBy("id > ? and salary = ? order by id limit 1", ObjectDTO.class, new Long(0), new Integer(1))
    obj.setName("b");
    obj = (ObjectDTO) tx.update(obj);
    ORM.delete(obj);
    tx.commit();


Get connection from pool if you need send SQL directly

    DataSource ds = (DataSource) Dependency.find("dataSource");
    Connection c=null;
    try {
        c = ds.getConnection();
        ...
    } finally {
         try {
            if (con != null) con.close();
         } catch (Exception e) {}
    }



Sleep

    Benchmark.timerStart();
    Time.sleepInSeconds(1.5);
    System.out.println("Sleep time: " + Benchmark.timerStop() + "ms");

Random generator

    int rndInt = Number.randomIntByRange(-10, 300);
    long rndLong = Number.randomLongByRange(-5L, -2L);
    double rndDouble = Number.randomDoubleByRange(100.5, 2222.343);

File/Folder

    File.writeString("Some text", "dir/somefile.txt");
    File.appendLine("Other line text", "dir/somefile.txt");
    File.readLines("dir/somefile.txt").forEach((line) -> {
        System.out.println(line);
    });

    String fileContent = File.readAllToString("dir/somefile.txt");
    Folder.deleteWithContent"tmp/someDirForDeletion");
    Folder.copyWithContent("/src.dir","/target.dir");

Tasks

    Future<?> task = Task.startProcedure(() -> {
        // Long time procedure code there
    });
    // Do something
    if(!task.isDone()) {
       task.get(); // Wait for done
    }

    Task.setThreadPoolSize(2); // Default value: 10
    Future<Object> taskResult = Task.startFunction(() -> {
        // Long time function code there
        return resultObject;
    });
    // Do something
    Object result = taskResult.get();

    Task.schedule(() -> {
        // Procedure code there
    }, repeatAfterSeconds);


Benchmark

    Benchmark.timerStart("loop1");
    for(int i = 0; i<10000 ; ++i) {
        // do something
    }
    System.out.println("loop1 time: " + Benchmark.timerStop("loop1") + "ms");

    Benchmark.timerStart("pausedTimer");
    // do something
    Benchmark.timerPause("pausedTimer");
    // do something
    Benchmark.timerResume("pausedTimer");
    // do something
    System.out.println("pausedTimer time: " + Benchmark.timerStop("pausedTimer") + "ms");

    for(int i = 0; i<10000 ; ++i) {
        Benchmark.timerBegin("loop2");
        // do something
        Benchmark.timerEnd("loop2");
    }
    System.out.println("loop2 total time: " + Benchmark.timerGetTotalTime("loop2") + "ms");
    System.out.println("loop2 repeat count: " + Benchmark.timerGetRepeatCount("loop2"));
    System.out.println("loop2 average time: " + Benchmark.timerGetAverageTime("loop2") + "ms");

JSON

    SomePOJO obj = (SomePOJO) JSON.parse(jsonString, SomePOJO.class));
    String json = JSON.toString(obj);

URL

    String responseBody = URL.getAllToString("http://localhost:8181/script");
    // Extended version: getAllToString(String url, String method, Map<String, Object> postParams, String postBody)


Reflection

    ArrayList<String> names = Reflection.getClassNamesFromPackage("com.your_package_name");
    Reflection.setField(object, "fieldName", value);
    Object value = Reflection.getFieldValue(object, "fieldName");


Log

    Log.setLogLevel(Log.INFO);
    Log.disableStdout(); // by default enabled
    Log.file("log_filename.log");
    Log.info("Info message");

Strings

    String s = "some line";
    Strings.stream(s).forEach((c) -> {
        // do something by each char in string
    });

Crypt/Decrypt operations


    String cryptedString = Cyphers.encryptAES(srcText, password);
    String uncryptedString = Cyphers.decryptAES(cryptedString,password);
    String md5Digest = Cyphers.md5(srcText);


If you do not want use WebServer

      <exclusions>
                <exclusion>
                    <groupId>org.eclipse.jetty</groupId>
                    <artifactId>jetty-server</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.eclipse.jetty</groupId>
                    <artifactId>jetty-security</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.eclipse.jetty</groupId>
                    <artifactId>jetty-servlet</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.thymeleaf</groupId>
                    <artifactId>thymeleaf</artifactId>
                </exclusion>
                <exclusion>
                	<groupId>nz.net.ultraq.thymeleaf</groupId>
	                <artifactId>thymeleaf-layout-dialect</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-simple</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>net.sourceforge.nekohtml</groupId>
                    <artifactId>nekohtml</artifactId>
                </exclusion>
      </exclusions>

If you do not want use JSON

      <exclusions>
                <exclusion>
                    <groupId>com.fasterxml.jackson.core</groupId>
                    <artifactId>jackson-databind</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>com.fasterxml.jackson.core</groupId>
                    <artifactId>jackson-core</artifactId>
                </exclusion>
      </exclusions>

If you do not want use ORM

      <exclusions>
                <exclusion>
                    <groupId>org.apache.commons</groupId>
                    <artifactId>commons-dbcp2</artifactId>
                </exclusion>
      </exclusions>

If you dont want use Redis client for cache

      <exclusions>
                <exclusion>
            	    <groupId>redis.clients</groupId>
        	        <artifactId>jedis</artifactId>
        	    </exclusion>
      </dependency>