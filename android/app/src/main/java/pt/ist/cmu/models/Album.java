package pt.ist.cmu.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Album {

    @SerializedName("album")
    private String name;

    private List<Membership> memberships = new ArrayList<>();

    public Album(String name, ArrayList<Membership> memberships) {
        this.name = name;
        this.memberships = memberships;
    }

    public Album(String name) {
        this.name = name;
    }

    public List<Membership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<Membership> memberships) {
        this.memberships = memberships;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
