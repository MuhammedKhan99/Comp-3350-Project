package com.vsn.presentation.board;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import com.vsn.R;
import com.vsn.exceptions.DatabaseException;
import com.vsn.presentation.user.SettingsActivity;
import com.vsn.presentation.user.SignInActivity;
import com.vsn.business.managers.sessionManager.SessionManager;
import com.vsn.business.managers.BoardManager;
import com.vsn.business.managers.NoteManager;
import com.vsn.objects.Board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vsn.business.VSNState;

import static com.vsn.business.DependencySelector.getBoardManager;
import static com.vsn.business.DependencySelector.getNoteManager;
import static com.vsn.business.DependencySelector.getSessionManager;

public class BoardListActivity extends AppCompatActivity {
    Collection<Board> boards;
    BoardManager bm;
    NoteManager nm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boardlist);
        bm = getBoardManager();
        nm = getNoteManager();
        loadData();
        addButtonsToScroller(boards);
    }

    public void changeScreenToSettings(View view){
        Intent i = new Intent(this,
                SettingsActivity.class);
        startActivity(i);
        finish();
    }

    /**
     * load data from current user's board list.
     *
     */
    private void loadData(){
        BoardManager bm = getBoardManager();
        try {
            boards = bm.listBoards(VSNState.getCurrentUsername());
            sortBoards(boards);
        } catch (DatabaseException e) {
            showError("ERROR: Database exception. Something went wrong.");
        }
    }

    /**
     * Sorts the collection of boards to be displayed in alphabetical order
     * @param unsorted the unsorted board set
     */
    private void sortBoards(Collection unsorted){
        Collections.sort((List<Board>) unsorted, new Comparator<Board>() {
            public int compare(Board boardA, Board boardB) {
                int name = boardA.getName().compareTo(boardB.getName());
                return name;
            }
        });
    }

    /**
     * Button handler for adding a new board to the user's list
     * @param view required but unused
     */
    public void onAddBoardPressed(View view){
        BoardManager bm = getBoardManager();

        try {
            Board board = bm.create(
                    "<new board>", VSNState.getCurrentUsername());
            boards.add(board);
            refreshBoardList();
        } catch (DatabaseException e) {
            showError("ERROR: Database exception. Something went wrong.");
        }
    }

    /**
     * Handler for back button
     */
    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, SignInActivity.class);
        startActivity(i);
        finish();
    }

    /**
     * removes button objects, retrieves board list and recreates buttons
     */
    public void refreshBoardList(){
        removeAllButtons();
        loadData();
        addButtonsToScroller(boards);
    }

    /**
     * Removes all buttons from the scrollable section
     * Buttons are the only entities, so all touchables will only be buttons
     */
    private void removeAllButtons(){
        ArrayList<View> touchables;
        LinearLayout linLayout = findViewById(R.id.boardListLinear);
        touchables = (linLayout).getTouchables();
        for(int i = 0; i < touchables.size(); i++){
            Button button = (Button) touchables.get(i);
            linLayout.removeView(button);
        }
    }

    public void addBoardButtonToScroller(Board board, LinearLayout linLayout){
        Button button = new Button(this);
        button.setBackgroundResource(R.drawable.rounded_button);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT );
        lp.setMargins( 20, 20, 20, 20);
        button.setLayoutParams(lp);
        String boardText = board.getName();
        button.setText(boardText);
        String uuid = board.getUuid();
        setOnClickListener(button, uuid);
        setOnLongClickListener(button,uuid);
        linLayout.addView(button);
    }

    /**
     * Adds buttons to the scrollable section of this activity
     *
     * @param boards list of boards to add to scroller
     */
    private void addButtonsToScroller(Collection<Board> boards){
        //prepare scrollable section
        LinearLayout boardListLinear = findViewById(R.id.boardListLinear);

        //generate buttons
        for(Board board: boards){
            addBoardButtonToScroller(board, boardListLinear);
        }
    }

    /**
     *  Sets a long click listener to a button object
     *  Sends user to settings page for that button
     *
     * @param button button object to attach handler to
     * @param uuid uuid of board to load settings for
     */
    private void setOnLongClickListener(Button button, final String uuid){
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent i = new Intent(view.getContext(),
                        BoardSettingsActivity.class);
                VSNState.setCurrentBoardUUID(uuid);
                startActivityForResult(i, 999);
                return true;
            }
        });
    }

    /**
     * Assigns a click listener to dynamically generated buttons
     *
     * @param button object to attach listener to
     * @param uuid uuid of board displayed when button is pressed
     */
    private void setOnClickListener(Button button, final String uuid){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                gotoBoardView(uuid);
            }
        });
    }

    /**
     * start activity for viewing a board
     *
     * @param uuid of board to be displayed
     */
    private void gotoBoardView(String uuid){
        Intent i = new Intent(this,
                BoardActivity.class);
        VSNState.setCurrentBoardUUID(uuid);
        startActivityForResult(i, 999);
    }

    /**
     * Handler for returning from activity stack
     * Automatically refresh data and board list
     *
     * @param requestCode unused
     * @param resultCode unused
     * @param data unused
     */
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        refreshBoardList();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Button handler for sign out button
     *
     * @param view required, unused
     */
    public void signOut(View view){
        SessionManager sm = getSessionManager();
        try {
            sm.logout(VSNState.getCurrentUsername());
            VSNState.setCurrentUsername("");
            Intent i = new Intent(this,
                    SignInActivity.class);
            startActivity(i);
            finish();
        } catch(DatabaseException e){
            showError("Sign out failed, try again later.");
        }
    }

    /**
     * Displays an error in the messagebox
     * @param text error to display
     */
    private void showError(String text){
        TextView messageBox = findViewById(R.id.errorReport);
        messageBox.setText(text);
    }

}
