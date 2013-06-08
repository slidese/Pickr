package se.slide.pickr;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import se.slide.pickr.db.DatabaseManager;
import se.slide.pickr.model.Path;

public class ConfiguredPathsActivity extends Activity {
    
    //private final int fragmentId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DatabaseManager.init(this);
        
        //setContentView(R.layout.configured_paths);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        if (savedInstanceState == null) {
            ConfiguredPathsFragment list = new ConfiguredPathsFragment();
            getFragmentManager().beginTransaction().add(android.R.id.content, list).commit();
        }
        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.folderslist, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        if (item.getItemId() == R.id.menu_add_folder) {

            startActivityForResult(new Intent(this, BrowseFoldersActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), BrowseFoldersActivity.ACTIVITY_CODE);

            return true;
        }
        
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == BrowseFoldersActivity.ACTIVITY_CODE && data != null && data.hasExtra(BrowseFoldersActivity.RESULT_FOLDER)) {
                Path p = addPath(data.getStringExtra(BrowseFoldersActivity.RESULT_FOLDER));
                pushPath(p);
                
                startService(new Intent(this, PickrService.class));
            }
        }
    }
    
    private Path addPath(String path) {
        if (path == null || path.isEmpty())
            return null;

        Path p = new Path();
        p.setPath(path);
        
        DatabaseManager.getInstance().addPath(p);
        
        Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
        
        return p;
    }
    
    private void pushPath(Path p) {
        if (p == null)
            return;
        
        Fragment fragment = getFragmentManager().findFragmentById(android.R.id.content);
        if (fragment != null) {
            ConfiguredPathsFragment frag = (ConfiguredPathsFragment) fragment;
            frag.addPath(p);
        }
    }
}
