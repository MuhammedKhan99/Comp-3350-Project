package com.vsn.acceptanceTests;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.vsn.CommonActions;
import com.vsn.presentation.user.AccountCreationActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.pressBack;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddStickyNoteToBoardTest {

    CommonActions actions;

    @Rule
    public ActivityTestRule<AccountCreationActivity> activityRule
            = new ActivityTestRule<>(AccountCreationActivity.class);

    @Before
    public void initAccountStrings() {
        actions = new CommonActions();
    }

    @Test
    public void addStickyNotesTest() {
        actions.createAccount();
        actions.testAccountCreated();
        actions.loginUser();

        actions.createBoard();
        actions.createNote("blue");
        pressBack();

        actions.deleteAccount();
    }

}
