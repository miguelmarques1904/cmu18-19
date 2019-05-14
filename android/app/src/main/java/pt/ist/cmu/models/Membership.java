package pt.ist.cmu.models;

import com.google.gson.annotations.SerializedName;

public class Membership {

    @SerializedName("user")
    private String username;

    @SerializedName("catalog")
    private String catalog;

    public Membership(String username, String catalog) {
        this.username = username;
        this.catalog = catalog;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    @Override
    public boolean equals(Object o) {
        Membership m = (Membership)o;
        if (m.getUsername().equals(this.username) && m.getCatalog().equals(this.catalog)) {
            return true;
        }
        return false;
    }

}
