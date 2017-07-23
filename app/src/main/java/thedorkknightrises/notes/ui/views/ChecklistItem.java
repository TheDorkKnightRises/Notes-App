package thedorkknightrises.notes.ui.views;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import thedorkknightrises.notes.R;
import thedorkknightrises.notes.interfaces.OnChecklistEventListener;

/**
 * Created by Samriddha on 23-07-2017.
 */

public class ChecklistItem extends LinearLayout {
    Context context;
    LinearLayout rootView;
    AppCompatCheckBox checkbox;
    ImageButton add;
    EditText editText;
    ImageButton delete;
    ImageView dragHandle;
    OnChecklistEventListener listener;

    public ChecklistItem(Context context) {
        super(context);
        init(context);
    }

    public ChecklistItem(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        init(context);
    }

    public void init(final Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.view_checklist_item, this, true);

        rootView = (LinearLayout) view.findViewById(R.id.rootview);
        checkbox = (AppCompatCheckBox) view.findViewById(R.id.checklist_item_checkbox);
        add = (ImageButton) view.findViewById(R.id.checklist_item_add_icon);
        editText = (EditText) view.findViewById(R.id.checklist_item_edittext);
        delete = (ImageButton) view.findViewById(R.id.checklist_item_delete);
        dragHandle = (ImageView) view.findViewById(R.id.checklist_item_drag_handle);


        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listener.onChecklistItemChecked(ChecklistItem.this, isChecked);
            }
        });

        add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEnterPressed(ChecklistItem.this);
            }
        });

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dragHandle.setVisibility(GONE);
                    if (!isEmpty()) {
                        delete.setVisibility(VISIBLE);
                    }
                } else {
                    listener.onLostFocus(ChecklistItem.this);
                }
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    listener.onEnterPressed(ChecklistItem.this);
                    return true;
                } else {
                    return false;
                }
            }
        });

        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem();
            }
        });
    }

    public void removeItem() {
        listener.onChecklistItemRemoved(ChecklistItem.this);
    }

    public void addListener(OnChecklistEventListener listener) {
        this.listener = listener;
    }

    public Editable getText() {
        return editText.getText();
    }

    public boolean isEmpty() {
        return (getText().toString().trim().length() == 0);
    }

    public boolean isChecked() {
        return checkbox.isChecked();
    }

    @Override
    public void setBackground(Drawable background) {
        rootView.setBackground(background);
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return editText.requestFocus(direction, previouslyFocusedRect);
    }
}
