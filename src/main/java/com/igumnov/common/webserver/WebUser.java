package com.igumnov.common.webserver;

public class WebUser {
    private String userName;
    private String password;
    private String[] roles;


    public WebUser(String userName, String password, String[] roles) {
        this.userName = userName;
        this.password = password;
        this.roles = roles;
    }

    public String getUserName() {
        return userName;
    }

    public WebUser setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public WebUser setPassword(String password) {
        this.password = password;
        return this;
    }

    public String[] getRoles() {
        return roles;
    }

    public WebUser setRoles(String[] roles) {
        this.roles = roles;
        return this;
    }
}
