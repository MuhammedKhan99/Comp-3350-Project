package com.vsn.business.managers;

import com.vsn.objects.Note;
import com.vsn.exceptions.DatabaseException;
import com.vsn.exceptions.ObjectAlreadyExistsException;
import com.vsn.exceptions.ObjectNotFoundException;
import com.vsn.persistance.NoteDatabase;
import com.vsn.business.DependencySelector;

import java.util.Collection;

public class NoteManager {

    private NoteDatabase database;

    public NoteManager(){
        this.database = DependencySelector.getNoteDatabase();
    }

    /**
     *
     * @param data       whatever initial data written for the 1st time
     *                   during note creation.
     * @param positionX  horizontal position on board
     * @param positionY  vertical position on board
     * @param creator    UserName
     */
    public Note createNote(String data,
                           float positionX,
                           float positionY,
                           String creator,
                           String boardUuid) throws DatabaseException {
        // by default permission is true. means editable by all.
        Note note = new Note(
                data, positionX, positionY, true,
                creator.toUpperCase(), boardUuid);
        try {
            database.createNote(note);
        } catch (ObjectAlreadyExistsException e) {
            // Would only happen on UUID collision, so once in a trillion years
        }
        return note;
    }

    /**
     *
     * @param UUID  UUID of the note to retrieve
     * @return      a Note object if successful, null if the note doesn't exist.
     */
    public void deleteNote(String UUID) throws DatabaseException {
        database.deleteNote(UUID);
    }

    /**
     *
     * @param UUID      UUID of the note to edit
     * @param newData   the data with which the note will be updated
     * @return  true if successful, false if note does not exist in database
     */
    public void editNote(String UUID, String newData) throws DatabaseException {
        Note note = getNote(UUID);
        note.setData(newData);
        database.updateNote(note);
    }

    /**
     *
     * @param note       The new note
     * @return  true if successful, false if note does not exist in database
     */
    public void updateNote(Note note) throws DatabaseException {
        database.updateNote(note);
    }

    /**
     *
     * @param UUID  UUID of the note to retrieve from database
     * @return      a Note object if successful, null if the note doesn't exist.
     */
    public Note getNote(String UUID) throws ObjectNotFoundException {
        return database.getNote(UUID);
    }

    /**
     * @return Return a collection of all Notes in the databasse
     */
    public Collection<Note> listNotes() throws DatabaseException {
        return database.getAllNotes();

    }

    /**
     *
     * @param boardUuid The UUID of the board to return all notes from
     * @return Return a collection of all Notes on the board given by boardUuid
     */
    public Collection<Note> listNotes(String boardUuid)
            throws DatabaseException {
        return database.getAllNotes(boardUuid);
    }

}
