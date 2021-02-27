package com.vsn.persistance;

import com.vsn.objects.User;
import com.vsn.exceptions.DatabaseException;

import java.util.Collection;

public interface UserDatabase {
    public void createUser(User user) throws DatabaseException;
    public User getUser(String username) throws DatabaseException;
    public void updateUser(User user) throws DatabaseException;
    public void deleteUser(String username) throws DatabaseException;
    public Collection<User> getAllUsers() throws DatabaseException;
    public void clearUsers() throws DatabaseException;
}
