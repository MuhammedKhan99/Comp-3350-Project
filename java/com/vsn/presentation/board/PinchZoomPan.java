package com.vsn.presentation.board;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import com.vsn.objects.Board;
import com.vsn.objects.Note;
import com.vsn.exceptions.DatabaseException;
import com.vsn.presentation.PopUp;
import com.vsn.business.managers.NoteManager;
import com.vsn.business.managers.BoardManager;
import com.vsn.R;
import com.vsn.exceptions.ObjectNotFoundException;
import com.vsn.business.VSNState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import androidx.annotation.Nullable;

import static com.vsn.business.DependencySelector.getBoardManager;
import static com.vsn.business.DependencySelector.getNoteManager;

public class PinchZoomPan extends View {

    /**
     * Private Class and method for retrieving double taps
     * These direct to the note editor activity
     */
    private class tapListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            Note note = getNoteFromPosition(x, y);
            if (note != null){
                BoardActivity activity = (BoardActivity) getActivity();
                activity.gotoNoteActivity(note.getUuid(), false);
            } else {
                BoardActivity activity = (BoardActivity) getActivity();
                activity.gotoBoardEditActivity();
            }

            return true;
        }
    }

    /**
     *  Private Class and method for tracking scale gestures
     *  These are used to update the camera's zoom and position
     */
    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            //Update Scale: 'Perform Pinch Zoom'
            scaleFactor *= scaleGestureDetector.getScaleFactor();
            scaleFactor = Math.max(minZoom, Math.min(scaleFactor, maxZoom));

            //update the camera position while scaling
            float pointX = scaleGestureDetector.getFocusX();
            float pointY = scaleGestureDetector.getFocusY();
            float spanX =
                    scaleGestureDetector.getCurrentSpanX() -
                            scaleGestureDetector.getPreviousSpanX();
            float spanY =
                    scaleGestureDetector.getCurrentSpanY() -
                            scaleGestureDetector.getPreviousSpanY();

            updateCameraScaling(spanX, spanY, pointX, pointY);

            //redraw
            invalidate();
            return true;
        }
    }

    //State enumerations for pointers and dragging
    private static final int INVALID_POINTER_ID = -1;
    private static final int DRAG_MODE_NULL = 0;
    private static final int DRAG_MODE_CAMERA = 1;
    private static final int DRAG_MODE_NOTE = 2;
    private static final int DRAG_MODE_PINCH = 3;

    //Limits and default sizes
    private final static float minZoom = 0.01f;
    private final static float maxZoom = 1.1f;
    private final float FONT_SIZE = 200.0f;
    private final int NOTE_W = 1200;
    private final int NOTE_H = 1200;
    private final int CONTENT_X_OFFSET = 100;
    private final int CONTENT_Y_OFFSET = 175;
    private final int CONTENT_Y_GAP = 10;

    //Data Object Managers
    private BoardManager boardManager;
    private NoteManager noteManager;

    //Class variables
    private LinkedList<Note> notes;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector doubleTapDetector;
    private Bitmap noteImage;
    private float scaleFactor = 10.0f;
    private float cameraX;
    private float cameraY;
    private float prevTouchX;
    private float prevTouchY;
    private int currentPointerID;
    private int dragLatch;
    private Note dragNote;
    private int noteWidth;
    private int noteHeight;

    /**
     * This method inflates the pinchzoompan, preparing:
     *      Gesture and Touch listeners
     *      Retrieve bitmap for display
     *      Data Object Managers
     *
     * @param context recieves context of activity caller
     * @param attrs attribute collection from activity caller
     */
    public PinchZoomPan(Context context, @Nullable AttributeSet attrs) {
        //Initialization
        super(context, attrs);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        doubleTapDetector = new GestureDetector(context, new tapListener());
        setupNoteBitmap();
        boardManager = getBoardManager();
        noteManager = getNoteManager();

        //initialize drag latch and touch id
        currentPointerID = INVALID_POINTER_ID;
        dragLatch = DRAG_MODE_NULL;
        dragNote = null;
    }

    /**
     * This retrieves notes from the currently loaded board, determines an
     * appropriate zoom, and then draws the board
     */
    public void loadDataAndInitializeView(){
        notes = new LinkedList<>();
        loadNotesFromBoard();
        scaleAndPositionWindow();
        invalidate();
    }

    /**
     * This retrieves notes from the currently loaded board
     */
    private void loadNotesFromBoard(){
        try {
            Collection<Note> notesCollection = noteManager.listNotes(
                    VSNState.getCurrentBoardUUID());
            notes.addAll(notesCollection);
        } catch (DatabaseException e) {
            PopUp.warning(getActivity(), e.toString());
        }
    }

    /**
     * This loads the note image from resources and sets up the bitmap to be
     * used during draw()
     */
    private void setupNoteBitmap(){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.note_image);
        float aspectRatio =
                (float) bitmap.getHeight() / (float) bitmap.getWidth();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        noteWidth = displayMetrics.widthPixels;
        noteHeight = Math.round(noteWidth * aspectRatio);

        noteImage = Bitmap.createScaledBitmap(
                bitmap, noteWidth, noteHeight, false
        );
    }

    /**
     * This retrieves data needed to generate a new note and creates via
     * NoteManager: (x, y, currentUser, current board)
     *
     * The note is set to the top of the z-order
     * With the object, the note text editor is called
     *
     */
    public void addNewNote(){
        float newX = newNoteXPosition();
        float newY = newNoteYPosition();

        Note note = null;
        try {
            note = noteManager.createNote(
                    "", newX, newY, VSNState.getCurrentUsername(),
                    VSNState.getCurrentBoardUUID());
        } catch (DatabaseException e) {
            PopUp.warning(getActivity(), e.toString());
        }
        notes.addFirst(note);
        BoardActivity activity = (BoardActivity) getActivity();
        activity.gotoNoteActivity(note.getUuid(), true);
    }

    /**
     * @return the default X position for a new note calculated from the
     * current canvas camera positioning
     */
    public float newNoteXPosition(){
        float midScreenX = getWindowWidth() * 0.5f;
        return -cameraX / scaleFactor +  midScreenX;
    }

    /**
     * @return the default Y position for a new note calculated from the
     * current canvas camera positioning
     */
    public float newNoteYPosition(){
        float midScreenY = getWindowHeight() * 0.5f;
        return -cameraY / scaleFactor + midScreenY;
    }

    /**
     * Occurs on touch events: Main triage method to distribute user input to
     * functionality
     *
     * @param event the event being triaged
     * @return true when triage complete
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        scaleDetector.onTouchEvent(event);
        doubleTapDetector.onTouchEvent(event);

        final int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                actionDown(event);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                actionPointerDown();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                actionMove(event);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                actionPointerUp(action, event);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                actionCancel();
                break;
            }
        }
        return true;
    }

    /**
     * Behaviour for an additional touch down (multitouch)
     *      Flip latch to pinch mode
     *
     */
    private void actionPointerDown(){
        dragLatch = DRAG_MODE_PINCH;
    }

    /**
     * Behaviour for a new touch down event
     *      Determine if touch is on note or canvas and latch mode to drag
     *      note or camera as appropriate.
     *
     *      Stores the original touch point for actionMove to determine the
     *      drag vector.
     *
     * @param event event from onTouchEvent() triage
     */
    private void actionDown(MotionEvent event){
        final float x = event.getX();
        final float y = event.getY();
        prevTouchX = x;
        prevTouchY = y;
        currentPointerID = event.getPointerId(0);
        Note note;

        //Latch gesture type
        if (dragLatch == DRAG_MODE_NULL) {
            note = getNoteFromPosition(x, y);
            if (note != null) {
                dragLatch = DRAG_MODE_NOTE;
                dragNote = note;
                setZOrder(note);
            } else {
                dragLatch = DRAG_MODE_CAMERA;
            }
        }
    }

    /**
     * This is called many times during a drag gesture; it breaks the
     * movement up into small x,y distances and distributes these to the
     * currently latched target (a note or the canvas camera)
     *
     * @param event event from onTouchEvent() triage
     */
    private void actionMove(MotionEvent event){
        //get pointer index and its x,y coordinates
        final int pointerIndex = event.findPointerIndex(
                currentPointerID);
        final float x = event.getX(pointerIndex);
        final float y = event.getY(pointerIndex);

        if (!scaleDetector.isInProgress()) {
            //calculate distance of move
            final float distanceX = x - prevTouchX;
            final float distanceY = y - prevTouchY;

            //distribute movement to the latched target
            switch (dragLatch) {
                case DRAG_MODE_CAMERA: {
                    updateCameraPosition(distanceX, distanceY);
                }
                case DRAG_MODE_NOTE: {
                    updateNotePosition(dragNote, distanceX, distanceY);
                }
            }
            //redraw canvas
            invalidate();
        }

        //push current coordinates to previous for next segment of drag
        prevTouchX = x;
        prevTouchY = y;
    }

    /**
     *  This is called when touch/multitouch is ended (fingers removed from
     *  screen) or when a touch action is interrupted: it resets the three
     *  variables used for handling gestures
     *
     */
    private void actionCancel(){
        currentPointerID = INVALID_POINTER_ID;
        dragLatch = DRAG_MODE_NULL;
        dragNote = null;
    }

    /**
     * This occurs when a multitouch has been lifted from the screen
     * This shifts IDs to prevent jumping around the screen
     *
     * @param actionId action id from onTouchEvent() triage
     * @param event event from onTouchEvent() triage
     */
    private void actionPointerUp(int actionId, MotionEvent event){
        //get the index that just left the screen
        final int pointerIndex = (
                actionId & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = event.getPointerId(pointerIndex);
        if (pointerId == currentPointerID) {
            //Choose another pointer and adjust
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            prevTouchX = event.getX(newPointerIndex);
            prevTouchY = event.getY(newPointerIndex);
            currentPointerID = event.getPointerId(newPointerIndex);
        }
    }

    /**
     * Linear 2d collision detection comparing touch point to note positions.
     *      Multi-collisions from overlapping notes are resolved by the
     *      Z-Ordering of the note list: first note found is returned as it is
     *      "closest to the screen"
     *
     * @param posX touch x
     * @param posY touch y
     * @return null if no collision, the Note object if a collision occurs.
     */
    private Note getNoteFromPosition(float posX, float posY){

        for (int index = 0; index < notes.size(); index++) {
            float[] notePos = notes.get(index).getPosition();
            float minX = cameraX + notePos[0] * scaleFactor;
            float minY = cameraY +notePos[1] * scaleFactor;
            float maxX = minX + noteWidth * scaleFactor;
            float maxY = minY + noteHeight * scaleFactor;

            boolean inRangeX = minX <= posX && posX <= maxX;
            boolean inRangeY = minY <= posY && posY <= maxY;

            if (inRangeX && inRangeY) {return notes.get(index);}
        }
        return null;
    }

    /**
     * This applies a directed offset to the camera position
     *
     * @param offsetX distance X from actionMove()
     * @param offsetY distance Y from actionMove()
     */
    private void updateCameraPosition(float offsetX, float offsetY){
        cameraX += offsetX;
        cameraY += offsetY;
    }

    /**
     * This applies a directional shift to a pinch zoom so that zoom is
     * applied to the touch point instead of the left top corner.
     *
     * @param spanX X scaling value from pinch
     * @param spanY Y scaling value from pinch
     * @param eventX X touchpoint
     * @param eventY Y touchpoint
     */
    private void updateCameraScaling(float spanX, float spanY, float eventX,
                                     float eventY){
        float shiftX = (cameraX - eventX) * spanX / getWindowWidth();
        float shiftY = (cameraY - eventY) * spanY / getWindowHeight();

        cameraX += shiftX;
        cameraY += shiftY;
    }

    /**
     * this applies a scaled direction shift to a note object and updates the
     * note's internal position record
     *
     * @param note the note object
     * @param offsetX the x shift
     * @param offsetY the y shift
     */
    private void updateNotePosition(Note note, float offsetX, float offsetY){
        if(note == null) {return;}

        float[] notePos = note.getPosition();
        float noteX = notePos[0];
        float noteY = notePos[1];

        float posX = noteX + offsetX / scaleFactor;
        float posY = noteY + offsetY / scaleFactor;

        note.setPosition(posX, posY);
        try {
            noteManager.updateNote(note);
        } catch (DatabaseException e) {
            PopUp.warning(getActivity(), e.toString());
        }

    }

    /**
     * This updates a note to the top of the z-order list
     *
     * @param note the new 'top' note object
     */
    private void setZOrder(Note note){
        notes.remove(note);
        notes.addFirst(note);
    }

    /**
     * This is the main drawing procedure called whenever the canvas has
     * become invalidated.
     *      Applies translation and scaling of the 'camera' position and zoom
     *      Iterates through note objects and calls their draw functions
     *
     * @param canvas the current canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        Board thisBoard = null;
        try {
            thisBoard = boardManager.getBoard(VSNState.getCurrentBoardUUID());
        } catch (ObjectNotFoundException e) {
            PopUp.warning(getActivity(), e.toString());
        }
        super.onDraw(canvas);

        int canvasColour = thisBoard.getColour();
        canvas.drawColor(canvasColour);

        canvas.save();
        canvas.translate(cameraX, cameraY);
        canvas.scale(scaleFactor, scaleFactor);

        int numNotes = notes.size() - 1;

        for (int index = 0; index <= numNotes; index++) {
            Note note = notes.get(numNotes - index);
            drawNote(note, canvas);
        }

        canvas.restore();
    }

    /**
     * This is the draw procedure for a note. Retrieves note data, draws a
     * note bitmap, calculates a collection of note text and draws onto the
     * canvas.
     *
     * @param note the note object to be drawn
     * @param canvas the canvas object to draw to
     */
    private void drawNote(Note note, Canvas canvas){
        //draw bitmap
        String contentStr = note.getData();
        float[] notePos = note.getPosition();
        float noteX = notePos[0];
        float noteY = notePos[1];
        float width = noteImage.getWidth() - 2*(CONTENT_X_OFFSET);
        float height = noteImage.getWidth() - CONTENT_Y_OFFSET;
        int colour = note.getColour();
        Paint paint = new Paint();
        ColorFilter filter =
                new PorterDuffColorFilter(colour, PorterDuff.Mode.MULTIPLY);
        paint.setColorFilter(filter);
        canvas.drawBitmap(noteImage, noteX, noteY, paint);

        if(contentStr.length() > 0) {
            Paint textPaint = getTextPaint();
            ArrayList<String >content =
                    CanvasTextCollapser.stringToConstrainedList(contentStr,
                    textPaint, height, width, CONTENT_Y_GAP);
            drawNoteText(canvas, content, textPaint, noteX, noteY);
        }
    }

    /**
     * this is the draw procedure for note text. Canvas text does not have
     * wrapping, so text must be split into a collection of appropriate
     * lengths. Paint is used to calculate the height of each line and
     * coordinate offsets for each new line.
     *
     * @param canvas the canvas object to draw to
     * @param content the collection of strings to draw
     * @param textPaint the Paint format to apply to the text
     * @param noteX the note's left coordinate
     * @param noteY the note's top coordinate
     */
    private void drawNoteText(Canvas canvas, ArrayList<String> content,
                              Paint textPaint, float noteX, float noteY){
        //set starting positions
        float xPosition = noteX + CONTENT_X_OFFSET;
        float yPosition = noteY + CONTENT_Y_OFFSET;
        float heightOffset = 0;

        //draw each line
        for (int i = 0; i < content.size(); i++) {
            yPosition += heightOffset;
            String textLine = content.get(i);
            canvas.drawText(textLine, xPosition, yPosition, textPaint);
            textLine += "A";// add a character to calc height of empty lines
            heightOffset = CONTENT_Y_GAP + CanvasTextCollapser.textHeight(
                    textLine, textPaint);
        }
    }

    /**
     * this is called at startup to determine how to show as many notes as
     * possible on the canvas via xy position and zooming.
     *      This always includes the most upper-left note
     *      This will be limited by the zoom limits
     */
    private void scaleAndPositionWindow(){
        //Scale
        float skew = calculateScaleFactor();
        scaleFactor = Math.max(
                minZoom, Math.min(skew, maxZoom));

        //Position
        setWindowPositionToMinXY();
    }

    /**
     * This finds the most upper-left note coordinate and sets the upper-left
     * of the camera view to that position
     */
    private void setWindowPositionToMinXY(){
        float minX = 0;
        float minY = 0;
        for (int index = 0; index < notes.size(); index++){
            float[] notePos = notes.get(index).getPosition();
            float noteX = notePos[0];
            float noteY = notePos[1];
            minX = Math.min(minX, noteX);
            minY = Math.min(minY, noteY);
        }
        cameraX = minX + 1;
        cameraY = minY + 1;
    }

    /**
     * This calculates the maximum of the x and y view brackets to scale
     * the window appropriately
     * (As possible by zoom limits)
     *
     * @return the scaling factor after window comparison
     */
    private float calculateScaleFactor(){
        float minX = 0;
        float maxX = 0;
        float minY = 0;
        float maxY = 0;

        for (int index = 0; index < notes.size(); index++){
            float[] notePos = notes.get(index).getPosition();
            float noteX = notePos[0];
            float noteY = notePos[1];

            minX = Math.min(minX, noteX);
            maxX = Math.max(maxX, noteX + NOTE_W);
            minY = Math.min(minY, noteY);
            maxY = Math.max(maxY, noteY + NOTE_H);
        }

        return calculateScalingVsWindow(maxX-minX, maxY-minY);
    }

    /**
     *  This compares the used X range and used Y range to the available
     *  screen width and height, returning the ratio with the greater
     *  requirement.
     *
     * @param sizeX X span of used screen
     * @param sizeY Y span of used screen
     * @return the scaling factor from note positions and screen size
     */
    private float calculateScalingVsWindow(float sizeX, float sizeY){
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float width = size.x;
        float height = size.y;

        float skewX =  width / sizeX;
        float skewY = height / sizeY;

        return Math.min(skewX, skewY);
    }

    /**
     * calculates the window's width from the window manager
     *
     * @return the window width
     */
    private float getWindowWidth(){
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    /**
     * calculates the window's height from the window manager
     *
     * @return the window height
     */
    private float getWindowHeight(){
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    /**
     * This aids in setup by preloading the bitmap used to draw notes, called
     * by Activity calling this class
     *
     * @param item the resource item
     */
    public void preloadBitmap(int item) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), item);
        float aspectRatio =
                (float) bitmap.getHeight() / (float) bitmap.getWidth();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        noteWidth = displayMetrics.widthPixels;
        noteHeight = Math.round(noteWidth * aspectRatio);
        noteImage = Bitmap.createScaledBitmap(
                bitmap, noteWidth, noteHeight, false
        );
        //draw
        invalidate();
    }

    /**
     * Sets up a returns the paint formatting to be used for text
     *
     * @return the Paint object
     */
    private Paint getTextPaint(){
        Paint textPaint = new Paint();
        textPaint.setColor(Color.rgb(0, 0, 0));
        textPaint.setTextSize(FONT_SIZE * scaleFactor);
        textPaint.setStrokeWidth(1);
        return textPaint;
    }

    /**
     * Retrieves the activity containing and using this Class
     *
     * @return Activity object
     */
    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

}