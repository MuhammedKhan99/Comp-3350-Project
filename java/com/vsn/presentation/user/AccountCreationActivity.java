package com.vsn.presentation.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.vsn.persistance.hsqldb.HSQLDBUtilities;
import com.vsn.presentation.PopUp;
import com.vsn.business.utilities.AccountUtilities;
import com.vsn.R;

import com.vsn.business.managers.UserManager;

import static com.vsn.business.DependencySelector.getUserManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class AccountCreationActivity extends AppCompatActivity {

    /*
     * Handler method when the user presses the "Create Account" button.
     * Checks each field for validity, reports with a message at any faults,
     * creates account if everything checks out.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this); // Initialize AndroidThreeTen
        setContentView(R.layout.activity_accountcreation);
        copyDatabaseToDevice();
        setListeners();
    }

    public void createAccount(View view) {
        //Obtain the contents of each of the text fields.
        EditText usernameBox = findViewById(R.id.usernameField);
        String username = usernameBox.getText().toString();
        EditText passwordBox = findViewById(R.id.passwordText);
        String password = passwordBox.getText().toString();
        EditText passwordConfirmBox = findViewById(R.id.passwordConfirmField);
        String passwordConfirm = passwordConfirmBox.getText().toString();
        EditText emailBox = findViewById(R.id.emailField);
        String email = emailBox.getText().toString();
        EditText firstNameBox = findViewById(R.id.firstNameField);
        String firstName = firstNameBox.getText().toString();
        EditText lastNameBox = findViewById(R.id.lastNameField);
        String lastName = lastNameBox.getText().toString();
        //Obtain and clear the message window.
        TextView messageBox = findViewById(R.id.accountCreationError);
        messageBox.setText("");
        //Create a UserManager to interface with.
        UserManager um = getUserManager();

        String output = AccountUtilities.createAccount(
                username,
                password,
                passwordConfirm,
                email,
                firstName,
                lastName);
        messageBox.setText(output);
    }

    //Handler method to change to the sign-in screen.
    public void changeScreenToSignIn(View view) {
        Intent i = new Intent(this, SignInActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
        moveTaskToBack(true);
    }

    private void setListeners(){
        EditText usernameField = findViewById(R.id.usernameField);
        EditText passwordBox = findViewById(R.id.passwordText);
        EditText passwordConfirmBox = findViewById(R.id.passwordConfirmField);
        EditText emailBox = findViewById(R.id.emailField);
        EditText firstNameBox = findViewById(R.id.firstNameField);
        EditText lastNameBox = findViewById(R.id.lastNameField);
        setListener(usernameField);
        setListener(passwordBox);
        setListener(passwordConfirmBox);
        setListener(emailBox);
        setListener(firstNameBox);
        setListener(lastNameBox);
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

    private void copyDatabaseToDevice() {
        final String DB_PATH = "db";

        String[] assetNames;
        Context context = getApplicationContext();
        File dataDirectory = context.getDir(DB_PATH, Context.MODE_PRIVATE);
        AssetManager assetManager = getAssets();

        try {
            assetNames = assetManager.list(DB_PATH);
            for (int i = 0; i < assetNames.length; i++) {
                assetNames[i] = DB_PATH + "/" + assetNames[i];
            }

            copyAssetsToDirectory(assetNames, dataDirectory);

            HSQLDBUtilities.setDBPathName(dataDirectory.toString() + "/" +
                    HSQLDBUtilities.getDBPathName());

        } catch (final IOException ioe) {
            PopUp.warning(this,
                    "Unable to connect to Database" + ioe.getMessage());
        }
    }

    public void copyAssetsToDirectory(String[] assets, File directory) throws IOException {
        AssetManager assetManager = getAssets();

        for (String asset : assets) {
            String[] components = asset.split("/");
            String copyPath = directory.toString() + "/" + components[components.length - 1];

            char[] buffer = new char[1024];
            int count;

            File outFile = new File(copyPath);

            if (!outFile.exists()) {
                InputStreamReader in = new InputStreamReader(assetManager.open(asset));
                FileWriter out = new FileWriter(outFile);

                count = in.read(buffer);
                while (count != -1) {
                    out.write(buffer, 0, count);
                    count = in.read(buffer);
                }

                out.close();
                in.close();
            }
        }
    }

}
