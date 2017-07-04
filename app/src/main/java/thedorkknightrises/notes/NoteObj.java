package thedorkknightrises.notes;

import java.io.Serializable;

/**
 * Created by Samriddha Basu on 6/20/2016.
 */
public class NoteObj implements Serializable {
    public int id;
    public String title;
    public String subtitle;
    public String content;
    public String time;
    public String created_at;
    public int archived;
    public int notified;
    public String color;
    public int encrypted;
    public int pinned;
    public int tag;
    public String reminder;
    public int checklist;

    public NoteObj(int id, String title, String subtitle, String content, String time, String created_at, int archived, int notified, String color, int encrypted, int pinned, int tag, String reminder, int checklist) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.content = content;
        this.time = time;
        this.created_at = created_at;
        this.archived = archived;
        this.notified = notified;
        this.color = color;
        this.encrypted = encrypted;
        this.pinned = pinned;
        this.tag = tag;
        this.reminder = reminder;
        this.checklist = checklist;
    }

    public int getArchived() {
        return archived;
    }

    public void setArchived(int archived) {
        this.archived = archived;
    }

    public int getNotified() {
        return notified;
    }

    public void setNotified(int notified) {
        this.notified = notified;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(int encrypted) {
        this.encrypted = encrypted;
    }

    public int getPinned() {
        return pinned;
    }

    public void setPinned(int pinned) {
        this.pinned = pinned;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public int getChecklist() {
        return checklist;
    }

    public void setChecklist(int checklist) {
        this.checklist = checklist;
    }
}
