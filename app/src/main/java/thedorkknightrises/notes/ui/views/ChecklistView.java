package thedorkknightrises.notes.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.jmedeisis.draglinearlayout.DragLinearLayout;

import thedorkknightrises.notes.R;
import thedorkknightrises.notes.interfaces.OnChecklistEventListener;

/**
 * Created by Samriddha on 23-07-2017.
 */

public class ChecklistView extends LinearLayout implements OnChecklistEventListener {
    Context context;
    DragLinearLayout parent;
    boolean moveCheckedToBottom;
    Drawable itemBackground;

    public ChecklistView(Context context) {
        super(context);
        init(context);
    }

    public ChecklistView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DragLinearLayout getDragLinearLayout() {
        return parent;
    }

    public void init(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_checklist, this, true);

        parent = (DragLinearLayout) findViewById(R.id.draggable_rootview);
        addItem(false, true);
    }

    public void addItem(boolean draggable, boolean hasFocus) {
        ChecklistItem newItem = new ChecklistItem(context);
        if (itemBackground != null) newItem.setBackground(itemBackground);
        newItem.addListener(this);
        if (draggable) {
            parent.addDragView(newItem, newItem.dragHandle);
        } else {
            parent.addView(newItem);
        }
        if (hasFocus) newItem.requestFocus();
    }

    public void setMoveCheckedToBottom(boolean moveCheckedToBottom) {
        this.moveCheckedToBottom = moveCheckedToBottom;
    }

    @Override
    public void onChecklistItemChecked(ChecklistItem item, boolean checked) {
        if (moveCheckedToBottom) {
            parent.removeView(item);
            parent.addDragView(item, item.dragHandle);
        }
    }

    @Override
    public void onChecklistItemRemoved(ChecklistItem item) {

    }

    @Override
    public void onEnterPressed(ChecklistItem item) {
        parent.setViewDraggable(item, item.dragHandle);
        if (!((ChecklistItem) parent.getChildAt(parent.getChildCount() - 1)).isEmpty()) {
            addItem(false, true);
        }
    }

    @Override
    public void onLostFocus(ChecklistItem item) {
        item.delete.setVisibility(GONE);
        if (item.isEmpty()) {
            if (!parent.getChildAt(parent.getChildCount() - 1).equals(item)) {
                parent.removeView(item);
            } else {
                item.checkbox.setVisibility(GONE);
                item.add.setVisibility(VISIBLE);
            }
        } else {
            item.checkbox.setVisibility(VISIBLE);
            item.add.setVisibility(GONE);
            parent.setViewDraggable(item, item.dragHandle);
            item.dragHandle.setVisibility(VISIBLE);
            if (!((ChecklistItem) parent.getChildAt(parent.getChildCount() - 1)).isEmpty()) {
                addItem(false, false);
            }
        }
    }

    public void setItemBackground(Drawable itemBackground) {
        this.itemBackground = itemBackground;
    }
}
