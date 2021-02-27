package com.vsn.presentation.board;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.vsn.R;
import androidx.appcompat.app.AppCompatActivity;

import com.vsn.exceptions.ObjectNotFoundException;
import com.vsn.presentation.note.NoteEditActivity;
import com.vsn.business.managers.BoardManager;
import com.vsn.business.VSNState;

public class BoardActivity extends AppCompatActivity {

    protected PinchZoomPan pinchZoomPan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_panzoom);

        pinchZoomPan = findViewById(R.id.image_sample);
        pinchZoomPan.preloadBitmap(R.drawable.note_image);
        pinchZoomPan.loadDataAndInitializeView();
    }

    /**
     *  Handles back button
     */
    public void onBackPressed() {
        finish();
    }

    /**
     * Starts up the note editor
     *
     * @param uuid uuid of the note to edit
     * @param isNew true if this is a newly generated note
     */
    public void gotoNoteActivity(String uuid, boolean isNew) {
        Intent i = new Intent(this,
                NoteEditActivity.class);
        VSNState.setCurrentNoteUUID(uuid);
        i.putExtra("isNew", isNew);
        super.startActivityForResult(i, 999);
    }

    /**
     *  starts up the board editor
     *
     */
    public void gotoBoardEditActivity() {
        Intent i = new Intent(this,
                BoardSettingsActivity.class);
        super.startActivityForResult(i, 999);
    }

    /**
     * called when returning from the activity stack
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check if board exists (post-delete)
        BoardManager bm = new BoardManager();
        try {
            bm.getBoard(VSNState.getCurrentBoardUUID());
            //reload all Notes
            pinchZoomPan.loadDataAndInitializeView();
        } catch (ObjectNotFoundException e) {
            //board was deleted - return to list
            finishActivity(999);
            finish();
        }
    }

    /**
     *  button handler for adding a new note
     *
     * @param view required for linking; unused
     */
    public void addNewNote(View view){
        pinchZoomPan.addNewNote();
    }

    /**
     * handler method to return screen to the Board List
     *
     * @param view
     */
    public void changeScreenToBoardListView(View view){
        finishActivity(999);
        finish();
    }

}