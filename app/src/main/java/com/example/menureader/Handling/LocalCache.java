package com.example.menureader.Handling;

import com.example.menureader.LogHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalCache {
    private static class Node {
        public String query;
        public ImageObjectList val;
        public int sizeBytes;
        public Node next;
        public Node prev;
    }

    private final HashMap<String, Node> lru_cache;
    private Node head; // represents eldest entry
    private Node tail; // represents newest entry
    private int MAX_CAPACITY_BYTES = 10_000_000;
    private int currSizeBytes;

    public LocalCache() {
        currSizeBytes = 0;
        lru_cache = new HashMap<>();
        head = null;
        tail = null;
    }

    public LocalCache(int capacity) {
        this();
        MAX_CAPACITY_BYTES = capacity;
    }

    private void sizeUpdatedFlag() {
        while (currSizeBytes > MAX_CAPACITY_BYTES) {
            if (head == null) {
                LogHandler.m("Max capacity bytes is too low");
                return;
            }
            Node n = lru_cache.get(head.query);
            removeNode(n, true);
            if (n.sizeBytes != n.val.sizeBytes()) {
                throw new IllegalStateException("The query " + n.query + " has not updated the cache size properly.");
            }
            currSizeBytes -= n.val.sizeBytes();
        }
    }
    private void innerPut(String query, ImageObjectList val) {
        Node n = lru_cache.get(query);
        if (n == null) {
            n = new Node();
            n.query = query;
            n.val = val;
            n.sizeBytes = val.sizeBytes();

            insertNodeAtTail(n, true);
            currSizeBytes += n.sizeBytes;
        } else {
            n.val = val;

            removeNode(n, false);
            insertNodeAtTail(n, false);

            currSizeBytes -= n.sizeBytes;
            currSizeBytes += n.val.sizeBytes();
            n.sizeBytes = n.val.sizeBytes();
        }
        sizeUpdatedFlag();
    }
    public synchronized void addToList(String query, ImageObject io) {
        Node n = lru_cache.get(query);
        ImageObjectList iol;
        if (n == null) {
            LogHandler.m("Entry was evicted. Creating new ImageObjectList");
            iol = new ImageObjectList(query);
        } else {
            iol = n.val;
        }
        iol.add(io);
        innerPut(query, iol);
    }
    public synchronized void put(String query, ImageObjectList val) {
        if (val == null) {
            throw new IllegalArgumentException("ImageObjectList is null");
        }
        innerPut(query, val);
    }

    public synchronized void remove(String query) {
        Node n = lru_cache.get(query);
        if (n == null) {
            return;
        }
        removeNode(n, true);
        currSizeBytes -= n.sizeBytes;
    }

    private void removeNode(Node n, boolean removeFromMap) {
        if (n == null)
            return;
        if (removeFromMap) {
            lru_cache.remove(n.query);
        }
        if (n.next != null && n.prev != null) {
            n.prev.next = n.next;
            n.next.prev = n.prev;
        } else if (n.next == null && n.prev != null) {
            tail = tail.prev;
            tail.next = null;
        } else if (n.next != null && n.prev == null) {
            head = head.next;
            head.prev = null;
        } else {
            head = null;
            tail = null;
        }
    }

    private void insertNodeAtTail(Node n, boolean addToMap) {
        if (addToMap) {
            lru_cache.put(n.query, n);
        }
        if (tail == null) {
            head = n;
            tail = n;
            return;
        }
        tail.next = n;
        n.prev = tail;
        tail = n;
    }

    public synchronized ImageObjectList get(String query) {
        Node n = lru_cache.get(query);
        if (n == null)
            return null;
        removeNode(n, false);
        insertNodeAtTail(n, false);
        return n.val;
    }

    public synchronized int getSize() {
        return lru_cache.size();
    }

    public synchronized int getCurrSizeBytes() {
        return currSizeBytes;
    }

    // public int getIndexOfImageObject(String query) {
    // if (lru_cache.get(query) == null) return -1;
    // Node ptr = head;
    // int index = 0;
    // while (ptr != null) {
    // if (ptr.query.equals(query)) {
    // return index;
    // }
    // index++;
    // ptr = ptr.next;
    // }
    // throw new IllegalStateException("LRU Cache and linked list out of sync");
    // }
    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LRU Cache | Elements: " + lru_cache.size() + " | Bytes: " +
                currSizeBytes + " | Capacity in Bytes: " + MAX_CAPACITY_BYTES + " |");
        for (String s : lru_cache.keySet()) {
            sb.append(s + "->" + lru_cache.get(s).val.sizeBytes() + "|");
        }
        return sb.toString();
    }

    public synchronized List<Map.Entry<String, ImageObjectList>> listOfCacheObjects() {
        List<Map.Entry<String, ImageObjectList>> res = new ArrayList<>(lru_cache.size());
        Node ptr = head;
        while (ptr != null) {
            res.add(Map.entry(ptr.query, ptr.val));
            ptr = ptr.next;
        }
        return res;
    }
}
