package com.vsn.acceptanceTests;

import com.vsn.CommonActions;
import com.vsn.presentation.user.AccountCreationActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class DeleteAccountTest {
    CommonActions actions;

    @Rule
    public ActivityTestRule<AccountCreationActivity> activityRule
            = new ActivityTestRule<>(AccountCreationActivity.class);

    @Before
    public void initAccountStrings() {
        actions = new CommonActions();
    }

    @Test
    public void createAccountDeleteAccount() {
        actions.createAccount();
        actions.testAccountCreated();
        actions.loginUser();

        actions.deleteAccount();
    }
}
