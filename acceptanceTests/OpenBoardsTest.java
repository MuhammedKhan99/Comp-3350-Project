package com.vsn.acceptanceTests;

import androidx.test.rule.ActivityTestRule;

import com.vsn.CommonActions;
import com.vsn.presentation.user.AccountCreationActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.pressBack;

import org.junit.runner.RunWith;

import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class OpenBoardsTest {

    CommonActions actions;

    @Rule
    public ActivityTestRule<AccountCreationActivity> activityRule
            = new ActivityTestRule<>(AccountCreationActivity.class);

    @Before
    public void initAccountStrings() {
        actions = new CommonActions();
    }

    @Test
    public void createBoard() {
        actions.createAccount();
        actions.testAccountCreated();
        actions.loginUser();

        actions.createBoard();
        pressBack();

        actions.deleteAccount();
    }
}
