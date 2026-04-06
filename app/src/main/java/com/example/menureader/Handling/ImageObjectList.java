package com.example.menureader.Handling;

import java.util.HashSet;

public class ImageObjectList {
    private HashSet<ImageObject> imageList;
    private int size_bytes;
    private LocalCache lc;

    public ImageObjectList(LocalCache lc) {
        size_bytes = 0;
        imageList = new HashSet<>();
        this.lc = lc;
    }
    public boolean contains(ImageObject io) {
        return imageList.contains(io);
    }
    public void add(ImageObject io) {
        assert(io != null);
        lc.updateSize(io.getSize());
        size_bytes += io.getSize();
        imageList.add(io);
    }
    public void remove(ImageObject io) {
        assert(io != null && imageList.contains(io));
        lc.updateSize(-io.getSize());
        size_bytes -= io.getSize();
        imageList.remove(io);
    }
    public int size() {
        return imageList.size();
    }
}
