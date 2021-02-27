package com.vsn.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import org.threeten.bp.Instant;
import static android.graphics.Color.rgb;

public class Board {

    private String name;
    private String owner;
    private String uuid;
    private Boolean deletable;
    private Boolean dirty;

    private HashMap<String, String> metadata;
    private HashSet<String> users;
    private HashSet<String> notes;

    //generic new
    public Board(
            String name_,
            String owner_
    ){
        name = name_;
        owner = owner_;
        uuid = UUID.randomUUID().toString();
        deletable = true;
        dirty = true;

        notes = new HashSet<>();
        users = new HashSet<>();
        metadata = new HashMap<>();

        String now = Instant.now().toString();
        metadata.put("created", now);
        metadata.put("lastEdit", now);
        metadata.put("lastEditedBy", "Factory");
        metadata.put("noteColour", "" + rgb(255, 255, 255));

        users.add(owner);
    }

    public Board(
            String name,
            String owner,
            String uuid,
            Boolean deletable,
            Boolean dirty,
            HashMap<String, String> metadata,
            HashSet<String> users,
            HashSet<String> notes
    ){
        this.name = name;
        this.owner = owner;
        this.uuid = uuid;
        this.deletable = deletable;
        this.dirty = dirty;
        this.metadata = metadata;
        this.users = users;
        this.notes = notes;
    }

    // Get
    public String getName(){return name;}

    public String getOwner(){return owner;}

    public String getUuid(){return uuid;}

    public HashSet<String> getNotes(){
        return (HashSet<String>)notes.clone();
    }

    public HashMap<String, String> getMetadata(){
        return (HashMap<String,String>)metadata.clone();
    }



    public boolean getDeletable(){return deletable;}

    public boolean getDirty(){return dirty;}

    // Set
    public void setDeletable(boolean deletable_){
        deletable = deletable_;
    }

    public void setDirty(boolean dirty_){
        dirty = dirty_;
    }

    public void setName(String name_) {
        name = name_;
    }

    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }

    // Note Additions / Removals
    public void addNote(String noteUuid) {
        notes.add(noteUuid);
    }

    public void removeNote(Note note) {
        String noteUuid = note.getUuid();
        notes.remove(noteUuid);
    }

    public void clearBoard(){
        notes.clear();
    }

    public void addUser(String username){
        users.add(username);
    }

    public void removeUser(String username){
        users.remove(username);
    }

    public HashSet<String> getUsers(){
        return (HashSet<String>) users.clone();
    }

    public void setColour(int newColour){ metadata.put("noteColour",
            "" + newColour); }

    public int getColour(){
        return Integer.parseInt(metadata.get("noteColour"));
    }


}