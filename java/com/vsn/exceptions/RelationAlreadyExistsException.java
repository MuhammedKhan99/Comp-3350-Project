package com.vsn.exceptions;

public class RelationAlreadyExistsException extends DatabaseException {
    public RelationAlreadyExistsException(String message) {
        super(message);
    }

    public RelationAlreadyExistsException(){
        super();
    }
}
