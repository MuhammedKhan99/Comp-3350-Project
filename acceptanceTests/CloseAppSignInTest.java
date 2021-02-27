package com.vsn.acceptanceTests;

import com.vsn.CommonActions;
import com.vsn.R;
import com.vsn.presentation.user.AccountCreationActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.NoActivityResumedException;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class CloseAppSignInTest {
    CommonActions actions;

    @Rule
    public ActivityTestRule<AccountCreationActivity> activityRule
            = new ActivityTestRule<>(AccountCreationActivity.class);

    @Before
    public void initAccountStrings() {
        actions = new CommonActions();
    }

    @Test
    public void closeAppSignIn() {
        actions.createAccount();
        actions.testAccountCreated();
        actions.loginUser();

        actions.logoutUser();

        closeAndReopenApp();
        onView(withId(R.id.signInChangeButton)).perform(click());

        actions.loginUser();
        actions.deleteAccount();
    }

    public void closeAndReopenApp(){
        try{
            for(int i = 0; i < 10; i++)
                pressBack();
        }catch(NoActivityResumedException e){
            activityRule.launchActivity(null);
        }
    }
}
