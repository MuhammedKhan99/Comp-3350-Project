package com.vsn.objects;

import java.util.HashMap;
import java.util.HashSet;

import org.threeten.bp.Instant;

public class User{
    private String username;
    private String password;
    private HashMap metadata;
    private String firstName;
    private String lastName;
    private String email;
    private String token;
    private HashSet<String> boards;

    public User(
            String username,
            String password,
            String firstName,
            String lastName,
            String email
    ) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.boards = new HashSet<>();
        this.token = "";

        metadata = new HashMap();
        String creationTime = Instant.now().toString();

        metadata.put("creationTime", creationTime);
    }
    public User(
            String username,
            String password,
            String firstName,
            String lastName,
            String email,
            HashMap<String, String> metadata,
            String token,
            HashSet<String> boards
    ) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.boards = boards;
        this.token = token;
        this.metadata = metadata;
    }


    // Getters and Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername(){
        return username;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getPassword(){
        return password;
    }

    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    public String getFirstName(){
        return firstName;
    }

    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    public String getLastName(){
        return lastName;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail(){
        return email;
    }

    public HashMap getMetadata() {
        return (HashMap) metadata.clone();
    }

    public void setToken(String token){
        this.token = token;
    }

    public String getToken(){
        return token;
    }

    public void addBoard(String boardUuid){
        boards.add(boardUuid);
    }

    public void removeBoard(String boardUuid){
        boards.remove(boardUuid);
    }

    public HashSet<String> getBoards(){
        return (HashSet<String>) boards.clone();
    }
}
