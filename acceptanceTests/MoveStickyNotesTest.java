package com.vsn.acceptanceTests;

import androidx.test.rule.ActivityTestRule;

import com.vsn.CommonActions;
import com.vsn.R;
import com.vsn.presentation.user.AccountCreationActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class MoveStickyNotesTest {

    CommonActions actions;

    @Rule
    public ActivityTestRule<AccountCreationActivity> activityRule
            = new ActivityTestRule<>(AccountCreationActivity.class);

    @Before
    public void initAccountStrings() {
        actions = new CommonActions();
    }

    @Test
    public void moveStickyNotes() {
        actions.createAccount();
        actions.testAccountCreated();
        actions.loginUser();

        actions.createBoard();
        actions.createNote("blue");
        actions.createNote("red");
        onView(withId(R.id.image_sample)).perform(swipeLeft());

        actions.moveAroundCanvas();
        pressBack();

        actions.deleteAccount();
    }
}
