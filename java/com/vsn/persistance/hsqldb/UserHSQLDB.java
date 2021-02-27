package com.vsn.persistance.hsqldb;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vsn.objects.Board;
import com.vsn.objects.User;
import com.vsn.exceptions.DatabaseException;
import com.vsn.exceptions.ObjectAlreadyExistsException;
import com.vsn.exceptions.ObjectNotFoundException;
import com.vsn.persistance.BoardDatabase;
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

public class UserHSQLDB implements UserDatabase {
    private static String dbPath;

    public UserHSQLDB(String path){
        dbPath = path;
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection("jdbc:hsqldb:file:" + dbPath +
                ";shutdown=true", "SA", "");
    }

    private User userFromResultSet(final ResultSet rs) throws SQLException{
        String username = rs.getString("username");
        String password = rs.getString("password");
        String firstName = rs.getString("firstName");
        String lastName = rs.getString("lastName");
        String email = rs.getString("email");
        String metadata = rs.getString("metadata");
        String token = rs.getString("token");
        String boardString = rs.getString("boards");

        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, String>>() {}.getType();
        HashMap<String, String> meta = gson.fromJson(metadata, type);
        type = new TypeToken<HashSet<String>>() {}.getType();
        HashSet<String> boards = gson.fromJson(boardString, type);

        return new User(username, password, firstName, lastName, email,
                meta, token, boards);
    }

    @Override
    public void createUser(User user) throws DatabaseException {
        try{
            getUser(user.getUsername());
            throw new ObjectAlreadyExistsException();
        } catch(ObjectNotFoundException e) {
            try {
                final Connection c = connection();
                final PreparedStatement st = c.prepareStatement(
                        "INSERT INTO users VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
                st.setString(1, user.getUsername());
                st.setString(2, user.getPassword());
                st.setString(3, user.getFirstName());
                st.setString(4, user.getLastName());
                st.setString(5, user.getEmail());
                Gson gson = new Gson();
                st.setString(6, gson.toJson(user.getMetadata()));
                st.setString(7, user.getToken());
                st.setString(8, gson.toJson(user.getBoards()));
                st.executeUpdate();
            } catch(SQLException f){
                throw new DatabaseException();
            }
        }
    }

    @Override
    public User getUser(String username) throws ObjectNotFoundException{
        try {
            final Connection c = connection();
            final PreparedStatement st = c.prepareStatement(
                    "SELECT username, password, firstName, lastName, email," +
                            " metadata, token, boards FROM users" +
                            " WHERE username = ?");
            st.setString(1, username);
            final ResultSet rs = st.executeQuery();
            rs.next();
            final User user = userFromResultSet(rs);
            st.close();
            rs.close();
            return user;
        } catch(SQLException e){
            throw new ObjectNotFoundException(e.getMessage());
        }
    }

    @Override
    public void updateUser(User user) throws DatabaseException {
        try{
            String username = user.getUsername();
            getUser(username);
            final Connection c = connection();
            final PreparedStatement st = c.prepareStatement(
                    "UPDATE users SET password = ?, firstName = ?," +
                            "lastName = ?, email = ?, metadata = ?, token = ?, " +
                            "boards = ? WHERE username = '" + username + "'");
            st.setString(1, user.getPassword());
            st.setString(2, user.getFirstName());
            st.setString(3, user.getLastName());
            st.setString(4, user.getEmail());
            Gson gson = new Gson();
            st.setString(5, gson.toJson(user.getMetadata()));
            st.setString(6, user.getToken());
            st.setString(7, gson.toJson(user.getBoards()));
            st.executeUpdate();
            st.close();
        } catch(SQLException e){
            throw new DatabaseException();
        }
    }

    @Override
    public void deleteUser(String username) throws DatabaseException {
        try{
            BoardDatabase boardDb = DependencySelector.getBoardDatabase();
            getUser(username);
            final Connection c = connection();
            final PreparedStatement st = c.prepareStatement(
                    "DELETE FROM users WHERE username = '" + username +"'");
            st.executeUpdate();
            st.close();

            for(Board board : boardDb.getAllBoards(username)){
                if(board.getOwner().equals(username)){
                    boardDb.deleteBoard(board.getUuid());
                }else{
                    boardDb.removeBoardUserRelation(username, board.getUuid());
                }
            }
        } catch(SQLException e){
            throw new ObjectNotFoundException ();
        }
    }

    @Override
    public Collection<User> getAllUsers() throws DatabaseException {
        final List<User> users = new ArrayList<>();
        try{
            final Connection c = connection();
            Statement st = c.createStatement();
            final ResultSet rs = st.executeQuery(
                    "SELECT username, password, firstName, lastName, " +
                            "email, metadata, token, boards " +
                            "FROM users");
            while(rs.next()){
                final User user = userFromResultSet(rs);
                users.add(user);
            }

            st.close();
            rs.close();

            return users;
        } catch(SQLException e){
            throw new DatabaseException();
        }
    }

    @Override
    public void clearUsers() throws DatabaseException {
        try{
            final Connection c = connection();
            final PreparedStatement st = c.prepareStatement(
                    "DELETE FROM users");
            st.executeUpdate();
            st.close();
        } catch(SQLException e){
            throw new DatabaseException();
        }
    }
}
