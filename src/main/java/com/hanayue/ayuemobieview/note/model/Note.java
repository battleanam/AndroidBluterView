package com.hanayue.ayuemobieview.note.model;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class Note extends LitePalSupport {
    @Column(unique = true, nullable = false)
    private long id;
    private String userId;
    private String title;
    private String content;
    private long noteTime;
    private long createTime;



    public Note() {
        setId(System.currentTimeMillis());
        this.title = "";
        this.content = "";
    }

    public Note(String title, String content, long noteTime) {
        setId(System.currentTimeMillis());
        this.title = title;
        this.content = content;
        this.noteTime = noteTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getNoteTime() {
        return noteTime;
    }

    public void setNoteTime(long noteTime) {
        this.noteTime = noteTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
