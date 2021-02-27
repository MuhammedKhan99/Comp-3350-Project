package com.vsn.persistance;

import com.vsn.objects.Note;
import com.vsn.exceptions.DatabaseException;
import com.vsn.exceptions.ObjectNotFoundException;

import java.util.Collection;

public interface NoteDatabase {
    // Note CRUD
    void createNote(Note note) throws DatabaseException;
    Note getNote(String noteUuid) throws ObjectNotFoundException;
    void updateNote(Note note) throws DatabaseException;
    void deleteNote(String noteUuid) throws DatabaseException;
    Collection<Note> getAllNotes() throws DatabaseException;
    Collection<Note> getAllNotes(String boardUuid) throws DatabaseException;
    void clearNotes() throws DatabaseException;

}
