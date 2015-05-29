package com.igumnov.common.webserver;

import javax.servlet.http.HttpServletRequest;

public interface RestControllerInterface {

    Object response(HttpServletRequest request, Object postObject) throws WebServerException;
}
