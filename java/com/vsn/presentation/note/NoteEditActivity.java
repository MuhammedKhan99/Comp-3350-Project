package com.vsn.presentation.note;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.skydoves.colorpickerpreference.ColorEnvelope;
import com.skydoves.colorpickerpreference.ColorListener;
import com.skydoves.colorpickerpreference.ColorPickerView;
import com.vsn.objects.Note;
import com.vsn.exceptions.DatabaseException;
import com.vsn.exceptions.ObjectNotFoundException;
import com.vsn.business.managers.NoteManager;
import com.vsn.R;
import com.vsn.business.VSNState;

import androidx.core.graphics.drawable.DrawableCompat;

import static com.vsn.business.DependencySelector.getNoteManager;

public class NoteEditActivity extends AppCompatActivity {

    NoteManager noteManager;
    Note note;
    TextView textView;
    View noteView;
    ColorPickerView colourPickerView;
    int originalColour;
    boolean writeEnable = false;

    boolean isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this); // Initialize AndroidThreeTen
        setContentView(R.layout.activity_noteeditor);

        Intent messageIntent = getIntent();
        isNew = messageIntent.getBooleanExtra("isNew", false);
        noteManager = getNoteManager();
        textView = findViewById(R.id.textView);
        noteView = findViewById(R.id.noteSquare);
        colourPickerView = findViewById(R.id.colorPickerView);

        //set up functionality
        setClickListenerCancel();
        setClickListenerSubmit();
        setTouchListener();
        setColourListener();
        setFocusChangeListener();

        //set up content
        try {
            note = noteManager.getNote(VSNState.getCurrentNoteUUID());

            String text = note.getData();
            textView.setText(text);

            int colour = note.getColour();
            originalColour = colour;
            setNoteBitmapTint(colour);

            writeEnable = true;

        } catch (ObjectNotFoundException e) {
            showError("Something went wrong.");
            finish();
        }

    }

    private void setClickListenerCancel(){
        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNew) {  // new Note gets Deleted on Cancellations
                    try {
                        noteManager.deleteNote(VSNState.getCurrentNoteUUID());
                    } catch (DatabaseException e) {
                        showError("Something went wrong.");
                    }
                } else {
                    updateNoteColour(originalColour);
                }
                finish();
            }
        });
    }

    private void setClickListenerSubmit(){
        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView t = findViewById(R.id.textView);
                String data = t.getText().toString();
                try {
                    noteManager.updateNote(note);
                    noteManager.editNote(note.getUuid(), data);
                } catch (DatabaseException e) {
                    showError("Something went wrong.");
                }
                finish();
            }
        });
    }

    private void setTouchListener(){
        textView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY);
                return false;
            }
        });
    }

    private void setColourListener(){
        colourPickerView.setColorListener(new ColorListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope) {
                int colour = envelope.getColor();
                if (colour != Color.rgb(254,255,254)) {
                    setNoteBitmapTint(colour);
                    updateNoteColour(colour);
                }
            }
        });
    }

    private void updateNoteColour(int colour){
        note.setColour(colour);
    }

    private void setFocusChangeListener(){
        textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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

    private void setNoteBitmapTint(int colour){
        Drawable drawable = noteView.getForeground();
        DrawableCompat.setTint(drawable, colour);
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.MULTIPLY);
    }

    private void showError(String text){
        TextView messageBox = findViewById(R.id.settingsWarning);
        messageBox.setText(text);
    }
}