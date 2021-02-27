package com.vsn.business.managers;

import com.vsn.exceptions.DatabaseException;

public interface SessionManager {
    /**
     * Logs in a user. If the username and password are valid, then the user
     * is issued a token.
     * @param username The username of the user to log in.
     * @param password The password of the user to log in.
     * @return An encrypted token upon success, null upon failure.
     */
    String login(String username, String password);

    /**
     * Logs out a user. This is accomplished by tracking the most recent logout
     * time of each user.
     * @param username The username of the user to logout.
     * @throws DatabaseException If the logout was not successful
     */
    void logout(String username) throws DatabaseException;

    /**
     * Validates whether an issued token is valid or not. Tokens are valid IFF
     *      - The token can be decrypted using the private key
     *      - The username on the token matches the username argument
     *      - The token has not expired
     *      - The token was issued in the past
     *      - The token was not issued after the most recent logout for the user
     * @param username The username of the user trying to do some task.
     * @param token The token to validate
     * @return true if the token was valid, false otherwise.
     */
    boolean validate(String username, String token);
}
