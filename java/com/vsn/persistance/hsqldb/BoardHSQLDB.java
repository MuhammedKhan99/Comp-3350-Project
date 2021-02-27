package com.vsn.persistance.hsqldb;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import com.vsn.business.DependencySelector;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BoardHSQLDB implements BoardDatabase {
    private static String dbPath;

    public BoardHSQLDB(String path){
        dbPath = path;
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection("jdbc:hsqldb:file:" + dbPath +
                ";shutdown=true", "SA", "");
    }

    private Board boardFromResultSet(final ResultSet rs) throws SQLException{
        String name = rs.getString("name");
        String owner = rs.getString("owner");
        String boardUUID = rs.getString("boardUUID");
        Boolean deletable = rs.getBoolean("deletable");
        Boolean dirty = rs.getBoolean("dirty");
        String metadata = rs.getString("metadata"); // JSON Formatted
        String usersString = rs.getString("users"); // JSON Formatted
        String notesString = rs.getString("notes"); // JSON Formatted


        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, String>>(){}.getType();
        HashMap<String, String> meta = gson.fromJson(metadata, type);
        type = new TypeToken<HashSet<String>>() {}.getType();
        HashSet<String> users = gson.fromJson(usersString, type);
        HashSet<String> notes = gson.fromJson(notesString, type);

        return new Board(name, owner, boardUUID, deletable, dirty, meta,
                users, notes);
    }

    @Override
    public void createBoard(Board board) throws DatabaseException {
        try{
            getBoard(board.getUuid());
            throw new ObjectAlreadyExistsException();
        } catch(ObjectNotFoundException e1) {
            try {
                final Connection c = connection();
                final PreparedStatement st = c.prepareStatement(
                        "INSERT INTO boards VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
                st.setString(1, board.getUuid());
                st.setString(2, board.getName());
                st.setString(3, board.getOwner());
                st.setBoolean(4, board.getDeletable());
                st.setBoolean(5, board.getDirty());
                Gson gson = new Gson();
                st.setString(6, gson.toJson(board.getMetadata()));
                st.setString(7, gson.toJson(board.getUsers()));
                st.setString(8, gson.toJson(board.getNotes()));
                st.executeUpdate();
                for(String username: board.getUsers()){
                    try {
                        addBoardUserRelation(username, board.getUuid());
                    } catch (RelationAlreadyExistsException e2) {
                        // Then the works already been done eh
                    }
                }
            } catch(SQLException f){
                throw new DatabaseException();
            }
        }
    }

    @Override
    public Board getBoard(String boardUuid) throws ObjectNotFoundException {
        try {
            final Connection c = connection();
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT boarduuid, name, owner, deletable, dirty," +
                            " metadata, users, notes FROM boards WHERE" +
                            " boarduuid = '" + boardUuid +"'");
            if(!rs.next()){
                st.close();
                rs.close();
                throw new ObjectNotFoundException();
            }
            final Board board = boardFromResultSet(rs);
            st.close();
            rs.close();
            return board;
        } catch(SQLException e){
            throw new ObjectNotFoundException();
        }
    }

    @Override
    public void updateBoard(Board board) throws DatabaseException {
        try{
            String boardUuid = board.getUuid();
            getBoard(boardUuid);
            final Connection c = connection();
            final PreparedStatement st = c.prepareStatement(
                    "UPDATE boards SET name = ?, owner = ?," +
                            "deletable = ?, dirty = ?, metadata = ?," +
                            " users = ?,  notes = ? WHERE boarduuid = '" +
                            boardUuid + "'");
            st.setString(1, board.getName());
            st.setString(2, board.getOwner());
            st.setBoolean(3, board.getDeletable());
            st.setBoolean(4, board.getDirty());
            Gson gson = new Gson();
            st.setString(5, gson.toJson(board.getMetadata()));
            st.setString(6, gson.toJson(board.getUsers()));
            st.setString(7, gson.toJson(board.getNotes()));

            st.executeUpdate();
            st.close();

            for(String username: board.getUsers()){
                try {
                    addBoardUserRelation(username, board.getUuid());
                } catch (RelationAlreadyExistsException e) {
                    // Then the works already been done eh
                }
            }
        } catch(SQLException e){
            throw new DatabaseException();
        }
    }

    @Override
    public void deleteBoard(String boardUuid) throws DatabaseException {
        try{
            NoteDatabase noteDb = DependencySelector.getNoteDatabase();
            Board board = getBoard(boardUuid);
            for(String username: board.getUsers()){
                removeBoardUserRelation(username, boardUuid);
            }
            for(Note note : noteDb.getAllNotes(boardUuid)){
                noteDb.deleteNote(note.getUuid());
            }

            final Connection c = connection();
            final PreparedStatement st = c.prepareStatement(
                    "DELETE FROM boards WHERE boarduuid = '"
                            + boardUuid + "'");
            st.executeUpdate();
            st.close();
        } catch(SQLException e){
            throw new ObjectNotFoundException ();
        }
    }

    @Override
    public Collection<Board> getAllBoards() throws DatabaseException {
        return getAllBoards(null);
    }

    @Override
    public Collection<Board> getAllBoards(String username)
            throws DatabaseException {
        final List<Board> boards = new ArrayList<>();
        try{
            final Connection c = connection();
            final PreparedStatement st;
            if(username == null) {
                st = c.prepareStatement(
                        "SELECT * FROM boards");
            }else{
                UserDatabase userDb = DependencySelector
                        .getUserDatabase();
                User user = userDb.getUser(username);
                HashSet<String> boardUuids = user.getBoards();
                for(String boardUuid: boardUuids){
                    boards.add(getBoard(boardUuid));
                }
                return boards;
            }
            final ResultSet rs = st.executeQuery();

            while(rs.next()){
                final Board board = boardFromResultSet(rs);
                boards.add(board);
            }

            st.close();
            rs.close();

            return boards;
        } catch(SQLException e){
            throw new DatabaseException();
        } catch(ObjectNotFoundException e){
            return new ArrayList<>();
        }
    }

    @Override
    public void clearBoards() throws DatabaseException {
        try{
            final Connection c = connection();
            final PreparedStatement st = c.prepareStatement(
                    "DELETE FROM boards");
            st.executeUpdate();
            st.close();
        } catch(SQLException e){
            throw new DatabaseException();
        }
    }

    @Override
    public void addBoardUserRelation(String username, String boardUuid) throws
            DatabaseException {
        UserDatabase userDb = DependencySelector.getUserDatabase();

        Board board = getBoard(boardUuid);
        User user = userDb.getUser(username);

        if(board.getUsers().contains(username) &&
                user.getBoards().contains(boardUuid))
            throw new RelationAlreadyExistsException();

        user.addBoard(boardUuid);
        board.addUser(username);

        userDb.updateUser(user);
        updateBoard(board);
    }

    @Override
    public void removeBoardUserRelation(String username, String boardUuid)
            throws DatabaseException {
        UserDatabase userDb = DependencySelector.getUserDatabase();

        Board board = getBoard(boardUuid);
        User user = userDb.getUser(username);

        user.removeBoard(boardUuid);
        board.removeUser(username);

        updateBoard(board);
        userDb.updateUser(user);
    }

    @Override
    public void addNoteBoardRelation(String boardUuid, String noteUuid)
            throws DatabaseException {
        NoteDatabase noteDb = DependencySelector.getNoteDatabase();

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
        NoteDatabase noteDb = DependencySelector.getNoteDatabase();

        Note note = noteDb.getNote(noteUuid);
        Board board = getBoard(boardUuid);

        note.setBoardUuid("");
        board.removeNote(note);

        noteDb.updateNote(note);
        updateBoard(board);
    }

}