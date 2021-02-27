package com.vsn.persistance.fakes;

import com.vsn.objects.Board;
import com.vsn.objects.Note;
import com.vsn.objects.User;
import com.vsn.exceptions.DatabaseException;
import com.vsn.exceptions.ObjectAlreadyExistsException;
import com.vsn.exceptions.ObjectNotFoundException;
import com.vsn.exceptions.RelationAlreadyExistsException;
import com.vsn.persistance.BoardDatabase;
import com.vsn.persistance.NoteDatabase;
import com.vsn.persistance.UserDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class FakeBoardDatabase implements BoardDatabase {
    private static final HashMap<String, Board> boardDatabase = new HashMap<>();
    private static UserDatabase userDb = new FakeUserDatabase();
    private static NoteDatabase noteDb = new FakeNoteDatabase();

    @Override
    public void createBoard(Board board) throws
            ObjectAlreadyExistsException, ObjectNotFoundException,
            DatabaseException {
        if(boardDatabase.containsKey(board.getUuid())){
            throw new ObjectAlreadyExistsException();
        }
        boardDatabase.put(board.getUuid(), board);
        for(String username: board.getUsers()){
            try {
                addBoardUserRelation(username, board.getUuid());
            } catch (RelationAlreadyExistsException e) {
                // Then the works already been done eh
            }
        }
    }

    @Override
    public Board getBoard(String boardUuid) throws ObjectNotFoundException {
        Board board = boardDatabase.get(boardUuid);
        if(board == null)
            throw new ObjectNotFoundException();
        return board;
    }

    @Override
    public void updateBoard(Board board) throws
            ObjectNotFoundException, DatabaseException {
        if(!boardDatabase.containsKey(board.getUuid())){
            throw new ObjectNotFoundException("Board does not exist");
        }
        boardDatabase.put(board.getUuid(), board);
        for(String username: board.getUsers()){
            try {
                addBoardUserRelation(username, board.getUuid());
            } catch (RelationAlreadyExistsException e) {
                // Then the works already been done eh
            }
        }
    }

    @Override
    public void deleteBoard(String boardUuid) throws
            ObjectNotFoundException, DatabaseException {

        if(!boardDatabase.containsKey(boardUuid)){
            throw new ObjectNotFoundException();
        }

        Board board = getBoard(boardUuid);
        for(String username: board.getUsers()){
            removeBoardUserRelation(username, boardUuid);
        }
        for(Note note : noteDb.getAllNotes(boardUuid)){
            noteDb.deleteNote(note.getUuid());
        }

        boardDatabase.remove(boardUuid);
    }

    @Override
    public Collection<Board> getAllBoards()
            throws DatabaseException {
        return getAllBoards(null);
    }
    @Override
    public Collection<Board> getAllBoards(String username)
            throws DatabaseException {
        if(username == null){
            return ((HashMap<String, Board>)(boardDatabase.clone())).values();
        }
        try {
            User user = userDb.getUser(username);
            HashSet<String> boardUuids = user.getBoards();
            ArrayList<Board> boards =  new ArrayList<>();
            for(String boardUuid: boardUuids){
                boards.add(getBoard(boardUuid));
            }
            return boards;
        }catch(ObjectNotFoundException e){
            return null;
        }
    }

    @Override
    public void clearBoards() throws DatabaseException {
        for(Board board: getAllBoards()){
            try {
                deleteBoard(board.getUuid());
            } catch (ObjectNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addBoardUserRelation(String username, String boardUuid) throws
            DatabaseException {

        Board board = getBoard(boardUuid);
        User user = userDb.getUser(username);

        if(board.getUsers().contains(username) &&
                user.getBoards().contains(boardUuid))
            throw new RelationAlreadyExistsException();

        user.addBoard(boardUuid);
        board.addUser(username);

        updateBoard(board);
        userDb.updateUser(user);

    }

    @Override
    public void removeBoardUserRelation(String username, String boardUuid)
            throws ObjectNotFoundException, DatabaseException {

        Board board = getBoard(boardUuid);
        User user = userDb.getUser(username);

        user.removeBoard(boardUuid);
        board.removeUser(username);

        updateBoard(board);
        userDb.updateUser(user);
    }

    @Override
    public void addNoteBoardRelation(String boardUuid, String noteUuid) throws
            DatabaseException {

        Note note = noteDb.getNote(noteUuid);
        Board board = getBoard(boardUuid);

        if(board.getNotes().contains(noteUuid) &&
                note.getBoardUuid().equals(boardUuid))
            throw new RelationAlreadyExistsException();

        note.setBoardUuid(boardUuid);
        board.addNote(note.getUuid());

        noteDb.updateNote(note);
        updateBoard(board);

    }

    @Override
    public void removeNoteBoardRelation(String boardUuid, String noteUuid)
            throws DatabaseException {

        Note note = noteDb.getNote(noteUuid);
        Board board = getBoard(boardUuid);

        note.setBoardUuid(null);
        board.removeNote(note);

        noteDb.updateNote(note);
        updateBoard(board);
    }


}
