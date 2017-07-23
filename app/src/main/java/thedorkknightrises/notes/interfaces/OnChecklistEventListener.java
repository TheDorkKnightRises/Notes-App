package thedorkknightrises.notes.interfaces;

import thedorkknightrises.notes.ui.views.ChecklistItem;

/**
 * Created by Samriddha on 23-07-2017.
 */

public interface OnChecklistEventListener {

    void onChecklistItemChecked(ChecklistItem item, boolean checked);

    void onChecklistItemRemoved(ChecklistItem item);

    void onEnterPressed(ChecklistItem item);

    void onLostFocus(ChecklistItem item);

}
