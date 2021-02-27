package com.vsn.exceptions;

public class ObjectAlreadyExistsException extends DatabaseException {
    public ObjectAlreadyExistsException(String message) {
        super(message);
    }

    public ObjectAlreadyExistsException(){
        super();
    }
}
