package com.vsn.business;

import com.vsn.persistance.fakes.FakeBoardDatabase;
import com.vsn.persistance.fakes.FakeNoteDatabase;
import com.vsn.persistance.fakes.FakeUserDatabase;
import com.vsn.persistance.hsqldb.BoardHSQLDB;
import com.vsn.persistance.hsqldb.HSQLDBUtilities;
import com.vsn.persistance.hsqldb.NoteHSQLDB;
import com.vsn.persistance.hsqldb.UserHSQLDB;
import com.vsn.persistance.BoardDatabase;
import com.vsn.persistance.NoteDatabase;
import com.vsn.persistance.UserDatabase;
import com.vsn.business.managers.BoardManager;
import com.vsn.business.managers.NoteManager;
import com.vsn.business.managers.sessionManager.SessionManager;
import com.vsn.business.managers.UserManager;

public class DependencySelector {

    // FakeDb
//    private static UserDatabase userDb = new FakeUserDatabase();
//    private static BoardDatabase boardDb = new FakeBoardDatabase();
//    private static NoteDatabase noteDb = new FakeNoteDatabase();

    // HSQLDb
    private static UserDatabase userDb = new UserHSQLDB(
            HSQLDBUtilities.getDBPathName());
    private static BoardDatabase boardDb = new BoardHSQLDB(
            HSQLDBUtilities.getDBPathName());
    private static NoteDatabase noteDb = new NoteHSQLDB(
            HSQLDBUtilities.getDBPathName());
    private static NoteManager noteManager = new NoteManager();
    private static UserManager userManager = new UserManager();
    private static BoardManager boardManager = new BoardManager();
    private static SessionManager sessionManager = new SessionManager();

    public static UserDatabase getUserDatabase(){
        return userDb;
    }

    public static void setUserDatabase(UserDatabase userDb){
        DependencySelector.userDb = userDb;
    }

    public static NoteDatabase getNoteDatabase(){
        return noteDb;
    }

    public static void setNoteDatabase(NoteDatabase noteDb){
        DependencySelector.noteDb = noteDb;
    }

    public static BoardDatabase getBoardDatabase(){
        return boardDb;
    }

    public static void setBoardDatabase(BoardDatabase boardDb){
        DependencySelector.boardDb = boardDb;
    }

    public static SessionManager getSessionManager() { return sessionManager; }

    public static void setSessionManager(SessionManager sessionManager) {
        DependencySelector.sessionManager = sessionManager;
    }

    public static NoteManager getNoteManager() { return noteManager; }

    public static void setNoteManager(NoteManager noteManager) {
        DependencySelector.noteManager = noteManager;
    }

    public static BoardManager getBoardManager() { return boardManager; }

    public static void setBoardManager(BoardManager boardManager) {
        DependencySelector.boardManager = boardManager;
    }

    public static UserManager getUserManager() { return userManager; }

    public static void setUserManager(UserManager userManager) {
        DependencySelector.userManager = userManager;
    }

    public static void refreshManagerClasses(){
        userManager = new UserManager();
        noteManager = new NoteManager();
        boardManager = new BoardManager();
        sessionManager = new SessionManager();
    }
}

