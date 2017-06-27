package thedorkknightrises.notes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import thedorkknightrises.notes.data.NotesDb;
import thedorkknightrises.notes.ui.NoteActivity;

/**
 * Created by Samriddha Basu on 6/20/2016.
 */
public class NotesAdapter extends RecyclerViewCursorAdapter<NotesAdapter.ViewHolder> {

    //public ArrayList<NoteObj> noteObjArrayList;
    private Cursor cursor;
    private Context context;
    private Activity activity;

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotesAdapter(Context c, Activity a, Cursor cursor) {
        super(cursor);
        //noteObjArrayList = arrayList;
        this.cursor = cursor;
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

    @Override
    protected void onBindViewHolder(final ViewHolder holder, Cursor cursor) {
        final int id = cursor.getInt(cursor.getColumnIndex(NotesDb.Note._ID));
        final String title = cursor.getString(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_TITLE));
        final String subtitle = cursor.getString(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_SUBTITLE));
        final String content = cursor.getString(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_CONTENT));
        final String time = cursor.getString(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_TIME));
        final String created_at = cursor.getString(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_CREATED_AT));
        final int archived = cursor.getInt(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_ARCHIVED));
        final int notified = cursor.getInt(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_NOTIFIED));
        final String color = cursor.getString(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_COLOR));
        final int encrypted = cursor.getInt(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_ENCRYPTED));
        final int pinned = cursor.getInt(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_PINNED));
        final int tag = cursor.getInt(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_TAG));
        final String reminder = cursor.getString(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_REMINDER));

        holder.title.setText(title);
        if (subtitle.equals("")) {
            holder.subtitle.setVisibility(View.GONE);
        } else holder.subtitle.setText(subtitle);
        holder.content.setText(content);
        holder.date.setText(time);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                Intent i = new Intent(context, NoteActivity.class);
                i.putExtra(NotesDb.Note._ID, id);
                i.putExtra(NotesDb.Note.COLUMN_NAME_TITLE, title);
                i.putExtra(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
                i.putExtra(NotesDb.Note.COLUMN_NAME_CONTENT, content);
                i.putExtra(NotesDb.Note.COLUMN_NAME_TIME, time);
                i.putExtra(NotesDb.Note.COLUMN_NAME_CREATED_AT, created_at);
                i.putExtra(NotesDb.Note.COLUMN_NAME_NOTIFIED, notified);
                i.putExtra(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
                i.putExtra(NotesDb.Note.COLUMN_NAME_COLOR, color);
                i.putExtra(NotesDb.Note.COLUMN_NAME_ENCRYPTED, encrypted);
                i.putExtra(NotesDb.Note.COLUMN_NAME_PINNED, pinned);
                i.putExtra(NotesDb.Note.COLUMN_NAME_TAG, tag);
                i.putExtra(NotesDb.Note.COLUMN_NAME_REMINDER, reminder);

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
                    if (!subtitle.equals("")) {
                        options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(activity, p1, p2, p3, p4, p5);
                    } else {
                        options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(activity, p1, p2, p4, p5);
                    }
                    context.startActivity(i, options.toBundle());
                } else
                    context.startActivity(i);
            }
        };
        holder.card.setOnClickListener(onClickListener);
        holder.content.setOnClickListener(onClickListener);
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        //if (noteObjArrayList == null) {
        //    return 0;
        //} else return noteObjArrayList.size();
        return cursor.getCount();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
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
