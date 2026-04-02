package com.example.menureader;

import android.graphics.Rect;

import java.util.List;

public class MenuCategory {
    private String text;
    private List<MenuLine> lines;
    private Rect boundingBox;
    public MenuCategory(String text, List<MenuLine> lines, Rect boundingBox) {
        this.text = text;
        this.lines = lines;
        this.boundingBox = boundingBox;
    }
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(Rect boundingBox) {
        this.boundingBox = boundingBox;
    }

    public List<MenuLine> getLines() {
        return lines;
    }

    public void setLines(List<MenuLine> lines) {
        this.lines = lines;
    }
}
