package com.igumnov.common.webserver;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Properties;

import org.thymeleaf.Arguments;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.messageresolver.MessageResolution;


public class MessageResolver implements IMessageResolver {

    private Properties properties;

    public MessageResolver(String fileName) throws IOException {
        super();

        properties = new Properties();
        FileInputStream input = new FileInputStream(fileName);
        InputStreamReader isr = new InputStreamReader(input, Charset.forName("UTF-8"));
        properties.load(isr);
    }


    public String getName() {
        return "WebServer Message Resolver";
    }

    public Integer getOrder() {
        return Integer.valueOf(1);
    }



    public MessageResolution resolveMessage( final Arguments arguments, final String key, final Object[] messageParameters) {

        final String messageValue = this.properties.getProperty(key);
        if (messageValue == null) {
            return null;
        }
        if (messageParameters == null || messageParameters.length == 0) {
            return new MessageResolution(messageValue);
        }

        final MessageFormat messageFormat = new MessageFormat(messageValue);
        return new MessageResolution(messageFormat.format(messageParameters));

    }


    public void initialize() {
        // Nothing to initialize
    }




}