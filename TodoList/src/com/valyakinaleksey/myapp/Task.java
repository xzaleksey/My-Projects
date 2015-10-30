package com.valyakinaleksey.myapp;

import org.joda.time.DateTime;

public class Task {
    private static int countOfTasks;
    private final int id;
    private String description;
    private DateTime date;
    private boolean checked = false;

    public Task(String description, DateTime date) {
        this.description = description;
        this.date = date;
        id = countOfTasks++;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
