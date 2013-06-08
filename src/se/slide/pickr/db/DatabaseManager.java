
package se.slide.pickr.db;

import android.content.Context;

import se.slide.pickr.model.Path;

import java.sql.SQLException;
import java.util.List;

public class DatabaseManager {
    static private DatabaseManager instance;
    private DatabaseHelper helper;

    static public void init(Context ctx) {
        if (instance == null) {
            instance = new DatabaseManager(ctx);
        }
    }

    static public DatabaseManager getInstance() {
        return instance;
    }

    private DatabaseManager(Context ctx) {
        helper = new DatabaseHelper(ctx);
    }

    private DatabaseHelper getHelper() {
        return helper;
    }

    public List<Path> getAllPaths() {
        List<Path> paths = null;
        try {
            paths = getHelper().getPathDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paths;
    }

    public void addPath(Path f) {
        try {
            getHelper().getPathDao().createOrUpdate(f);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void deletePath(Path f) {
        try {
            getHelper().getPathDao().delete(f);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Path> getPath(int id) {
        List<Path> paths = null;
        try {
            paths = getHelper().getPathDao().query(
                    getHelper().getPathDao().queryBuilder().where().eq("id", id).prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paths;
    }
}