package com.vsn.acceptanceTests;

import com.vsn.CommonActions;
import com.vsn.R;
import com.vsn.presentation.user.AccountCreationActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class LogOutSwitchAccountTest {
    CommonActions actions;
    CommonActions actions2;

    @Rule
    public ActivityTestRule<AccountCreationActivity> activityRule
            = new ActivityTestRule<>(AccountCreationActivity.class);

    @Before
    public void initAccountStrings() {
        actions = new CommonActions();
        actions2 = new CommonActions();
    }

    @Test
    public void performFullLifeCycle() {
        actions.createAccount();
        actions.testAccountCreated();
        pressBack();

        actions2.createAccount();
        actions2.testAccountCreated();

        actions.loginUser();
        actions.logoutUser();

        actions2.loginUser();
        actions2.deleteAccount();

        onView(withId(R.id.signInChangeButton)).perform(click());
        actions.loginUser();
        actions.deleteAccount();
    }
}
