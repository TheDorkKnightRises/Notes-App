package thedorkknightrises.notes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;

import thedorkknightrises.notes.db.NotesDb;
import thedorkknightrises.notes.ui.MainActivity;
import thedorkknightrises.notes.ui.NoteActivity;

/**
 * Created by Samriddha Basu on 6/20/2016.
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    public ArrayList<NoteObj> noteObjArrayList;
    Context context;
    Activity activity;

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotesAdapter(ArrayList<NoteObj> arrayList, Context c, Activity a) {
        noteObjArrayList = arrayList;
        context = c;
        activity = a;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NotesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_layout, parent, false);

        ViewHolder vh = new ViewHolder(v);
        vh.title = (TextView) v.findViewById(R.id.note_title);
        vh.subtitle = (TextView) v.findViewById(R.id.note_subtitle);
        vh.content = (TextView) v.findViewById(R.id.note_content);
        vh.date = (TextView) v.findViewById(R.id.note_date);
        vh.card = v.findViewById(R.id.note_card);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final NoteObj note = noteObjArrayList.get(position);
        holder.title.setText(note.title);
        if (note.subtitle.equals("")) {
            holder.subtitle.setVisibility(View.GONE);
        } else holder.subtitle.setText(note.subtitle);
        holder.content.setText(note.content);
        holder.date.setText(note.time.substring(0, 16));
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, NoteActivity.class);
                i.putExtra("id", note.id);
                i.putExtra("title", note.title);
                i.putExtra("subtitle", note.subtitle);
                i.putExtra("content", note.content);
                i.putExtra("time", note.time.substring(0, 16));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.card.setTransitionName("card");
                    holder.title.setTransitionName("title");
                    holder.title.setTransitionName("subtitle");
                    holder.title.setTransitionName("content");
                    holder.date.setTransitionName("time");
                    Pair<View, String> p1 = Pair.create(holder.card, "card");
                    Pair<View, String> p2 = Pair.create((View) holder.title, "title");
                    Pair<View, String> p3 = Pair.create((View) holder.subtitle, "subtitle");
                    Pair<View, String> p4 = Pair.create((View) holder.content, "content");
                    Pair<View, String> p5 = Pair.create((View) holder.date, "time");
                    ActivityOptionsCompat options;
                    if (!note.subtitle.equals("")) {
                        options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(activity, p1, p2, p3, p4, p5);
                    } else {
                        options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(activity, p1, p2, p4, p5);
                    }

                    context.startActivity(i, options.toBundle());
                }
                else
                    context.startActivity(i);
            }
        };
        holder.card.setOnClickListener(onClickListener);
        holder.content.setOnClickListener(onClickListener);
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (noteObjArrayList == null) {
            return 0;
        } else return noteObjArrayList.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;
        TextView title;
        TextView subtitle;
        TextView content;
        TextView date;
        View card;

        public ViewHolder(View v) {
            super(v);
            mView = v;
        }

    }

}
