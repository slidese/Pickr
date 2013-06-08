package se.slide.pickr;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;

public class StorageExplorer {
    
    public static final String TAG = "StorageExplorer";

    public static String getBasePath() {
        return Environment.getExternalStorageDirectory().toString();
    }
    
    public static Item[] getList(String folder) {
        Item[] fileList = null;
        File path = new File(Environment.getExternalStorageDirectory() + "");
        boolean root = true;
        
        if (folder != null && !folder.isEmpty()) {
            path =  new File(Environment.getExternalStorageDirectory() + "/" + folder);
            root = false;
        }
        
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card ");
        }

        // Checks whether path exists
        if (path.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    // Filters based on whether the file is hidden or not
                    return (sel.isFile() || sel.isDirectory()) && !sel.isHidden();

                }
            };

            String[] fList = path.list(filter);
            fileList = new Item[fList.length];
            for (int i = 0; i < fList.length; i++) {
                fileList[i] = new Item(fList[i], R.drawable.ic_action_document, false, true);

                // Convert into file path
                File sel = new File(path, fList[i]);

                // Set drawables
                if (sel.isDirectory()) {
                    fileList[i].icon = R.drawable.ic_action_folder_closed;
                    fileList[i].isFile = false;
                    Log.d("DIRECTORY", fileList[i].file);
                } else {
                    Log.d("FILE", fileList[i].file);
                }
            }

            if (!root) {
                Item temp[] = new Item[fileList.length + 1];
                for (int i = 0; i < fileList.length; i++) {
                    temp[i + 1] = fileList[i];
                }
                temp[0] = new Item("Up", R.drawable.ic_action_goleft, true, false);
                fileList = temp;
            }
        } else {
            Log.e(TAG, "path does not exist");
        }
        
        if (fileList == null)
            fileList = new Item[0];
            

        return fileList;
    }
    
    public static class Item {
        public String file;
        public int icon;
        public boolean up;
        public boolean isFile;

        public Item(String file, Integer icon, boolean up, boolean isFile) {
            this.file = file;
            this.icon = icon;
            this.up = up;
            this.isFile = isFile;
        }

        @Override
        public String toString() {
            return file;
        }
    }
}
