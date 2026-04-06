package com.example.menureader.Handling;

import com.example.menureader.LogHandler;

import java.util.HashMap;


public class LocalCache {
    private static class Node {
        public String query;
        public ImageObjectList val;
        public Node next;
        public Node prev;
    }
    private HashMap<String, Node> lru_cache;
    private Node head; // represents eldest entry
    private Node tail; // represents newest entry
    private final int MAX_CAPACITY_BYTES = 10_000_000;
    private int currSizeBytes;
    public LocalCache() {
        currSizeBytes = 0;
        lru_cache = new HashMap<>();
        head = null;
        tail = null;
    }
    public void put(String query, ImageObjectList val) {
        assert(val != null);
        while (val.size() + currSizeBytes >= MAX_CAPACITY_BYTES) {
            if (tail == null) {
                LogHandler.m("Max capacity bytes is too low");
                return;
            }
            Node n = lru_cache.get(query);
            removeNode(lru_cache.get(query), true);
            currSizeBytes -= n.val.size();
        }
        Node n = new Node();
        n.query = query;
        n.val = val;

        insertNodeAtTail(n, true);
        currSizeBytes += n.val.size();
    }
    public void remove(String query) {
        Node n = lru_cache.get(query);
        if (n == null) {
            return;
        }
        lru_cache.remove(query);
        currSizeBytes -= n.val.size();
    }
    public void removeNode(Node n, boolean removeFromMap) {
        if (n == null) return;
        if (removeFromMap) {
            lru_cache.remove(n.query);
        }
        if (n.next != null && n.prev != null) {
            n.prev.next = n.next;
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
    public void insertNodeAtTail(Node n, boolean addToMap) {
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
    public ImageObjectList get(String query) {
        Node n = lru_cache.get(query);
        if (n == null) return null;
        removeNode(n, false);
        insertNodeAtTail(n, false);
        return n.val;
    }

}
