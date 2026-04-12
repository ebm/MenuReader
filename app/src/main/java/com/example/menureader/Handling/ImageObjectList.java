package com.example.menureader.Handling;

import java.util.HashSet;

public class ImageObjectList {
    private HashSet<ImageObject> imageList;
    private int sizeBytes;
    private String query;

    public ImageObjectList(String query) {
        this.query = query;
        sizeBytes = 0;
        imageList = new HashSet<>();
    }

    public boolean contains(ImageObject io) {
        return imageList.contains(io);
    }

    public void add(ImageObject io) {
        if (io == null || imageList.contains(io)) {
            throw new IllegalArgumentException("Attempted to add null or duplicate");
        }
        sizeBytes += io.getSizeBytes();
        imageList.add(io);
    }

    public void remove(ImageObject io) {
        if (io == null || !imageList.contains(io)) {
            throw new IllegalArgumentException("Attempted to remove null or nonexistent");
        }
        sizeBytes -= io.getSizeBytes();
        imageList.remove(io);
    }

    public int sizeBytes() {
        return sizeBytes;
    }

    public int size() {
        return imageList.size();
    }

    public String getQuery() {
        return query;
    }

    public HashSet<ImageObject> getImageObjects() {
        return imageList;
    }
}
