package com.vsn.business.utilities;

public abstract class EmailUtilities {

    /*
     * Checks if the email inputted into the email text-field at least has the
     * proper format of an email; to further validate if the email itself is a
     * usable email would require external libraries, could be a future
     * iteration if we want to be certain our user's emails are legitimate and
     * usable.
     */
    public static boolean verifyEmail(String email) {
        //Uses a regex to break the email into its parts for verification.
        String regex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+" +
                "/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21" +
                "\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x" +
                "7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9]" +
                "(?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[" +
                "0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[" +
                "a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21" +
                "-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+" +
                ")\\])";
        //The above regex was taken from emailregex.com

        String[] splitEmail = email.toLowerCase().split(regex);
        // 0 len == success
        return (splitEmail.length == 0);
    }
}
