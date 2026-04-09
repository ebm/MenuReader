package com.example.menureader.Handling;

import java.util.HashSet;

public class ImageObjectList {
    private HashSet<ImageObject> imageList;
    private int sizeBytes;
    private final LocalCache lc;
    private String query;

    public ImageObjectList(String query, LocalCache lc) {
        this.query = query;
        sizeBytes = 0;
        imageList = new HashSet<>();
        this.lc = lc;
    }

    public boolean contains(ImageObject io) {
        return imageList.contains(io);
    }

    public void add(ImageObject io) {
        synchronized (lc) {
            if (io == null || imageList.contains(io)) {
                throw new IllegalArgumentException("Attempted to add null or duplicate");
            }
            lc.updateSize(io.getSizeBytes(), query);
            sizeBytes += io.getSizeBytes();
            imageList.add(io);
        }
    }

    public void remove(ImageObject io) {
        if (io == null || !imageList.contains(io)) {
            throw new IllegalArgumentException("Attempted to remove null or nonexistent");
        }
        lc.updateSize(-io.getSizeBytes(), query);
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
