package pt.ist.cmu.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Album {

    @SerializedName("album")
    private String name;

    private List<Membership> catalogs = new ArrayList<>();

    public Album(String name, ArrayList<Membership> catalogs) {
        this.name = name;
        this.catalogs = catalogs;
    }

    public Album(String name) {
        this.name = name;
    }

    public List<Membership> getCatalogs() {
        return catalogs;
    }

    public void setCatalogs(List<Membership> catalogs) {
        this.catalogs = catalogs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
