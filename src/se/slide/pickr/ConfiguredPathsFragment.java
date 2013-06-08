package se.slide.pickr;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;

import se.slide.pickr.db.DatabaseManager;
import se.slide.pickr.model.Path;

import java.util.List;

public class ConfiguredPathsFragment extends ListFragment implements PathArrayAdapter.OnClickAdapterCallback {

    private PathArrayAdapter mAdapter;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        List<Path> paths = DatabaseManager.getInstance().getAllPaths();
        
        mAdapter = new PathArrayAdapter(getActivity(), R.layout.listview_item, paths, this);
        setListAdapter(mAdapter);
        
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    
    
    @Override
    public void deletePressed(int position, Path p) {
        /*
        View v = getListView().getChildAt(position);
        
        Animation animation = AnimationUtils.loadAnimation(
                getActivity(),
                android.R.anim.slide_out_right);
            
        animation.setDuration(500);

        // ensure animation final state is "persistent"
        animation.setFillAfter(true);
        animation.setStartOffset(0);
        
        v.startAnimation(animation);
        */
        
        DatabaseManager.getInstance().deletePath(p);
        
        deletePath(p);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        View lv = getListView();
        
        
        View v = lv.findViewById(R.id.active);
        
        if (v == null)
            Log.d("es", "es");
    }

    public void addPath(Path p) {
        mAdapter.add(p);
        mAdapter.notifyDataSetChanged();
    }
    
    public void deletePath(Path p) {
        mAdapter.remove(p);
        mAdapter.notifyDataSetChanged();
    }
}
