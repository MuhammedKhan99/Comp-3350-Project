package com.vsn.presentation.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.app.ProgressDialog;
import com.vsn.R;
import com.vsn.exceptions.DatabaseException;
import com.vsn.exceptions.ObjectNotFoundException;
import com.vsn.presentation.board.BoardListActivity;
import com.vsn.presentation.PopUp;
import com.vsn.business.managers.UserManager;
import com.vsn.business.managers.sessionManager.SessionManager;
import com.vsn.business.VSNState;

import static com.vsn.business.DependencySelector.getSessionManager;
import static com.vsn.business.DependencySelector.getUserManager;

public class SignInActivity extends AppCompatActivity {
    String userSignedIn;
    String userToken;
    UserManager um;
    SessionManager sm;

    //Handler method when the user attempts to sign in.
    public void signIn(View view){
        //Obtain all fields and their contents.
        EditText usernameBox = findViewById(R.id.signInUsernameField);
        String username = usernameBox.getText().toString();
        EditText passwordBox = findViewById(R.id.signInPasswordField);
        String password = passwordBox.getText().toString();
        //First, check to see if a user is already signed in.
        if(userToken!=null && sm.validate(userSignedIn, userToken)){
            showError("An account is already logged in.");
        }
        else {
            //show dialog
            final ProgressDialog progressDialog =
                    new ProgressDialog(SignInActivity.this,
                            R.style.Theme_AppCompat_DayNight_Dialog_Alert);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();

            //Now, attempt to login with the credentials inputted; if they are
            //invalid, then sm.login will return null, which will result in an
            //error message, otherwise a valid token is returned which will be
            //remembered for future use.
            userSignedIn = username;
            userToken = sm.login(username, password);

            if (userToken != null) {
                showError("You've been sucessfully logged in.");
                progressDialog.dismiss();
                changeScreenToBoardView(view);
            } else {
                showError("Invalid username and/or password.");

                //Negative Feedback for bad login: Punish!
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, 2000);
            }
        }
    }

    //Handler method when the user tries to log out.
    public void signOut(View view){
        //sm.logout returns a boolean: true if the logout was successful, and
        //false if the logout never happened due to the user not having been
        //logged in at all.
        try {
            sm.logout(userSignedIn);
            userSignedIn = null;
            userToken = null;
            VSNState.setCurrentUsername("");
            showError("You have been signed out.");
        } catch (ObjectNotFoundException e) {
            showError("You aren't logged in.");
        } catch (DatabaseException e) {
            PopUp.warning(this, e.toString());
        }
    }

    //Handler method when the user changes to the create account screen.
    public void changeScreenToBoardView(View view){
        Intent i = new Intent(this,
                BoardListActivity.class);
        VSNState.setCurrentUsername(userSignedIn);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this,
                AccountCreationActivity.class);
        startActivity(i);
        finish();
    }

    //Handler method when the user changes to the create account screen.
    public void changeScreenToCreateAccount(View view){
        Intent i = new Intent(this,
                              AccountCreationActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        um = getUserManager();
        sm = getSessionManager();
        setListeners();
    }

    private void setListeners(){
        EditText usernameBox = findViewById(R.id.signInUsernameField);
        EditText passwordBox = findViewById(R.id.signInPasswordField);
        setListener(usernameBox);
        setListener(passwordBox);
    }

    private void setListener(EditText view){
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
    }

    private void showError(String text){
        TextView messageBox = findViewById(R.id.signInError);
        messageBox.setText(text);
    }
}
