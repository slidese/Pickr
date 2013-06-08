
package se.slide.pickr;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import se.slide.pickr.db.DatabaseManager;
import se.slide.pickr.model.Path;

import java.util.List;

public class PathArrayAdapter extends ArrayAdapter<Path> {

    public interface OnClickAdapterCallback {
        public void deletePressed(int position, Path p);
    }

    Context context;
    OnClickAdapterCallback callback;

    public PathArrayAdapter(Context context, int textViewResourceId, List<Path> objects, OnClickAdapterCallback callback) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.callback = callback;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final Path path = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            // convertView =
            // mInflater.inflate(android.R.layout.simple_list_item_2, null);
            convertView = mInflater.inflate(R.layout.listview_item, null);

            holder = new ViewHolder();
            holder.googlealbum = (TextView) convertView.findViewById(R.id.album);
            holder.folder = (TextView) convertView.findViewById(R.id.folder);
            holder.delete = (ImageView) convertView.findViewById(R.id.delete);
            holder.active = (CheckBox) convertView.findViewById(R.id.active);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        if (path.getAlbumname() == null || path.getAlbumname().length() < 1) {
            holder.googlealbum.setText(Html.fromHtml("<u>Select album</u>"));
        }
        else {
            holder.googlealbum.setText(path.getAlbumname());
        }

        holder.googlealbum.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Select album...", Toast.LENGTH_SHORT).show();
            }
        });

        // Grey-out widgets when active state is unchecked
        holder.active.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });

        holder.delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                callback.deletePressed(position, path);
            }
        });

        holder.folder.setText(path.getPath());
        // holder.icon.setImageResource(R.drawable.ic_action_folder_closed);

        return convertView;
    }

    private class ViewHolder {
        TextView folder;
        TextView googlealbum;
        ImageView delete;
        CheckBox active;
    }
}
