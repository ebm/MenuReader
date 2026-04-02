package com.example.menureader;

import android.graphics.Rect;

public class MenuLine {
    private String text;
    private Rect lineBounds;
    public MenuLine(String text, Rect lineBounds) {
        this.text = text;
        this.lineBounds = lineBounds;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Rect getLineBounds() {
        return lineBounds;
    }

    public void setLineBounds(Rect lineBounds) {
        this.lineBounds = lineBounds;
    }
}
