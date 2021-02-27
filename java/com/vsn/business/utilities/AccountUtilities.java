package com.vsn.business.utilities;

import com.vsn.objects.Board;
import com.vsn.objects.User;
import com.vsn.exceptions.DatabaseException;
import com.vsn.exceptions.ObjectAlreadyExistsException;
import com.vsn.exceptions.ObjectNotFoundException;
import com.vsn.business.managers.BoardManager;
import com.vsn.business.managers.NoteManager;
import com.vsn.business.managers.UserManager;

public class AccountUtilities {
    private static UserManager um = new UserManager();

    //Attempts to create an account, returns the message to be read back to the
    //user.
    public static String createAccount(String username,
                                       String password,
                                       String passwordConfirm,
                                       String email,
                                       String firstName,
                                       String lastName){
        //Check each field of the account creation screen for validity.
        boolean validUsername = verifyUsernameUniqueness(username);
        boolean validPassword = verifyPassword(password);
        boolean matchingPasswords = matchPasswords(password, passwordConfirm);
        boolean validEmail = EmailUtilities.verifyEmail(email);
        boolean noBlanks = checkBlanks(username, password, passwordConfirm, email,
                firstName, lastName);
        //If all are valid, then create the account. Note that the method in
        //the if-statement automatically reports faulty entries in the
        //account creation fields.
        if(!validUsername){
            return "That username is taken.";
        }
        else if(!validPassword){
            return "Passwords require at least six characters and at least " +
                    "one uppercase letter, one lowercase letter, and one " +
                    "number.";
        }
        else if(!matchingPasswords){
            return "Those passwords don't match.";
        }
        else if(!validEmail){
            return "That email isn't valid.";
        }
        else if(!noBlanks){
            return "All fields are required.";
        }
        else{
            try {
                um.createUser(username, password, firstName,
                        lastName, email);
                createFirstBoardAndNote(username);
                return "Account created successfully.";
            } catch (ObjectAlreadyExistsException e) {
                return "That username is taken.";
            } catch (DatabaseException e) {
                return "Something went wrong: DBE.";
            }
        }
    }

    //Add board and note to a newly created user
    private static void createFirstBoardAndNote(String username)
            throws DatabaseException, ObjectNotFoundException  {
        BoardManager bm = new BoardManager();
        NoteManager nm = new NoteManager();
        Board b = bm.create(username + "'s Board", username);
        String bAUUID = b.getUuid();
        nm.createNote("My first Note", 0, 0, username,
                bAUUID);
    }

    //Verify that the username is unique to create the account.
    private static boolean verifyUsernameUniqueness(String username) {
        try {
            um.getUser(username);
            return false;
        } catch (DatabaseException e) {
            return true;
        }
    }

    /*
     * Verify that the password matches all requirements; is at least 6
     * characters long, has at least one uppercase letter, one lowercase letter,
     * and a number.
     */
    public static boolean verifyPassword(String password) {
        boolean uppercase = false;
        boolean lowercase = false;
        boolean number = false;
        boolean length = false;
        char c;
        for (int i = 0; i < password.length(); i++) {
            c = password.charAt(i);
            if (Character.isUpperCase(c)) {
                uppercase = true;
            } else if (Character.isLowerCase(c)) {
                lowercase = true;
            } else if (Character.isDigit(c)) {
                number = true;
            }
        }
        if (password.length() >= 6) length = true;
        return uppercase && lowercase && number && length;
    }

    //Check if the confirm password field matches the password field.
    private static boolean matchPasswords(String password,
                                          String passwordConfirm) {
        return password.equals(passwordConfirm);
    }

    //Ensure none of the entries are blank.
    private static boolean checkBlanks(
            String username,
            String password,
            String passwordConfirm,
            String email,
            String firstName,
            String lastName) {
        return !username.trim().equals("") &&
                !password.equals("") &&
                !passwordConfirm.equals("") &&
                !email.trim().equals("") &&
                !firstName.trim().equals("") &&
                !lastName.trim().equals("");
    }

    public static String processAccountModification(
            String username,
            String password,
            String newPassword,
            String confirmPassword,
            String email) {
        if (email.equals("") && newPassword.equals("")
                && confirmPassword.equals("")) {
            return "No changes made.";
        }
        try {
            User u = um.getUser(username);
            if (matchPasswords(password, u.getPassword())) {
               String output = processEmailChange(u, email);
               output += processPasswordChange(
                        u, newPassword, confirmPassword);
               return output;
            } else {
                return "Your password is incorrect.";
            }
        } catch (DatabaseException e) {
            return "Something went wrong.";
        }
    }

    private static String processEmailChange(User u, String email)
            throws DatabaseException {
        if (!email.equals("")) {
            boolean validEmail = EmailUtilities.verifyEmail(email);
            if (validEmail) {
                u.setEmail(email);
                um.updateUser(u);
                return "Your email has been updated.\n";
            } else {
                return "That email is in the wrong format.\n";
            }
        }
        return "";
    }

    private static String processPasswordChange(
            User u,
            String newPassword,
            String confirmPassword
    ) throws DatabaseException {
        if (!newPassword.equals("")) {
            if (!newPassword.equals(confirmPassword)) {
                return "Your new passwords don't match.";
            } else if (!AccountUtilities.verifyPassword(newPassword)) {
                return "A password needs to be at least six " +
                       "characters long and have at least one " +
                       "uppercase letter, one lowercase letter, and " +
                       "a number.";
            } else {
                u.setPassword(newPassword);
                um.updateUser(u);
                return "Your password has been updated.";
            }
        }
        return "";
    }
}
