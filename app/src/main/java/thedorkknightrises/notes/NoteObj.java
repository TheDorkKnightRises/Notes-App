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

    public NoteObj(int id, String title, String subtitle, String content, String time) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.content = content;
        this.time = time;
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

}
