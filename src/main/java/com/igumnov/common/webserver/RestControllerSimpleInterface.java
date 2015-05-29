package com.igumnov.common.webserver;

import javax.servlet.http.HttpServletRequest;

public interface RestControllerSimpleInterface {

    Object response(HttpServletRequest request) throws WebServerException;


}

