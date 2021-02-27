package com.vsn.exceptions;

public class ObjectNotFoundException extends DatabaseException {
    public ObjectNotFoundException(String message) {
        super(message);
    }

    public ObjectNotFoundException(){
        super();
    }
}
