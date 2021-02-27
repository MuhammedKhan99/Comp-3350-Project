package com.vsn.exceptions;

public class DatabaseException extends Exception {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(){
        super();
    }
}
