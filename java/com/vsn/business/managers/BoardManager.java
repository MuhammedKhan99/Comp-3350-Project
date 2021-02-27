package com.vsn.business.managers;

import com.vsn.objects.Board;
import com.vsn.exceptions.DatabaseException;
import com.vsn.exceptions.ObjectAlreadyExistsException;
import com.vsn.exceptions.ObjectNotFoundException;
import com.vsn.persistance.BoardDatabase;
import com.vsn.business.DependencySelector;

import java.util.Collection;

import static com.vsn.business.DependencySelector.getNoteManager;

public class BoardManager {
    private NoteManager noteManager;
    private BoardDatabase database;

    public BoardManager()
    {
        this.database = DependencySelector.getBoardDatabase();
        noteManager = getNoteManager();
    }

    public Board create(String boardName , String userName)
            throws DatabaseException {
        Board board = new Board(boardName,userName.toUpperCase());
        try {
            database.createBoard(board);
        } catch (ObjectAlreadyExistsException e) {
            // 1 in a Trillion year UUID collision occurrence.
        }
        return board;
    }

    public void changeTitle (String newTitle, String uuid)
            throws DatabaseException {
        Board board = getBoard(uuid);
        board.setName(newTitle);
        database.updateBoard(board);
    }

    public void updateBoard(Board board) throws DatabaseException{
        database.updateBoard(board);
    }

    public void addNote(String boardUuid, String noteUuid)
            throws DatabaseException {
        database.addNoteBoardRelation(boardUuid, noteUuid);
    }

    public void removeNote(String noteUuid)
            throws DatabaseException {
        noteManager.deleteNote(noteUuid);
    }

    public void clearBoard(String boardUuid) throws DatabaseException {
        Board board = getBoard(boardUuid);
        for(String noteUuid: board.getNotes()){
            removeNote(noteUuid);
        }
    }


    public Collection<Board> listBoards(String username)
            throws DatabaseException {
        return database.getAllBoards(username.toUpperCase());
    }

    public Collection<Board> listBoards() throws DatabaseException {
        return database.getAllBoards();
    }

    public void deleteBoard(String boardUuid) throws DatabaseException {
        database.deleteBoard(boardUuid);
    }

    public Board getBoard(String BoardUuid) throws ObjectNotFoundException {
        return database.getBoard(BoardUuid);
    }


}
