package com.vsn;

import org.junit.Assert;

import java.util.UUID;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.doubleClick;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class CommonActions {
    private String username;
    private String password;
    private String email;
    private String fName;
    private String lName;
    private String boardTitle;

    public CommonActions(){
        // Initialize Values
        username = "User-"+UUID.randomUUID().toString().substring(0,8);
        password = "Password123";
        email = "not_a_robot@email.com";
        fName = "Real Human First Name";
        lName = "Real Human Last Name";
        boardTitle = "Board-"+UUID.randomUUID().toString().substring(0,13);
    }

    public void createAccount(){
        // Enter Account Details, then create the account.
        onView(withId(R.id.usernameField))
                .perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.passwordText))
                .perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.passwordConfirmField))
                .perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.emailField))
                .perform(replaceText(email), closeSoftKeyboard());
        onView(withId(R.id.firstNameField))
                .perform(replaceText(fName), closeSoftKeyboard());
        onView(withId(R.id.lastNameField))
                .perform(typeText(lName), closeSoftKeyboard());
        onView(withId(R.id.createAccountButton)).perform(click());
    }

    public void testAccountCreated(){
        // Successfully Created?
        onView(withId(R.id.accountCreationError))
                .check(matches(withText("Account created successfully.")));
        onView(withId(R.id.signInChangeButton)).perform(click());
    }

    public void loginUser(){
        // Login
        onView(withId(R.id.signInUsernameField))
                .perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.signInPasswordField))
                .perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.signInChangeButton)).perform(click());
    }

    public void logoutUser(){
        // Login
        onView(withId(R.id.signOutButton)).perform(click());
    }

    public void createBoard(){
        // Create Board
        onView(withId(R.id.addBoardButton)).perform(click());

        onView(withText("<new board>")).perform(longClick());
        onView(withId(R.id.colorPickerView_Board)).perform(swipeLeft());
        onView(withId(R.id.boardTitle))
                .perform(replaceText(boardTitle), closeSoftKeyboard());
        onView(withId(R.id.AcceptChangesbutton_BoardEdit)).perform(click());
        onView(withText(boardTitle)).perform(click());
    }

    public void createNotes(){
        // Creates some notes and moves around
        createNote("blue");
        moveAroundCanvas();
        createNote("red");
        moveAroundCanvas();
        createNote("");
        moveAroundCanvas();
        pressBack();
    }

    public void createNote(String colour){
        //Create Note
        onView(withId(R.id.button_AddNewNote)).perform(click());
        switch(colour){
            case "red":
                onView(withId(R.id.colorPickerView)).perform(swipeRight());
                break;
            case "blue":
                onView(withId(R.id.colorPickerView)).perform(swipeDown());
                break;
            case "green":
                onView(withId(R.id.colorPickerView)).perform(swipeLeft());
                break;
            case "orange":
                onView(withId(R.id.colorPickerView)).perform(swipeUp());
                break;
        }
        onView(withId(R.id.textView))
                .perform(typeText(generateNoteText()), closeSoftKeyboard());
        onView(withId(R.id.submitButton)).perform(click());
    }

    public void editNote(){
        onView(withId(R.id.image_sample)).perform(doubleClick());
        onView(withId(R.id.colorPickerView)).perform(swipeUp());
        onView(withId(R.id.textView))
                .perform(replaceText(""),
                        closeSoftKeyboard());
        onView(withId(R.id.textView))
                .perform(typeText(
                        "New " + generateNoteText()),
                        closeSoftKeyboard());
        onView(withId(R.id.submitButton)).perform(click());
    }


    public String generateNoteText(){
        StringBuilder builder = new StringBuilder();
        builder.append("Fake Note text: ");
        for(int i = 0; i < 2; i++){
            builder.append(UUID.randomUUID().toString());
        }
        return builder.toString();
    }

    public void moveAroundCanvas(){
        //Swipes around the canvas
        onView(withId(R.id.image_sample)).perform(swipeRight());
        onView(withId(R.id.image_sample)).perform(swipeUp());
        onView(withId(R.id.image_sample)).perform(swipeLeft());
        onView(withId(R.id.image_sample)).perform(swipeLeft());
        onView(withId(R.id.image_sample)).perform(swipeDown());
        onView(withId(R.id.image_sample)).perform(swipeDown());
        onView(withId(R.id.image_sample)).perform(swipeUp());
        onView(withId(R.id.image_sample)).perform(swipeRight());
    }

    public void enterAndMoveAroundBoard(){
        onView(withText(boardTitle)).perform(click());
        moveAroundCanvas();
        pressBack();
    }

    public void clearBoard(){
        onView(withText(boardTitle)).perform(longClick());
        onView(withId(R.id.boardDeleteNotes)).perform(click());
    }

    public void changeEmailPassword(){
        onView(withId(R.id.settingsButton)).perform(click());
        onView(withId(R.id.passwordField))
                .perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.emailField))
                .perform(typeText(
                        "this.is.a@real-human.me"),
                        closeSoftKeyboard());
        password = "NewPassword123";
        onView(withId(R.id.newPasswordField))
                .perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.passwordConfirmField))
                .perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.saveButton)).perform(click());
        pressBack();
        pressBack();
    }

    public void deleteAccount(){
        onView(withId(R.id.settingsButton)).perform(click());
        onView(withId(R.id.passwordField))
                .perform(replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.passwordField))
                .perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.deleteAccountButton)).perform(click());
        onView(withText("Are you sure? This is irreversible."))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText("Yes")).perform(click());
        pressBack();
    }


}
