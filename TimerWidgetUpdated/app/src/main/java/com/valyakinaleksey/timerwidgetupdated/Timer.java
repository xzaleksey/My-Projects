package com.valyakinaleksey.timerwidgetupdated;

public class Timer {
    private int appWidgetId;
    private long startTime;
    private long elapsedTime;
    private long endTime;
    private boolean started;
    private Thread thread;

    public Timer(int appWidgetId, long endTime) {
        this.appWidgetId = appWidgetId;
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public int getAppWidgetId() {
        return appWidgetId;
    }

    public void setAppWidgetId(int appWidgetId) {
        this.appWidgetId = appWidgetId;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
}
