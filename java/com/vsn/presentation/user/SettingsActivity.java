package com.vsn.presentation.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.vsn.exceptions.DatabaseException;
import com.vsn.presentation.board.BoardListActivity;
import com.vsn.business.utilities.AccountUtilities;
import com.vsn.R;

import com.vsn.business.managers.UserManager;
import com.vsn.business.VSNState;

import static com.vsn.business.DependencySelector.getUserManager;

public class SettingsActivity extends AppCompatActivity {
    private UserManager um;

    public void saveChanges(View view){
        // Gather user input
        EditText passwordBox = findViewById(R.id.passwordField);
        String password = passwordBox.getText().toString();
        EditText emailBox = findViewById(R.id.emailField);
        String email = emailBox.getText().toString();
        EditText newPasswordBox = findViewById(R.id.newPasswordField);
        String newPassword = newPasswordBox.getText().toString();
        EditText confirmPasswordBox = findViewById(R.id.passwordConfirmField);
        String confirmPassword = confirmPasswordBox.getText().toString();

        // Process and Show Message
        String toShow = AccountUtilities.processAccountModification(
                VSNState.getCurrentUsername(),
                password,
                newPassword,
                confirmPassword,
                email);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(toShow).show();
    }

    public void deleteAccount(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setMessage("Are you sure? This is irreversible.")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    um.deleteUser(
                                            VSNState.getCurrentUsername());
                                    changeScreenToSignIn();
                                } catch (DatabaseException e) {
                                    showError(
                                        "Could not delete user account.");
                                }
                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.cancel();
                        }
                    })
                    .show();
    }



    public void changeScreenToBoardList(View view){
        viewBoardList();
    }

    public void changeScreenToSignIn(){
        Intent i = new Intent(this,
                SignInActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        um = getUserManager();
        TextView usernameBox = findViewById(R.id.usernameText);
        usernameBox.setText(VSNState.getCurrentUsername());
        EditText emailField = findViewById(R.id.emailField);
        setListener(emailField);
        EditText passwordField = findViewById(R.id.passwordField);
        setListener(passwordField);
        EditText newPasswordField = findViewById(R.id.newPasswordField);
        setListener(newPasswordField);
        EditText passwordConfirmField = findViewById(R.id.passwordConfirmField);
        setListener(passwordConfirmField);
    }

    private void setListener(EditText view){
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        viewBoardList();
    }

    public void viewBoardList(){
        Intent i = new Intent(this,
                BoardListActivity.class);
        startActivity(i);
        finish();
    }

    private void showError(String text){
        TextView messageBox = findViewById(R.id.settingsWarning);
        messageBox.setText(text);
    }
}
