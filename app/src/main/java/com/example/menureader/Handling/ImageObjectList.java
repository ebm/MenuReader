package com.example.menureader.Handling;

import java.util.HashSet;

public class ImageObjectList {
    private HashSet<ImageObject> imageList;
    private int sizeBytes;
    private LocalCache lc;

    public ImageObjectList(LocalCache lc) {
        sizeBytes = 0;
        imageList = new HashSet<>();
        this.lc = lc;
    }
    public boolean contains(ImageObject io) {
        return imageList.contains(io);
    }
    public void add(ImageObject io) {
        if (io == null || imageList.contains(io)) {
            throw new IllegalArgumentException("Attempted to add null or duplicate");
        }
        lc.updateSize(io.getSizeBytes());
        sizeBytes += io.getSizeBytes();
        imageList.add(io);
    }
    public void remove(ImageObject io) {
        if (io == null || !imageList.contains(io)) {
            throw new IllegalArgumentException("Attempted to remove null or nonexistent");
        }
        lc.updateSize(-io.getSizeBytes());
        sizeBytes -= io.getSizeBytes();
        imageList.remove(io);
    }
    public int sizeBytes() {
        return sizeBytes;
    }
    public int size() {
        return imageList.size();
    }
}
