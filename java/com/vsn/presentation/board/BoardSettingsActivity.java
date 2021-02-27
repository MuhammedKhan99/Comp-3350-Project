package com.vsn.presentation.board;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.skydoves.colorpickerpreference.ColorEnvelope;
import com.skydoves.colorpickerpreference.ColorListener;
import com.skydoves.colorpickerpreference.ColorPickerView;
import com.vsn.R;
import com.vsn.objects.Board;
import com.vsn.exceptions.DatabaseException;
import com.vsn.exceptions.ObjectNotFoundException;
import com.vsn.business.managers.BoardManager;
import com.vsn.business.VSNState;

public class BoardSettingsActivity extends AppCompatActivity {

    BoardManager boardManager;
    Board thisBoard;
    EditText boardTitle;
    ColorPickerView colourPickerView;
    String originalTitle;
    private int originalColour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_settings);

        boardManager = new BoardManager();
        colourPickerView = findViewById(R.id.colorPickerView_Board);

        try {
            thisBoard = boardManager.getBoard(VSNState.getCurrentBoardUUID());
            originalColour = thisBoard.getColour();
        } catch (ObjectNotFoundException e) {
            finishActivity(999);
            finish();
        }

        boardTitle = findViewById(R.id.boardTitle);
        originalTitle = thisBoard.getName();
        boardTitle.setText(originalTitle);

        setListener(boardTitle);
        setColourListener();

    }

    /**
     * Handler for delete notes button, removes notes from board
     * @param view required, unused
     */
    public void onNotesDeleteButton(View view) {
        try {
            boardManager.clearBoard(VSNState.getCurrentBoardUUID());
            setResult(999);
            finish();
        } catch (DatabaseException e) {
            finishActivity(999);
        }
    }

    /**
     * Handler for delete button
     * @param view required, used to retrieve context
     */
    public void onDeleteButtonPressed(View view){
        confirmDelete(view.getContext());
    }

    /**
     * deletes board and returns to activity stack
     */
    public void deleteBoard() {
        try {
            boardManager.deleteBoard(VSNState.getCurrentBoardUUID());
            setResult(999);
            finish();
        } catch (DatabaseException e) {
            finishActivity(999);
        }
    }

    /**
     * handler for submit button, applies changes and returns
     * @param view requred, unused
     */
    public void onSubmitPressed(View view){
        String newTitle = boardTitle.getText().toString();
        try {
            boardManager.updateBoard(thisBoard);
            boardManager.changeTitle(newTitle, thisBoard.getUuid());
            setResult(999);
            finish();
        } catch (DatabaseException e) {
            finishActivity(999);
        }
    }

    /**
     * handler for cancel button, removes changes and returns
     * @param view required, unused
     */
    public void onCancelButtonPressed(View view){
        updateBoardColour(originalColour);
        finishActivity(999);
        finish();
    }

    /**
     * handler for back button, returns to previous activity
     */
    @Override
    public void onBackPressed() {
        updateBoardColour(originalColour);
        finishActivity(999);
        finish();
    }

    /**
     * sets listener to text field; when focus is lost, hides the keyboard
     * @param view required to apply listener
     */
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

    /**
     * sets listener for new colour selection and applies changes to activity
     * background and board settings
     */
    private void setColourListener(){
        colourPickerView.setColorListener(new ColorListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope) {
                int colour = envelope.getColor();
                if (colour != Color.rgb(254,255,254)) {
                    setBackgroundColour(colour);
                    updateBoardColour(colour);
                }
            }
        });
    }

    /**
     * applies colour to the current board
     * @param colour to be applied
     */
    private void updateBoardColour(int colour){
        thisBoard.setColour(colour);
    }

    /**
     * sets background colour of the activity
     * @param colour integer representation
     */
    private void setBackgroundColour(int colour){
        ConstraintLayout cl = findViewById(R.id.board_settings_view);
        cl.setBackgroundColor(colour);
    }

    private void confirmDelete(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder
                .setMessage("Are you sure?")
                .setPositiveButton("Yes",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        deleteBoard();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

}
