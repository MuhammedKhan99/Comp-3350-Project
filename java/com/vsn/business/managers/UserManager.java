package com.vsn.business.managers;

import com.vsn.objects.User;
import com.vsn.exceptions.DatabaseException;
import com.vsn.persistance.UserDatabase;
import com.vsn.business.DependencySelector;

public class UserManager {
    private UserDatabase database;

    public UserManager(){
        this.database = DependencySelector.getUserDatabase();
    }

    /**
     * Creates a user object and adds it to the userDatabase.
     * @param username: The username of the user.
     * @param password: The password of the user.
     * @param firstName: The first name of the user
     * @param lastName: The last name of the user
     * @param email: The email address of the user.
     * @return The created user upon success, null if the username is taken.
     */
    public User createUser(
            String username,
            String password,
            String firstName,
            String lastName,
            String email
    ) throws DatabaseException {
        User user = new User(
                username.toUpperCase(), password, firstName, lastName, email);
        database.createUser(user);
        return user;
    }

    /**
     * Retrieves a user from the userDatabase and returns it.
     * @param username The username of the user to retrieve
     * @return a User object if successful, null if the user doesn't exist.
     */
    public User getUser(String username) throws DatabaseException {
        return database.getUser(username.toUpperCase());
    }

    /**
     * Updates a user in the userDatabase.
     * @param user The updated User object as it is to be stored in the userDatabase
     * @return true if successful, false if the username is not in the userDatabase
     */
    public void updateUser(User user) throws DatabaseException {
        database.updateUser(user);
    }

    /**
     * Removes a user from the userDatabase.
     * @param username The username of the user to remove
     * @return The User object that was removed from the userDatabase if successful,
     *      null if the user did not exist.
     */
    public void deleteUser(String username) throws DatabaseException {
        database.deleteUser(username.toUpperCase());
    }
}
