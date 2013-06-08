package se.slide.pickr;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import se.slide.pickr.StorageExplorer.Item;

public class BrowseFoldersFragment extends ListFragment {
    
    private Activity mActivity;
    private String mFolderpath;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState == null) {
            Bundle extra = getArguments();
            
            if (extra != null && extra.containsKey(BrowseFoldersActivity.EXTRA_FOLDER))
                mFolderpath = getArguments().getString(BrowseFoldersActivity.EXTRA_FOLDER, "");
        }
        else {
            mFolderpath = "";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        Item[] list = StorageExplorer.getList(mFolderpath);
        
        //setListAdapter(new ArrayAdapter<Item>(getActivity(), android.R.layout.simple_list_item_1, list));
        
        setListAdapter(new ArrayAdapter<Item>(getActivity(),
                android.R.layout.select_dialog_item, android.R.id.text1,
                list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // creates view
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view
                        .findViewById(android.R.id.text1);

                Item item = getItem(position);
                
                // put the image on the text view
                textView.setCompoundDrawablesWithIntrinsicBounds(item.icon, 0, 0, 0);

                // add margin between image and text (support various screen
                // densities)
                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                textView.setCompoundDrawablePadding(dp5);

                return view;
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        
        Item item = (Item) (getListView().getItemAtPosition(position));
        
        if (item.isFile)
            return;
        
        if (mActivity instanceof BrowseFoldersActivity)
            ((BrowseFoldersActivity) mActivity).folderClicked(item);
    }
}
