package com.igumnov.common.webserver;

import com.igumnov.common.Log;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.MappedLoginService.*;
import org.eclipse.jetty.server.UserIdentity;

import javax.security.auth.Subject;
import javax.servlet.ServletRequest;

import org.eclipse.jetty.util.security.Credential;

import java.security.Principal;

public class LoginServiceHandler implements LoginService {


    private IdentityService identityService = new DefaultIdentityService();
    private LoginServiceInterface loginServiceInterface;

    public LoginServiceHandler(LoginServiceInterface loginServiceInterface) {
        this.loginServiceInterface=loginServiceInterface;
    }

    @Override
    public String getName() {
        return "Custom";
    }

    @Override
    public UserIdentity login(String userName, Object credentials, ServletRequest request) {
        Log.debug("UserIdentity login called");
        WebUser user = loginServiceInterface.getUser(userName);
        if(user == null) {
            return null;
        }
        String userPassword = user.getPassword();
        String[] roles = user.getRoles();

        Principal userPrincipal = new KnownUser(userName, Credential.getCredential(userPassword));
        Subject subject = new Subject();
        subject.getPrincipals().add(userPrincipal);
        subject.getPrivateCredentials().add(Credential.getCredential(credentials.toString()));

        if (roles != null)
            for (String role : roles)
                subject.getPrincipals().add(new RolePrincipal(role));

        subject.setReadOnly();
        UserIdentity identity = identityService.newUserIdentity(subject, userPrincipal, roles);


        UserPrincipal principal = (UserPrincipal) identity.getUserPrincipal();
        if (principal.authenticate(credentials))
            return identity;
        return null;
    }

    @Override
    public boolean validate(UserIdentity user) {
        Log.debug("validate UserIdentity called");
        WebUser u = loginServiceInterface.getUser(user.getUserPrincipal().getName());

        if (u != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public IdentityService getIdentityService() {
        return this.identityService;
    }

    @Override
    public void setIdentityService(IdentityService service) {
        this.identityService = service;
    }

    @Override
    public void logout(UserIdentity user) {

    }
}
