package com.vsn.persistance.fakes;

import com.vsn.objects.Board;
import com.vsn.objects.Note;
import com.vsn.exceptions.DatabaseException;
import com.vsn.exceptions.ObjectAlreadyExistsException;
import com.vsn.exceptions.ObjectNotFoundException;
import com.vsn.exceptions.RelationAlreadyExistsException;
import com.vsn.persistance.BoardDatabase;
import com.vsn.persistance.NoteDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class FakeNoteDatabase implements NoteDatabase {
    private static final HashMap<String, Note> noteDatabase = new HashMap<>();
    private static BoardDatabase boardDb = new FakeBoardDatabase();

    @Override
    public void createNote(Note note) throws DatabaseException {
        if (noteDatabase.containsKey(note.getUuid())) {
            throw new ObjectAlreadyExistsException();
        }
        noteDatabase.put(note.getUuid(), note);
        try {
            boardDb.addNoteBoardRelation(note.getBoardUuid(), note.getUuid());
        }catch(RelationAlreadyExistsException e){
            // Relation can't already exist.
        }catch (ObjectNotFoundException e){
            deleteNote(note.getUuid());
            throw new ObjectNotFoundException("Board not found");
        }
    }

    @Override
    public Note getNote(String noteUuid) throws ObjectNotFoundException{
        Note note = noteDatabase.get(noteUuid);
        if(note == null)
            throw new ObjectNotFoundException();
        return note;
    }

    @Override
    public void updateNote(Note note) throws ObjectNotFoundException {
        if(!noteDatabase.containsKey(note.getUuid())){
            throw new ObjectNotFoundException();
        }
        noteDatabase.put(note.getUuid(), note);
    }

    @Override
    public void deleteNote(String noteUuid) throws
            ObjectNotFoundException, DatabaseException {
        if(!noteDatabase.containsKey(noteUuid)){
            throw new ObjectNotFoundException();
        }

        Note note = getNote(noteUuid);
        boardDb.removeNoteBoardRelation(note.getBoardUuid(), noteUuid);

        noteDatabase.remove(noteUuid);

    }

    @Override
    public Collection<Note> getAllNotes() {
        return getAllNotes(null);
    }

    @Override
    public Collection<Note> getAllNotes(String boardUuid) {
        if(boardUuid == null){
            return ((HashMap<String, Note>)(noteDatabase.clone())).values();
        }
        try {
            Board board = boardDb.getBoard(boardUuid);
            HashSet<String> noteUuids =  board.getNotes();
            ArrayList<Note> notes =  new ArrayList<>();
            for(String noteUuid: noteUuids){
                notes.add(getNote(noteUuid));
            }
            return notes;
        }catch(ObjectNotFoundException e){
            return new ArrayList<>();
        }
    }

    @Override
    public void clearNotes() throws DatabaseException {
        for(Note note: getAllNotes()){
            try{
                deleteNote(note.getUuid());
            } catch (ObjectNotFoundException e) {
                // Should never happen
                e.printStackTrace();
            }
        }
    }

}
