package org.smartregister.cbhc.notification;

import java.io.Serializable;

public class NotificationDTO implements Serializable {
    private String text;
    private long timestamp;
    private String date;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
