package com.vsn.objects;

import org.threeten.bp.Instant;
import java.util.HashMap;
import java.util.UUID;

import static android.graphics.Color.rgb;


public class Note {

    private String data;
    private float[] position;
    private boolean permission;
    private HashMap metaData;
    private String boardUUID;
    private String noteUUID;

    public Note(
            String data,
            float positionX,
            float positionY,
            boolean permission,
            String creator,
            String boardUuid
    ) {
        this.data = data;
        this.position = new float[] { positionX, positionY };
        this.permission = permission;

        metaData = new HashMap<String, String>();
        String creationTime = Instant.now().toString();

        metaData.put("creatorName", creator);
        metaData.put("creationTime", creationTime);
        metaData.put("lastEditTime", creationTime);
        metaData.put("lastEditUserName", creator);
        metaData.put("noteColour", Integer.toString(
                rgb(255, 255, 153)));
        noteUUID = UUID.randomUUID().toString();
        this.boardUUID = boardUuid;
    }

    public Note(
            String data,
            float positionX,
            float positionY,
            boolean permission,
            String boardUuid,
            String noteUUID,
            HashMap<String,String> metaData
    ) {
        this.data = data;
        this.position = new float[] { positionX, positionY };
        this.permission = permission;
        this.boardUUID = boardUuid;
        this.noteUUID = noteUUID;
        this.metaData = metaData;
    }

    // Getters and Setters
    public void setData(String data){ this.data = data; }

    public String getData(){ return data; }

    public void setPosition(float positionX, float positionY) {
        position[0] = positionX;
        position[1] = positionY;
    }

    public float[] getPosition(){ return position; }

    public void setPermission(boolean permission)
    { this.permission = permission; }

    public boolean getPermission() { return permission; }

    public String getUuid() {
        return this.noteUUID;
    }

    public String getBoardUuid() {
        return this.boardUUID;
    }

    public void setColour(int newColour){ metaData.put("noteColour",
            Integer.toString(newColour)); }

    public int getColour(){
        return Integer.parseInt((String)metaData.get("noteColour"));
    }

    public void setBoardUuid(String boardUuid) {
        this.boardUUID = boardUuid;
    }

    public HashMap getMetaData() { return metaData; }

}