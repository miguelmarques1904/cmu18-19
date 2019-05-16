package pt.ist.cmu.models;

import com.google.gson.annotations.SerializedName;

public class Membership {

    @SerializedName("user")
    private String username;

    @SerializedName("catalog")
    private String catalog;

    @SerializedName("key")
    private String key;

    public Membership(String username, String catalog, String key) {
        this.username = username;
        this.catalog = catalog;
        this.key = key;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
