package com.vsn.persistance.fakes;

import com.vsn.objects.Board;
import com.vsn.objects.User;
import com.vsn.exceptions.DatabaseException;
import com.vsn.exceptions.ObjectAlreadyExistsException;
import com.vsn.exceptions.ObjectNotFoundException;
import com.vsn.persistance.BoardDatabase;
import com.vsn.persistance.UserDatabase;

import java.util.Collection;
import java.util.HashMap;

public class FakeUserDatabase implements UserDatabase {
    private static final HashMap<String, User> userDatabase = new HashMap<>();
    BoardDatabase boardDb = new FakeBoardDatabase();
    @Override
    public void createUser(User user) throws ObjectAlreadyExistsException {
        if(userDatabase.containsKey(user.getUsername())){
            throw new ObjectAlreadyExistsException();
        }
        userDatabase.put(user.getUsername(), user);
    }

    @Override
    public User getUser(String username) throws ObjectNotFoundException {
        User user = userDatabase.get(username);
        if(user == null)
            throw new ObjectNotFoundException();
        return user;
    }

    @Override
    public void updateUser(User user) throws ObjectNotFoundException {
        if(!userDatabase.containsKey(user.getUsername())){
            throw new ObjectNotFoundException();
        }
        userDatabase.put(user.getUsername(), user);
    }

    @Override
    public void deleteUser(String username) throws DatabaseException {
        if(!userDatabase.containsKey(username)){
            throw new ObjectNotFoundException();
        }
        for(Board board : boardDb.getAllBoards(username)){
            if(board.getOwner().equals(username)){
                boardDb.deleteBoard(board.getUuid());
            }else{
                boardDb.removeBoardUserRelation(username, board.getUuid());
            }
        }
        userDatabase.remove(username);
    }

    @Override
    public Collection<User> getAllUsers() {
        return ((HashMap<String, User>)(userDatabase.clone())).values();
    }

    @Override
    public void clearUsers() throws DatabaseException {
        for(User user: getAllUsers()){
            try {
                deleteUser(user.getUsername());
            } catch (ObjectNotFoundException e) {
                // Should never happen
                e.printStackTrace();
            }
        }
    }


}
