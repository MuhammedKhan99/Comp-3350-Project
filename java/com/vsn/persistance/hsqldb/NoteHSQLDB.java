package com.vsn.persistance.hsqldb;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vsn.objects.Note;
import com.vsn.exceptions.DatabaseException;
import com.vsn.exceptions.ObjectAlreadyExistsException;
import com.vsn.exceptions.ObjectNotFoundException;
import com.vsn.exceptions.RelationAlreadyExistsException;
import com.vsn.persistance.BoardDatabase;
import com.vsn.persistance.NoteDatabase;
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
import java.util.List;

public class NoteHSQLDB implements NoteDatabase {
    private static String dbPath;

    public NoteHSQLDB(String path){
        dbPath = path;
    }

    private Connection connection() throws SQLException{
        return DriverManager.getConnection("jdbc:hsqldb:file:" + dbPath +
                ";shutdown=true", "SA", "");
    }

    private Note noteFromResultSet(final ResultSet rs) throws SQLException {
        String data = rs.getString("data");
        float posx = rs.getFloat("positionX");
        float posy = rs.getFloat("positionY");
        boolean permission = rs.getBoolean("permission");
        String boardUUID = rs.getString("boardUUID");
        String noteUUID = rs.getString("noteUUID");
        String metadata = rs.getString("metadata"); // JSON Formatted

        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, String>>() {}.getType();
        HashMap<String, String> meta = gson.fromJson(metadata, type);

        return new Note(data, posx, posy, permission, boardUUID,
                noteUUID, meta);
    }


    @Override
    public void createNote(Note note) throws DatabaseException {
        try{
            getNote(note.getUuid());
            throw new ObjectAlreadyExistsException();
        } catch(ObjectNotFoundException e1) {
            try {
                BoardDatabase boardDb = DependencySelector
                        .getBoardDatabase();

                final Connection c = connection();
                final PreparedStatement st = c.prepareStatement(
                        "INSERT INTO notes VALUES(?, ?, ?, ?, ?, ?, ?)");
                st.setString(1, note.getUuid());
                st.setString(2, note.getData());
                st.setDouble(3, note.getPosition()[0]);
                st.setDouble(4, note.getPosition()[1]);
                st.setBoolean(5, note.getPermission());
                st.setString(6, note.getBoardUuid());
                Gson gson = new Gson();
                st.setString(7, gson.toJson(note.getMetaData()));
                st.executeUpdate();

                try {
                    boardDb.addNoteBoardRelation(
                            note.getBoardUuid(), note.getUuid());
                }catch(RelationAlreadyExistsException e2){
                    // Relation can't already exist.
                }catch (ObjectNotFoundException e2){
                    deleteNote(note.getUuid());
                    throw new ObjectNotFoundException("Board not found");
                }

            } catch(SQLException f){
                throw new DatabaseException();
            }
        }
    }

    @Override
    public Note getNote(String noteUuid) throws ObjectNotFoundException {
        try {
            final Connection c = connection();
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT noteuuid, data, positionx, positiony," +
                            " permission, boarduuid, metadata FROM notes" +
                            " WHERE noteuuid = '" + noteUuid + "'");
            if(rs.next()) {
                final Note note = noteFromResultSet(rs);
                rs.close();
                st.close();
                return note;
            }else{
                rs.close();
                st.close();
                throw new ObjectNotFoundException("note not found");
            }
        } catch(SQLException e){
            throw new ObjectNotFoundException();
        }
    }

    @Override
    public void updateNote(Note note) throws DatabaseException {
        try{
            String noteUuid = note.getUuid();
            getNote(noteUuid);
            final Connection c = connection();
            final PreparedStatement st = c.prepareStatement(
                    "UPDATE notes SET data = ?, positionX = ?," +
                            "positionY = ?, permission = ?, boarduuid = ?, " +
                            "metadata = ? WHERE noteuuid = '" + noteUuid + "'");
            st.setString(1, note.getData());
            st.setDouble(2, note.getPosition()[0]);
            st.setDouble(3, note.getPosition()[1]);
            st.setBoolean(4, note.getPermission());
            st.setString(5, note.getBoardUuid());
            Gson gson = new Gson();
            st.setString(6, gson.toJson(note.getMetaData()));

            st.executeUpdate();
            st.close();
        } catch(SQLException e){
            throw new DatabaseException();
        }
    }

    @Override
    public void deleteNote(String noteUuid) throws DatabaseException {
        try{
            BoardDatabase boardDb = DependencySelector
                    .getBoardDatabase();

            Note note = getNote(noteUuid);
            boardDb.removeNoteBoardRelation(note.getBoardUuid(), noteUuid);

            final Connection c = connection();
            final PreparedStatement st = c.prepareStatement(
                    "DELETE FROM notes WHERE noteuuid = '" + noteUuid + "'");
            st.executeUpdate();
            st.close();
        } catch(SQLException e){
            throw new ObjectNotFoundException ();
        }
    }

    @Override
    public Collection<Note> getAllNotes()
            throws DatabaseException {
        return getAllNotes(null);
    }

    @Override
    public Collection<Note> getAllNotes(String boardUuid)
            throws DatabaseException {
        final List<Note> notes = new ArrayList<>();
        try{
            final Connection c = connection();
            final PreparedStatement st;
            if(boardUuid == null) {
                st = c.prepareStatement(
                        "SELECT * FROM notes");
            }else{
                st = c.prepareStatement(
                        "SELECT * FROM notes WHERE boarduuid = '"
                                + boardUuid + "'");
            }
            final ResultSet rs = st.executeQuery();

            while(rs.next()){
                final Note note = noteFromResultSet(rs);
                notes.add(note);
            }

            st.close();
            rs.close();

            return notes;
        } catch(SQLException e){
            throw new DatabaseException();
        }
    }

    @Override
    public void clearNotes() throws DatabaseException {
        try{
            final Connection c = connection();
            final PreparedStatement st = c.prepareStatement(
                    "DELETE FROM notes");
            st.executeUpdate();
            st.close();
        } catch(SQLException e){
            throw new DatabaseException();
        }
    }
}
