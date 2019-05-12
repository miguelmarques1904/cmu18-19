package pt.ist.cmu.models;

import com.google.gson.annotations.SerializedName;

public class Album {

    @SerializedName("album")
    private String name;

    public Album(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
