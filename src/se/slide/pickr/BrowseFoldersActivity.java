package se.slide.pickr;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import se.slide.pickr.StorageExplorer.Item;

public class BrowseFoldersActivity extends Activity {
    
    private String mFolderpath;
    
    public static final int ACTIVITY_CODE = 1;
    public static final String EXTRA_FOLDER = "folder";
    public static final String RESULT_FOLDER = "folderpath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mFolderpath = "";

        // Create the list fragment and add it as our sole content.
        if (getFragmentManager().findFragmentById(android.R.id.content) == null) {
            BrowseFoldersFragment list = new BrowseFoldersFragment();
            getFragmentManager().beginTransaction().add(android.R.id.content, list).commit();
        }
    }
    
    public void folderClicked(Item item) {
        String foldername = item.file;
        boolean up = item.up;

        if (up) {
            int index = mFolderpath.lastIndexOf("/");
            mFolderpath = mFolderpath.substring(0, index);
        }
        else if (foldername != null && !foldername.isEmpty()) {
            mFolderpath = mFolderpath + "/" + foldername;
        }
        else {
            mFolderpath = "";
        }
        
        BrowseFoldersFragment list = new BrowseFoldersFragment();
        
        Bundle extra = new Bundle();
        extra.putString(EXTRA_FOLDER, mFolderpath);
        
        list.setArguments(extra);
        
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        
        //transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_in);
        transaction.replace(android.R.id.content, list);
        transaction.commit();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.selectfolder, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        if (item.getItemId() == R.id.menu_select_folder) {

            //startActivityForResult(new Intent(this, BrowseFoldersActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), BrowseFoldersActivity.ACTIVITY_CODE);
            //setResult(ACTIVITY_CODE, getIntent());
            
            Intent intent = new Intent();
            intent.putExtra(RESULT_FOLDER, StorageExplorer.getBasePath() + mFolderpath);
            setResult(RESULT_OK, intent);
            
            finish();
            return true;
        }
        if (item.getItemId() == R.id.menu_cancel_folder) {

            //startActivityForResult(new Intent(this, BrowseFoldersActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), BrowseFoldersActivity.ACTIVITY_CODE);
            finish();
            return true;
        }
        
        return super.onMenuItemSelected(featureId, item);
    }
}
