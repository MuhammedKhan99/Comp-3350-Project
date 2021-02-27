package com.vsn.presentation;

import android.app.Activity;
import android.app.AlertDialog;
import com.vsn.R;

public class PopUp {

    public static void warning(Activity owner, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(owner).create();

        alertDialog.setTitle(owner.getString(R.string.warning));
        alertDialog.setMessage(message);

        alertDialog.show();
    }
}
