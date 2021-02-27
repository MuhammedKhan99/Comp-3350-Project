package com.vsn.persistance;

import com.vsn.objects.Board;
import com.vsn.exceptions.DatabaseException;
import com.vsn.exceptions.ObjectNotFoundException;

import java.util.Collection;

public interface BoardDatabase {
    // Board CRUD
    void createBoard(Board board) throws DatabaseException;
    Board getBoard(String boardUuid) throws ObjectNotFoundException;
    void updateBoard(Board board) throws DatabaseException;
    void deleteBoard(String boardUuid) throws DatabaseException;
    Collection<Board> getAllBoards() throws DatabaseException;
    Collection<Board> getAllBoards(String username) throws DatabaseException;
    void clearBoards() throws DatabaseException;

    // Relations
    void addBoardUserRelation(String username, String boardUuid) throws
            DatabaseException;
    void removeBoardUserRelation(String username, String boardUuid)
            throws DatabaseException;
    void addNoteBoardRelation(String boardUuid, String noteUuid) throws
            DatabaseException;
    void removeNoteBoardRelation(String boardUuid, String noteUuid)
            throws DatabaseException;

}
