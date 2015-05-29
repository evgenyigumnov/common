package com.igumnov.common.webserver;

import javax.servlet.http.HttpServletRequest;

public interface StringInterface {
    String response(HttpServletRequest request) throws WebServerException;
}
