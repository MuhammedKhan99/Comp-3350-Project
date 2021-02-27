package com.vsn.business.managers.sessionManager;

import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vsn.objects.User;
import com.vsn.exceptions.DatabaseException;
import com.vsn.exceptions.ObjectNotFoundException;
import com.vsn.business.managers.UserManager;

import org.threeten.bp.Instant;

import java.lang.reflect.Type;
import java.security.Key;
import java.util.Hashtable;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static com.vsn.business.DependencySelector.getUserManager;

/**
 * This class mocks a server which would keep track of user logins.
 * Users are assigned tokens which are used to accomplish tasks.
 * In the instance of this project tokens would be attached to requests to
 * modify/access notes, boards, and user details.
 */
public class FakeServerSessionManager {
    private UserManager userManager;

    private static String KEY = "TEMP_ITER_1_KEY_";  // Must be 16/24/32 chars
    private Cipher cipher;
    private Key aesKey;

    private final static Hashtable<String, Instant> logouts = new Hashtable<>();

    public FakeServerSessionManager(){
        userManager = getUserManager();
        initializeCipher();
    }

    /**
     * Logs in a user. If the username and password are valid, then the user
     * is issued a token.
     * @param username The username of the user to log in.
     * @param password The password of the user to log in.
     * @return An encrypted token upon success, null upon failure.
     */
    public String login(String username, String password)
            throws ObjectNotFoundException, DatabaseException {
        if(!verifyPassword(username,password)) {
            return null;
        }

        // Generate Token
        Hashtable<String, String> tokenMap = new Hashtable<>();
        tokenMap.put("USERNAME", username);
        Instant now = Instant.now();
        tokenMap.put("ISSUED", now.toString());
        tokenMap.put("EXPIRES", now.plusSeconds(3600).toString());
        String jsonToken = (new Gson()).toJson(tokenMap);
        return encrypt(jsonToken);
    }

    /**
     * Logs out a user. This is accomplished by tracking the most recent logout
     * time of each user.
     * @param username The username of the user to logout.
     */
    public void logout(String username) throws ObjectNotFoundException, DatabaseException {
        User user = userManager.getUser(username);
        logouts.put(username, Instant.now());
    }

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
    public boolean validate(String username, String token){
        // Decrypt Token
        String jsonToken = decrypt(token);
        if(jsonToken == null){
            return false;
        }

        // Create Hashtable from Decrypted Token
        Hashtable<String,String> tokenMap = getTokenMap(jsonToken);
        if(tokenMap == null){
            return false;
        }

        // Validate Username
        if(!username.equals(tokenMap.get("USERNAME"))){
            return false;
        }

        // Validate token Issue and Expiry times
        return validateTokenTimes(username, tokenMap);
    }

    /**
     * Tests if a given password and username match
     * @param username The username to test.
     * @param password The password to test
     * @return true upon success, false otherwise.
     */
    private boolean verifyPassword(String username, String password)
            throws DatabaseException {
        User user = userManager.getUser(username);
        if(user == null){
            return false;
        }
        return user.getPassword().equals(password);
    }

    /**
     * Takes a JSON serialized token and returns a Hashtable representation
     * @param jsonToken The JSON Serialized token
     * @return A Hashtable of the Token parameters and values. Null if the
     *      string is not a serialized Hashtable.
     */
    private Hashtable<String,String> getTokenMap (String jsonToken){
        try{
            Type type = new TypeToken<Hashtable<String, String>>(){}.getType();
            Gson gson = new Gson();
            return gson.fromJson(jsonToken, type);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Validates token times. Returns true IFF
     *      - The token has not expired
     *      - The token was issued in the past
     *      - The token was not issued after the most recent logout for the user
     * @param username The Username of the user trying to do some task.
     * @param tokenMap The Hashtable of the Token parameters and values.
     * @return A boolean of if the token satisfies the above requirements.
     */
    private boolean validateTokenTimes(
            String username,
            Hashtable<String,String> tokenMap)
    {
        // Generate Instants from tokenMap
        String issuedString = tokenMap.get("ISSUED");
        String expiresString = tokenMap.get("EXPIRES");
        if(issuedString == null || expiresString == null){
            return false; // ISSUES or EXPIRES missing from tokenMap
        }
        Instant issued = Instant.parse(issuedString);
        Instant expires = Instant.parse(expiresString);
        Instant now = Instant.now();

        // Is the token expired?
        if(expires.toEpochMilli() < now.toEpochMilli()){
            return false;
        }

        // Was the token issued before the user logged out?
        Instant mostRecentLogout = logouts.get(username);
        if(mostRecentLogout != null){
            if(issued.toEpochMilli() <= mostRecentLogout.toEpochMilli()){
                return false;
            }
        }

        // Was the token issued in the future?
        return issued.toEpochMilli() <= now.toEpochMilli();
    }

    /**
     * Initialize the key and cipher used to encrypt tokens.
     */
    private void initializeCipher(){
        try {
            // Create key and cipher
            aesKey = new SecretKeySpec(KEY.getBytes(), "AES");
            cipher = Cipher.getInstance("AES");
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Encrypt a String using the initialized cipher and key.
     * @param text The String to encrypt
     * @return The encrypted String.
     */
    private String encrypt(String text){
        try {
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = cipher.doFinal(text.getBytes());
            return BaseEncoding.base64().encode(encrypted);
        }catch(Exception e){
            return null;
        }
    }

    /**
     * Decrypts a String using the initialized cipher and key.
     * @param encrypted The encrypted String
     * @return The decrypted String
     */
    private String decrypt(String encrypted){
        try{
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] bytes = BaseEncoding.base64().decode(encrypted);
            byte[] decrypted = cipher.doFinal(bytes);
            return new String(decrypted);
        }catch(Exception e){
            return null;
        }
    }
}
