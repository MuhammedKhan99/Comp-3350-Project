package com.vsn.business.managers.sessionManager;

import com.vsn.exceptions.DatabaseException;


public class SessionManager implements com.vsn.business.managers.SessionManager {
    FakeServerSessionManager connection; // Fake connection to a server

    public SessionManager(){
        connection = new FakeServerSessionManager();
    }

    @Override
    public String login(String username, String password){
        try {
            return connection.login(username, password);
        } catch (DatabaseException e) {
            return null; // Wrong Username
        }
    }

    @Override
    public void logout(String username) throws DatabaseException {
        connection.logout(username);
    }

    @Override
    public boolean validate(String username, String token) {
        return connection.validate(username, token);
    }
}
