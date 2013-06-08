package se.slide.pickr.model;

import com.j256.ormlite.field.DatabaseField;

public class Path {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String path;
    
    @DatabaseField
    private String cloudprovider;
    
    @DatabaseField
    private String albumname;
    
    @DatabaseField
    private String albumid;

    @DatabaseField
    private String googleAlbumUrl;
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCloudprovider() {
        return cloudprovider;
    }

    public void setCloudprovider(String cloudprovider) {
        this.cloudprovider = cloudprovider;
    }

    public String getAlbumname() {
        return albumname;
    }

    public void setAlbumname(String albumname) {
        this.albumname = albumname;
    }

    public String getAlbumid() {
        return albumid;
    }

    public void setAlbumid(String albumid) {
        this.albumid = albumid;
    }

    public int getId() {
        return id;
    }

    public String getGoogleAlbumUrl() {
        return googleAlbumUrl;
    }

    public void setGoogleAlbumUrl(String googleAlbumUrl) {
        this.googleAlbumUrl = googleAlbumUrl;
    }
    
    
}
