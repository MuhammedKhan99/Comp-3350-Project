package com.vsn.business;

public class VSNState {

    private static String username = "";
    private static String boardUUID = "";
    private static String noteUUID = "";

    public static String getCurrentUsername(){
        return username;
    }

    public static void setCurrentUsername(String username){
        VSNState.username = username;
    }

    public static String getCurrentBoardUUID(){
        return boardUUID;
    }

    public static void setCurrentBoardUUID(String boardUUID){
        VSNState.boardUUID = boardUUID;
    }

    public static String getCurrentNoteUUID(){
        return noteUUID;
    }

    public static void setCurrentNoteUUID(String noteUUID){
        VSNState.noteUUID = noteUUID;
    }
}
